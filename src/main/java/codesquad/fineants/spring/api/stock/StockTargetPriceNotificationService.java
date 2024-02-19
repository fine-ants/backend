package codesquad.fineants.spring.api.stock;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.domain.stock_target_price.StockTargetPrice;
import codesquad.fineants.domain.stock_target_price.StockTargetPriceRepository;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotification;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotificationRepository;
import codesquad.fineants.spring.api.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.StockErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.errors.exception.ForBiddenException;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.stock.request.TargetPriceNotificationCreateRequest;
import codesquad.fineants.spring.api.stock.request.TargetPriceNotificationUpdateRequest;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationCreateResponse;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationDeleteResponse;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationSearchItem;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationSearchResponse;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationUpdateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockTargetPriceNotificationService {

	private static final int TARGET_PRICE_NOTIFICATION_LIMIT = 5;

	private final StockTargetPriceRepository repository;
	private final TargetPriceNotificationRepository targetPriceNotificationRepository;
	private final MemberRepository memberRepository;
	private final StockRepository stockRepository;
	private final LastDayClosingPriceManager manager;

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
}
