package codesquad.fineants.spring.api.notification.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.notification.NotificationRepository;
import codesquad.fineants.domain.notification.policy.NotificationPolicy;
import codesquad.fineants.domain.notification.policy.max_loss.MaxLossNotificationPolicy;
import codesquad.fineants.domain.notification.policy.target_gain.TargetGainNotificationPolicy;
import codesquad.fineants.domain.notification.policy.target_price.TargetPriceNotificationPolicy;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.domain.stock_target_price.StockTargetPrice;
import codesquad.fineants.domain.stock_target_price.StockTargetPriceRepository;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotification;
import codesquad.fineants.spring.api.common.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.common.errors.errorcode.PortfolioErrorCode;
import codesquad.fineants.spring.api.common.errors.exception.FineAntsException;
import codesquad.fineants.spring.api.common.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.fcm.service.FcmService;
import codesquad.fineants.spring.api.firebase.service.FirebaseMessagingService;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.notification.manager.NotificationSentManager;
import codesquad.fineants.spring.api.notification.request.PortfolioNotificationRequest;
import codesquad.fineants.spring.api.notification.response.NotifyMessage;
import codesquad.fineants.spring.api.notification.response.PortfolioNotificationResponse;
import codesquad.fineants.spring.api.notification.response.PortfolioNotifyMessageItem;
import codesquad.fineants.spring.api.notification.response.PortfolioNotifyMessagesResponse;
import codesquad.fineants.spring.api.notification.response.SentNotifyMessage;
import codesquad.fineants.spring.api.notification.response.TargetPriceNotificationResponse;
import codesquad.fineants.spring.api.stock_target_price.request.StockNotificationRequest;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotifyMessageItem;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotifyMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class NotificationService {

	private static final Executor executor = Executors.newFixedThreadPool(100, r -> {
		Thread t = new Thread(r);
		t.setDaemon(true);
		return t;
	});

	private final PortfolioRepository portfolioRepository;
	private final FcmService fcmService;
	private final FirebaseMessagingService firebaseMessagingService;
	private final NotificationRepository notificationRepository;
	private final MemberRepository memberRepository;
	private final CurrentPriceManager currentPriceManager;
	private final NotificationSentManager sentManager;
	private final StockTargetPriceRepository stockTargetPriceRepository;
	private final TargetGainNotificationPolicy targetGainNotificationPolicy;
	private final MaxLossNotificationPolicy maximumLossNotificationPolicy;
	private final TargetPriceNotificationPolicy targetPriceNotificationPolicy;

	// 알림 저장
	@Transactional
	public PortfolioNotificationResponse saveNotification(PortfolioNotificationRequest request) {
		Member member = findMember(request.getMemberId());
		codesquad.fineants.domain.notification.Notification notification = notificationRepository.save(
			request.toEntity(member)
		);
		return PortfolioNotificationResponse.from(notification);
	}

	private Member findMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new NotFoundResourceException(MemberErrorCode.NOT_FOUND_MEMBER));
	}

	// 알림 저장
	@Transactional
	public TargetPriceNotificationResponse saveNotification(StockNotificationRequest request) {
		Member member = findMember(request.getMemberId());
		codesquad.fineants.domain.notification.Notification notification = notificationRepository.save(
			request.toEntity(member)
		);
		return TargetPriceNotificationResponse.from(notification);
	}

	// 모든 회원을 대상으로 목표 수익률 달성 알림 푸시
	@Transactional
	public PortfolioNotifyMessagesResponse notifyTargetGain() {
		// 모든 회원의 포트폴리오 중에서 입력으로 받은 종목들을 가진 포트폴리오들을 조회
		List<Portfolio> portfolios = portfolioRepository.findAllWithAll().stream()
			.peek(portfolio -> portfolio.applyCurrentPriceAllHoldingsBy(currentPriceManager))
			.collect(Collectors.toList());

		Function<PortfolioNotifyMessageItem, CompletionStage<PortfolioNotifyMessageItem>> sentFunction = item -> CompletableFuture.supplyAsync(
			() -> {
				sentManager.addTargetGainSendHistory(Long.valueOf(item.getReferenceId()));
				return item;
			}, executor);
		return notifyMessage(portfolios, targetGainNotificationPolicy, sentFunction);
	}

	// 특정 포트폴리오의 목표 수익률 달성 알림 푸시
	@Transactional
	public PortfolioNotifyMessagesResponse notifyTargetGainBy(Long portfolioId) {
		Portfolio portfolio = portfolioRepository.findByPortfolioIdWithAll(portfolioId).stream()
			.peek(p -> p.applyCurrentPriceAllHoldingsBy(currentPriceManager))
			.findFirst()
			.orElseThrow(() -> new FineAntsException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));

		Function<PortfolioNotifyMessageItem, CompletionStage<PortfolioNotifyMessageItem>> sentFunction = item -> CompletableFuture.supplyAsync(
			() -> {
				sentManager.addTargetGainSendHistory(Long.valueOf(item.getReferenceId()));
				return item;
			}, executor);
		return notifyMessage(List.of(portfolio), targetGainNotificationPolicy, sentFunction);
	}

	// 모든 트폴리오의 최대 손실율 달성 알림 푸시
	@Transactional
	public PortfolioNotifyMessagesResponse notifyMaxLoss() {
		List<Portfolio> portfolios = portfolioRepository.findAllWithAll().stream()
			.peek(portfolio -> portfolio.applyCurrentPriceAllHoldingsBy(currentPriceManager))
			.collect(Collectors.toList());
		Function<PortfolioNotifyMessageItem, CompletionStage<PortfolioNotifyMessageItem>> sentFunction = item -> CompletableFuture.supplyAsync(
			() -> {
				sentManager.addMaxLossSendHistory(Long.valueOf(item.getReferenceId()));
				return item;
			}, executor);
		return notifyMessage(portfolios, maximumLossNotificationPolicy, sentFunction);
	}

	// 특정 포트폴리오의 최대 손실율 달성 알림 푸시
	@Transactional
	public PortfolioNotifyMessagesResponse notifyMaxLoss(Long portfolioId) {
		Portfolio portfolio = portfolioRepository.findByPortfolioIdWithAll(portfolioId)
			.stream().peek(p -> p.applyCurrentPriceAllHoldingsBy(currentPriceManager))
			.findAny()
			.orElseThrow(() -> new FineAntsException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
		Function<PortfolioNotifyMessageItem, CompletionStage<PortfolioNotifyMessageItem>> sentFunction = item -> CompletableFuture.supplyAsync(
			() -> {
				sentManager.addMaxLossSendHistory(Long.valueOf(item.getReferenceId()));
				return item;
			}, executor);
		return notifyMessage(List.of(portfolio), maximumLossNotificationPolicy, sentFunction);
	}

	private PortfolioNotifyMessagesResponse notifyMessage(
		List<Portfolio> portfolios,
		NotificationPolicy<Portfolio> policy,
		Function<PortfolioNotifyMessageItem, CompletionStage<PortfolioNotifyMessageItem>> sentFunction) {
		// 포트폴리오별 FCM 토큰 리스트맵 생성
		Map<Portfolio, List<String>> fcmTokenMap = portfolios.stream()
			.collect(Collectors.toMap(
				portfolio -> portfolio,
				portfolio -> fcmService.findTokens(portfolio.getMember().getId())
			));
		// 각 토큰에 전송할 NotifyMessage 객체 생성
		List<NotifyMessage> notifyMessages = new ArrayList<>();
		for (Map.Entry<Portfolio, List<String>> entry : fcmTokenMap.entrySet()) {
			Portfolio p = entry.getKey();
			for (String token : entry.getValue()) {
				policy.apply(p, p.getMember().getNotificationPreference(), token)
					.ifPresent(notifyMessages::add);
			}
		}

		// 알림 전송
		List<SentNotifyMessage> sentNotifyMessages = new ArrayList<>();
		for (NotifyMessage notifyMessage : notifyMessages) {
			firebaseMessagingService.send(notifyMessage.toMessage())
				.ifPresentOrElse(
					messageId -> sentNotifyMessages.add(SentNotifyMessage.create(notifyMessage, messageId)),
					() -> fcmService.deleteToken(notifyMessage.getToken()));
		}

		List<CompletableFuture<PortfolioNotifyMessageItem>> futures = sentNotifyMessages.stream()
			.distinct()
			// 알림 저장
			.map(message -> CompletableFuture.supplyAsync(() ->
						this.saveNotification(PortfolioNotificationRequest.from(message.getNotifyMessage())),
					executor
				).thenCombine(CompletableFuture.supplyAsync(message::getMessageId, executor),
					PortfolioNotifyMessageItem::from)
			)
			// 발송 이력 저장
			.map(future -> future.thenCompose(sentFunction))
			.collect(Collectors.toList());

		List<PortfolioNotifyMessageItem> items = futures.stream()
			.map(CompletableFuture::join)
			.collect(Collectors.toList());
		return PortfolioNotifyMessagesResponse.create(items);
	}

	// 모든 회원을 대상으로 특정 티커 심볼들에 대한 종목 지정가 알림 발송
	@Transactional
	public TargetPriceNotifyMessageResponse notifyTargetPriceBy(List<String> tickerSymbols) {
		List<TargetPriceNotification> targetPrices = stockTargetPriceRepository.findAllByTickerSymbols(tickerSymbols)
			.stream()
			.map(StockTargetPrice::getTargetPriceNotifications)
			.flatMap(Collection::stream)
			.collect(Collectors.toList());
		Function<TargetPriceNotifyMessageItem, CompletionStage<TargetPriceNotifyMessageItem>> sentFunction = item -> CompletableFuture.supplyAsync(
			() -> {
				sentManager.addTargetPriceSendHistory(item.getTargetPriceNotificationId());
				return item;
			}, executor);
		return notifyTargetPrice(
			targetPrices,
			targetPriceNotificationPolicy,
			sentFunction
		);
	}

	// 회원에 대한 종목 지정가 알림 발송
	@Transactional
	public TargetPriceNotifyMessageResponse notifyTargetPriceBy(Long memberId) {
		List<TargetPriceNotification> targetPrices = stockTargetPriceRepository.findAllByMemberId(memberId)
			.stream()
			.map(StockTargetPrice::getTargetPriceNotifications)
			.flatMap(Collection::stream)
			.collect(Collectors.toList());

		Function<TargetPriceNotifyMessageItem, CompletionStage<TargetPriceNotifyMessageItem>> sentFunction = item -> CompletableFuture.supplyAsync(
			() -> {
				sentManager.addTargetPriceSendHistory(item.getTargetPriceNotificationId());
				return item;
			}, executor);
		return notifyTargetPrice(
			targetPrices,
			targetPriceNotificationPolicy,
			sentFunction
		);
	}

	private TargetPriceNotifyMessageResponse notifyTargetPrice(
		List<TargetPriceNotification> targetPrices,
		NotificationPolicy<TargetPriceNotification> policy,
		Function<TargetPriceNotifyMessageItem, CompletionStage<TargetPriceNotifyMessageItem>> sentFunction) {
		log.debug("종목 지정가 알림 발송 서비스 시작, targetPrices={}", targetPrices);

		// 지정가 알림별 FCM 토큰 리스트맵 생성
		Map<TargetPriceNotification, List<String>> fcmTokenMap = targetPrices.stream()
			.collect(Collectors.toMap(
				t -> t,
				t -> fcmService.findTokens(t.getStockTargetPrice().getMember().getId())
			));
		// 각 토큰에 전송할 NotifyMessage 객체 생성
		List<NotifyMessage> notifyMessages = new ArrayList<>();
		for (Map.Entry<TargetPriceNotification, List<String>> entry : fcmTokenMap.entrySet()) {
			TargetPriceNotification t = entry.getKey();
			for (String token : entry.getValue()) {
				policy.apply(t, t.getStockTargetPrice().getMember().getNotificationPreference(), token)
					.ifPresent(notifyMessages::add);
			}
		}
		log.debug("notifyMessage : {}", notifyMessages);

		// 알림 전송
		List<SentNotifyMessage> sentNotifyMessages = new ArrayList<>();
		for (NotifyMessage notifyMessage : notifyMessages) {
			firebaseMessagingService.send(notifyMessage.toMessage())
				.ifPresentOrElse(
					messageId -> sentNotifyMessages.add(SentNotifyMessage.create(notifyMessage, messageId)),
					() -> fcmService.deleteToken(notifyMessage.getToken()));
		}

		// 알림 저장
		List<CompletableFuture<TargetPriceNotifyMessageItem>> futures = sentNotifyMessages.stream()
			.distinct()
			// 알림 저장
			.map(message -> CompletableFuture.supplyAsync(() ->
						this.saveNotification(StockNotificationRequest.from(message.getNotifyMessage())),
					executor
				).thenCombine(CompletableFuture.supplyAsync(message::getMessageId, executor),
					TargetPriceNotifyMessageItem::from)
			)
			// 발송 이력 저장
			.map(future -> future.thenCompose(sentFunction))
			.collect(Collectors.toList());

		List<TargetPriceNotifyMessageItem> items = futures.stream()
			.map(CompletableFuture::join)
			.collect(Collectors.toList());
		log.debug("종목 지정가 알림 발송 서비스 결과, items={}", items);
		return TargetPriceNotifyMessageResponse.from(items);
	}

}
