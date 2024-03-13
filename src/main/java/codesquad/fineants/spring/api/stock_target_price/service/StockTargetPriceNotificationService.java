package codesquad.fineants.spring.api.stock_target_price.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.fcm_token.FcmRepository;
import codesquad.fineants.domain.fcm_token.FcmToken;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.domain.stock_target_price.StockTargetPrice;
import codesquad.fineants.domain.stock_target_price.StockTargetPriceRepository;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotification;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotificationRepository;
import codesquad.fineants.spring.api.common.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.common.errors.errorcode.StockErrorCode;
import codesquad.fineants.spring.api.common.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.common.errors.exception.ForBiddenException;
import codesquad.fineants.spring.api.common.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.kis.client.KisCurrentPrice;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.kis.service.KisService;
import codesquad.fineants.spring.api.notification.response.NotifyMessageItem;
import codesquad.fineants.spring.api.notification.service.NotificationService;
import codesquad.fineants.spring.api.stock_target_price.manager.TargetPriceNotificationSentManager;
import codesquad.fineants.spring.api.stock_target_price.request.StockTargetPriceNotificationCreateRequest;
import codesquad.fineants.spring.api.stock_target_price.request.TargetPriceNotificationCreateRequest;
import codesquad.fineants.spring.api.stock_target_price.request.TargetPriceNotificationUpdateRequest;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotificationCreateResponse;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotificationDeleteResponse;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotificationSearchItem;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotificationSearchResponse;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotificationSendItem;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotificationSendResponse;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotificationSpecificItem;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotificationSpecifiedSearchResponse;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotificationUpdateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockTargetPriceNotificationService {

	private static final Executor executor = Executors.newFixedThreadPool(100, r -> {
		Thread thread = new Thread(r);
		thread.setDaemon(true);
		return thread;
	});
	private static final int TARGET_PRICE_NOTIFICATION_LIMIT = 5;
	private final Predicate<TargetPriceNotification> isActivePredicate = TargetPriceNotification::isActive;
	private final Predicate<TargetPriceNotification> hasNotificationSentPredicate = new Predicate<>() {
		@Override
		public boolean test(TargetPriceNotification targetPriceNotification) {
			return !sentManager.hasNotificationSent(targetPriceNotification.getId());
		}
	};

	private final StockTargetPriceRepository repository;
	private final TargetPriceNotificationRepository targetPriceNotificationRepository;
	private final MemberRepository memberRepository;
	private final StockRepository stockRepository;
	private final LastDayClosingPriceManager manager;
	private final CurrentPriceManager currentPriceManager;
	private final TargetPriceNotificationSentManager sentManager;
	private final KisService kisService;
	private final NotificationService notificationService;
	private final FcmRepository fcmRepository;

	@Transactional
	public TargetPriceNotificationCreateResponse createStockTargetPriceNotification(
		TargetPriceNotificationCreateRequest request,
		Long memberId
	) {
		String tickerSymbol = request.getTickerSymbol();
		// 현재 지정가 알림 개수가 최대 갯수를 초과 했는지 검증
		verifyNumberOfLimitTargetPriceNotifications(tickerSymbol, memberId);
		// 지정가 알림이 이미 존재하는지 검증
		verifyExistTargetPriceNotification(tickerSymbol, request.getTargetPrice(), memberId);

		Member member = findMember(memberId);
		Stock stock = findStock(tickerSymbol);
		saveStockTargetPrice(member, stock);
		StockTargetPrice stockTargetPrice = saveStockTargetPrice(member, stock);
		TargetPriceNotification targetPriceNotification = targetPriceNotificationRepository.save(
			request.toEntity(stockTargetPrice));
		log.info("종목 지정가 알림 추가 결과 : stockTargetPrice={}, targetPriceNotification={}", stockTargetPrice,
			targetPriceNotification);
		return TargetPriceNotificationCreateResponse.from(stockTargetPrice, targetPriceNotification);
	}

	private void verifyNumberOfLimitTargetPriceNotifications(String tickerSymbol, Long memberId) {
		int size = (int)repository.findByTickerSymbolAndMemberId(tickerSymbol, memberId)
			.stream()
			.map(StockTargetPrice::getId)
			.mapToLong(id -> targetPriceNotificationRepository.findAllByStockTargetPriceId(id).size())
			.sum();
		if (size >= TARGET_PRICE_NOTIFICATION_LIMIT) {
			throw new BadRequestException(StockErrorCode.BAD_REQUEST_TARGET_PRICE_NOTIFICATION_LIMIT);
		}
	}

	private StockTargetPrice saveStockTargetPrice(Member member, Stock stock) {
		StockTargetPrice stockTargetPrice = repository.findByTickerSymbolAndMemberId(stock.getTickerSymbol(),
				member.getId())
			.orElseGet(() -> createStockTargetPrice(member, stock));
		return repository.save(stockTargetPrice);
	}

	private StockTargetPrice createStockTargetPrice(Member member, Stock stock) {
		return StockTargetPrice.builder()
			.member(member)
			.stock(stock)
			.isActive(true)
			.build();
	}

	private void verifyExistTargetPriceNotification(String tickerSymbol, Long targetPrice, Long memberId) {
		if (targetPriceNotificationRepository.findByTickerSymbolAndTargetPriceAndMemberId(tickerSymbol, targetPrice,
			memberId).isPresent()) {
			throw new BadRequestException(StockErrorCode.BAD_REQUEST_TARGET_PRICE_NOTIFICATION_EXIST);
		}
	}

	private Member findMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new NotFoundResourceException(MemberErrorCode.NOT_FOUND_MEMBER));
	}

	private Stock findStock(String tickerSymbol) {
		return stockRepository.findByTickerSymbol(tickerSymbol)
			.orElseThrow(() -> new NotFoundResourceException(StockErrorCode.NOT_FOUND_STOCK));
	}

	// 모든 회원을 대상으로 특정 티커 심볼들에 대한 종목 지정가 알림 발송
	@Transactional
	public TargetPriceNotificationSendResponse sendAllStockTargetPriceNotification(List<String> tickerSymbols) {
		return sendStockTargetPriceNotification(
			repository.findAllByTickerSymbols(tickerSymbols),
			List.of(isActivePredicate, hasNotificationSentPredicate)
		);
	}

	// 회원에 대한 종목 지정가 알림 발송
	@Transactional
	public TargetPriceNotificationSendResponse sendStockTargetPriceNotification(Long memberId) {
		return sendStockTargetPriceNotification(
			repository.findAllByMemberId(memberId),
			List.of(isActivePredicate, hasNotificationSentPredicate)
		);
	}

	private TargetPriceNotificationSendResponse sendStockTargetPriceNotification(List<StockTargetPrice> stocks,
		List<Predicate<TargetPriceNotification>> predicates) {
		log.debug("종목 지정가 알림 발송 서비스 시작, stocks={}", stocks);
		// 종목 지정가에 대한 현재가들 조회
		Map<StockTargetPrice, CompletableFuture<Long>> futureMap = stocks.stream()
			.collect(Collectors.toMap(
				stock -> stock,
				stock -> CompletableFuture.supplyAsync(() -> this.fetchCurrentPrice(stock), executor)
			));
		Map<StockTargetPrice, Long> currentPriceMap = futureMap.entrySet().stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> entry.getValue().join()
			));
		log.debug("currentPriceMap : {}", currentPriceMap);

		Predicate<TargetPriceNotification> combinedPredicate = t -> currentPriceMap.get(t.getStockTargetPrice())
			.equals(t.getTargetPrice());
		for (Predicate<TargetPriceNotification> p : predicates) {
			combinedPredicate = combinedPredicate.and(p);
		}

		// 종목의 현재가가 지정가에 맞는 것들을 조회
		List<TargetPriceNotification> targetPrices = stocks.parallelStream()
			.map(StockTargetPrice::getTargetPriceNotifications)
			.flatMap(Collection::stream)
			.filter(combinedPredicate)
			.collect(Collectors.toList());
		log.debug("targetPrices : {}", targetPrices);

		// 회원 id 추출
		List<Long> memberIds = stocks.parallelStream()
			.map(StockTargetPrice::getMember)
			.filter(member -> member.getNotificationPreference().isPossibleStockTargetPriceNotification())
			.map(Member::getId)
			.distinct()
			.collect(Collectors.toList());
		log.debug("memberIds : {}", memberIds);

		// 푸시 알림을 하기 위한 회원의 토큰들 조회
		Map<Member, List<FcmToken>> fcmTokenMap = fcmRepository.findAllByMemberIds(memberIds).parallelStream()
			.collect(Collectors.groupingBy(FcmToken::getMember));
		log.debug("fcmTokenMap : {}", fcmTokenMap);

		// 종목 지정가 알림 전송 및 저장
		List<CompletableFuture<TargetPriceNotificationSendItem>> futures = new ArrayList<>();
		targetPrices.forEach(targetPrice -> {
			Member member = targetPrice.getStockTargetPrice().getMember();
			List<FcmToken> fcmTokens = fcmTokenMap.getOrDefault(member, Collections.emptyList());
			List<CompletableFuture<NotifyMessageItem>> notifyMessageItemFutures = fcmTokens.stream()
				.map(FcmToken::getToken)
				.map(token -> CompletableFuture.supplyAsync(
					() -> notificationService.notifyStockAchievedTargetPrice(token, targetPrice), executor))
				.map(future -> future.thenApply(Optional::orElseThrow)
					.exceptionally(throwable -> null))
				.collect(Collectors.toList());

			Set<NotifyMessageItem> items = notifyMessageItemFutures.stream()
				.map(CompletableFuture::join)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

			List<CompletableFuture<TargetPriceNotificationSendItem>> sendItemFutures = items.stream()
				// 알림 저장
				.map(item -> CompletableFuture.supplyAsync(() ->
					notificationService.saveStockTargetPriceNotification(
						StockTargetPriceNotificationCreateRequest.of(item, targetPrice),
						item.getMemberId()), executor)
				)
				.map(future -> future.thenApply(TargetPriceNotificationSendItem::from))
				// 발송 이력 저장
				.map(future -> future.thenCompose(item -> {
					sentManager.addTargetPriceNotification(item.getTargetPriceNotificationId());
					return CompletableFuture.supplyAsync(() -> item, executor);
				}))
				.collect(Collectors.toList());
			futures.addAll(sendItemFutures);
		});

		// 전부 완료할때까지 대기
		List<TargetPriceNotificationSendItem> items = futures.stream()
			.map(CompletableFuture::join)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
		log.debug("TargetPriceNotificationSendItem 리스트 결과 : {}", items);

		// response 생성
		TargetPriceNotificationSendResponse response = TargetPriceNotificationSendResponse.from(items);
		log.info("종목 지정가 알림 발송 서비스 결과 : response={}", response);
		return response;
	}

	private Long fetchCurrentPrice(StockTargetPrice stock) {
		String tickerSymbol = stock.getStock().getTickerSymbol();
		// currentPrice가 없다면 kis 서버에 현재가 가져오기
		return currentPriceManager.getCurrentPrice(tickerSymbol)
			.orElseGet(() -> {
				KisCurrentPrice kisCurrentPrice = kisService.fetchCurrentPrice(tickerSymbol)
					.blockOptional(Duration.ofMinutes(1L))
					.orElseGet(() -> KisCurrentPrice.empty(tickerSymbol));
				currentPriceManager.addCurrentPrice(kisCurrentPrice);
				return kisCurrentPrice.getPrice();
			});
	}

	// 종목 지정가 알림 단일 제거
	@Transactional
	public TargetPriceNotificationDeleteResponse deleteStockTargetPriceNotification(
		Long targetPriceNotificationId,
		Long memberId
	) {
		List<Long> targetPriceNotificationIds = List.of(targetPriceNotificationId);
		// 삭제하고자 하는 종목 지정가가 존재하는지 검증
		verifyExistTargetPriceById(targetPriceNotificationIds);
		// 삭제 권한이 있는지 검증
		verifyHasDeleteAuthorization(targetPriceNotificationIds, memberId);

		TargetPriceNotification targetPriceNotification = findTargetPriceNotification(targetPriceNotificationId);
		int deletedTargetPriceNotificationCount = deleteAllTargetPriceNotification(targetPriceNotificationIds);

		// TargetPriceNotification이 한개도 없으면 StockTargetPrice 제거
		if (targetPriceNotification.getStockTargetPrice().getTargetPriceNotifications().isEmpty()) {
			repository.deleteById(targetPriceNotification.getStockTargetPrice().getId());
		}

		log.info("종목 지정가 알림 제거 결과 : targetPriceNotificationIds={}, deletedTargetPriceNotificationCount={}",
			targetPriceNotificationIds, deletedTargetPriceNotificationCount);
		return TargetPriceNotificationDeleteResponse.from(targetPriceNotificationIds);
	}

	private TargetPriceNotification findTargetPriceNotification(Long targetPriceNotificationId) {
		return targetPriceNotificationRepository.findById(targetPriceNotificationId)
			.orElseThrow(() -> new NotFoundResourceException(StockErrorCode.NOT_FOUND_TARGET_PRICE));
	}

	// 종목 지정가 존재 검증
	private void verifyExistTargetPriceById(List<Long> targetPriceNotificationIds) {
		if (targetPriceNotificationRepository.findAllById(targetPriceNotificationIds).size()
			!= targetPriceNotificationIds.size()) {
			throw new NotFoundResourceException(StockErrorCode.NOT_FOUND_TARGET_PRICE);
		}
	}

	// 지정가 알림 삭제 권한 검증
	private void verifyHasDeleteAuthorization(List<Long> targetPriceNotificationIds, Long memberId) {
		if (!targetPriceNotificationRepository.findAllById(targetPriceNotificationIds).stream()
			.allMatch(t -> t.isMatchMember(memberId))) {
			throw new ForBiddenException(StockErrorCode.FORBIDDEN_DELETE_TARGET_PRICE_NOTIFICATION);
		}
	}

	@Transactional
	public TargetPriceNotificationDeleteResponse deleteAllStockTargetPriceNotification(
		List<Long> targetPriceNotificationIds,
		String tickerSymbol,
		Long memberId
	) {
		// 존재하지 않는 종목 검증
		verifyExistStock(tickerSymbol);
		// 존재하지 않는 종목 지정가가 있는지 검증
		verifyExistTargetPriceById(targetPriceNotificationIds);
		// 지정가 알림 삭제 권한 검증
		verifyHasDeleteAuthorization(targetPriceNotificationIds, memberId);

		int deletedCount = deleteAllTargetPriceNotification(targetPriceNotificationIds);
		int deletedStockTargetPriceCount = deleteStockTargetPrice(tickerSymbol, memberId);
		log.info("종목 지정가 알림 제거 결과 : ids={}, deletedTargetPriceNotificationCount={}, deletedStockTargetPriceCount = {}",
			targetPriceNotificationIds, deletedCount, deletedStockTargetPriceCount);
		return TargetPriceNotificationDeleteResponse.from(targetPriceNotificationIds);
	}

	private void verifyExistStock(String tickerSymbol) {
		if (stockRepository.findByTickerSymbol(tickerSymbol).isEmpty()) {
			throw new NotFoundResourceException(StockErrorCode.NOT_FOUND_STOCK);
		}
	}

	private int deleteAllTargetPriceNotification(List<Long> targetPriceNotificationIds) {
		return targetPriceNotificationRepository.deleteAllByTargetPriceNotificationIds(targetPriceNotificationIds);
	}

	private int deleteStockTargetPrice(String tickerSymbol, Long memberId) {
		return repository.deleteByTickerSymbolAndMemberId(tickerSymbol, memberId);
	}

	public TargetPriceNotificationSearchResponse searchStockTargetPriceNotification(Long memberId) {
		List<StockTargetPrice> stockTargetPrices = repository.findAllByMemberId(memberId);
		List<TargetPriceNotificationSearchItem> stocks = stockTargetPrices.stream()
			.map(stockTargetPrice -> TargetPriceNotificationSearchItem.from(stockTargetPrice, manager))
			.collect(Collectors.toList());
		return TargetPriceNotificationSearchResponse.from(stocks);
	}

	@Transactional
	public TargetPriceNotificationUpdateResponse updateStockTargetPriceNotification(
		TargetPriceNotificationUpdateRequest request, Long memberId) {
		StockTargetPrice stockTargetPrice = repository.findByTickerSymbolAndMemberId(request.getTickerSymbol(),
				memberId)
			.orElseThrow(() -> new NotFoundResourceException(StockErrorCode.NOT_FOUND_STOCK_TARGET_PRICE));
		stockTargetPrice.changeIsActive(request.getIsActive());
		return TargetPriceNotificationUpdateResponse.from(stockTargetPrice);
	}

	public TargetPriceNotificationSpecifiedSearchResponse searchTargetPriceNotifications(
		String tickerSymbol,
		Long memberId
	) {
		List<TargetPriceNotificationSpecificItem> targetPrices = repository.findByTickerSymbolAndMemberIdUsingFetchJoin(
				tickerSymbol, memberId)
			.stream()
			.flatMap(stockTargetPrice -> stockTargetPrice.getTargetPriceNotifications().stream())
			.map(TargetPriceNotificationSpecificItem::from)
			.collect(Collectors.toList());
		TargetPriceNotificationSpecifiedSearchResponse response = TargetPriceNotificationSpecifiedSearchResponse.from(
			targetPrices
		);
		log.info("특정 종목의 지정가 알림들 조회 결과 : response={}", response);
		return response;
	}
}
