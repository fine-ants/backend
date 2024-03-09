package codesquad.fineants.spring.api.notification.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushFcmOptions;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.notification.NotificationRepository;
import codesquad.fineants.domain.notification.type.NotificationType;
import codesquad.fineants.domain.notification_preference.NotificationPreference;
import codesquad.fineants.domain.notification_preference.NotificationPreferenceRepository;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.purchase_history.PurchaseHistoryRepository;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotification;
import codesquad.fineants.spring.api.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.NotificationPreferenceErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.PortfolioErrorCode;
import codesquad.fineants.spring.api.errors.exception.FineAntsException;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.fcm.service.FcmService;
import codesquad.fineants.spring.api.firebase.FirebaseMessagingService;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.notification.request.PortfolioNotificationCreateRequest;
import codesquad.fineants.spring.api.notification.response.NotificationCreateResponse;
import codesquad.fineants.spring.api.notification.response.NotifyMessageItem;
import codesquad.fineants.spring.api.notification.response.NotifyPortfolioMessagesResponse;
import codesquad.fineants.spring.api.portfolio.PortFolioService;
import codesquad.fineants.spring.api.stock_target_price.request.StockTargetPriceNotificationCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class NotificationService {

	private final FirebaseMessaging firebaseMessaging;
	private final PortFolioService portfolioService;
	private final PortfolioRepository portfolioRepository;
	private final FcmService fcmService;
	private final FirebaseMessagingService firebaseMessagingService;
	private final NotificationRepository notificationRepository;
	private final NotificationPreferenceRepository notificationPreferenceRepository;
	private final MemberRepository memberRepository;
	private final CurrentPriceManager currentPriceManager;
	private final PurchaseHistoryRepository purchaseHistoryRepository;

	// 알림 저장
	@Transactional
	public NotificationCreateResponse createPortfolioNotification(PortfolioNotificationCreateRequest request,
		Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new NotFoundResourceException(MemberErrorCode.NOT_FOUND_MEMBER));
		codesquad.fineants.domain.notification.Notification saveNotification = notificationRepository.save(
			request.toEntity(member)
		);
		return NotificationCreateResponse.from(saveNotification);
	}

	// 알림 저장
	@Transactional
	public NotificationCreateResponse createStockTargetPriceNotification(
		StockTargetPriceNotificationCreateRequest request, Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new NotFoundResourceException(MemberErrorCode.NOT_FOUND_MEMBER));
		codesquad.fineants.domain.notification.Notification saveNotification = notificationRepository.save(
			request.toEntity(member)
		);
		return NotificationCreateResponse.from(saveNotification);
	}

	// 회원에게 포트폴리오의 목표 수익률 달성 알림 푸시
	@Transactional
	public NotifyPortfolioMessagesResponse notifyPortfolioTargetGainMessages(Long portfolioId,
		Long memberId) {
		// 알림 조건
		// 회원의 계정 알림 설정에서 브라우저 알림 설정 및 목표 수익률 도달 알림이 활성화되어야 함
		NotificationPreference preference = findNotificationPreference(memberId);
		if (!preference.isPossibleTargetGainNotification()) {
			log.info("계정 알림 설정 비활성화로 인한 빈 리스트 반환");
			return NotifyPortfolioMessagesResponse.empty();
		}

		Portfolio portfolio = portfolioRepository.findById(portfolioId)
			.orElseThrow(() -> new FineAntsException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
		portfolio.applyCurrentPriceAllHoldingsBy(currentPriceManager);
		List<PurchaseHistory> histories = purchaseHistoryRepository.findAllByHoldingIds(
			portfolio.getPortfolioHoldings().stream()
				.map(PortfolioHolding::getId)
				.collect(Collectors.toList()));
		if (!portfolio.reachedTargetGain(histories)) {
			log.info("목표 수익률 미달로 인한 빈 리스트 반환");
			return NotifyPortfolioMessagesResponse.empty();
		}

		List<NotifyMessageItem> notifications = new ArrayList<>();
		fcmService.findTokens(memberId)
			.forEach(token -> notifyPortfolioTargetGainMessage(token, portfolio)
				.ifPresentOrElse(notifications::add, () -> fcmService.deleteToken(token)));

		notifications.stream()
			.findAny()
			.ifPresent(item -> {
				NotificationCreateResponse createResponse = this.createPortfolioNotification(
					PortfolioNotificationCreateRequest.builder()
						.portfolioName(portfolio.getName())
						.title(item.getTitle())
						.type(item.getType())
						.referenceId(item.getReferenceId())
						.build(),
					portfolio.getMember().getId());
				log.info("알리 저장 결과 : response={}", createResponse);
			});
		return NotifyPortfolioMessagesResponse.from(notifications);
	}

	private NotificationPreference findNotificationPreference(Long memberId) {
		return notificationPreferenceRepository.findByMemberId(memberId)
			.orElseThrow(() ->
				new NotFoundResourceException(NotificationPreferenceErrorCode.NOT_FOUND_NOTIFICATION_PREFERENCE));
	}

	private Optional<NotifyMessageItem> notifyPortfolioTargetGainMessage(String token,
		Portfolio portfolio) {
		String title = "포트폴리오";
		String content = String.format("%s의 목표 수익률을 달성했습니다", portfolio.getName());
		String referenceId = portfolio.getId().toString();
		String link = "/portfolio/" + referenceId;
		Message message = createMessage(
			title,
			content,
			token,
			link
		);
		return firebaseMessagingService.sendNotification(message)
			.map(messageId -> createNotifyMessageItem(
					messageId,
					title,
					NotificationType.PORTFOLIO_TARGET_GAIN,
					referenceId
				)
			);
	}

	private NotifyMessageItem createNotifyMessageItem(
		String messageId,
		String title,
		NotificationType type,
		String referenceId) {
		return NotifyMessageItem.builder()
			.title(title)
			.type(type)
			.referenceId(referenceId)
			.messageId(messageId)
			.build();
	}

	// 포트폴리오 최대 손실율 달성 알림 푸시
	@Transactional
	public NotifyPortfolioMessagesResponse notifyPortfolioMaxLossMessages(Long portfolioId, Long memberId) {
		// 알림 조건
		// 회원의 계정 알림 설정에서 브라우저 알림 설정 및 최대 손실율 도달 알림이 활성화되어야 함
		NotificationPreference preference = findNotificationPreference(memberId);
		if (!preference.isPossibleMaxLossNotification()) {
			log.info("계정 알림 설정 비활성화로 인한 빈 리스트 반환");
			return NotifyPortfolioMessagesResponse.empty();
		}

		Portfolio portfolio = portfolioService.findPortfolio(portfolioId);
		portfolio.applyCurrentPriceAllHoldingsBy(currentPriceManager);
		List<PurchaseHistory> histories = purchaseHistoryRepository.findAllByHoldingIds(
			portfolio.getPortfolioHoldings().stream()
				.map(PortfolioHolding::getId)
				.collect(Collectors.toList()));
		if (!portfolio.reachedMaximumLoss(histories)) {
			log.info("최대 손실율 미달로 인한 빈 리스트 반환");
			return NotifyPortfolioMessagesResponse.empty();
		}

		List<NotifyMessageItem> notifications = new ArrayList<>();
		fcmService.findTokens(memberId)
			.forEach(token -> notifyPortfolioMaxLossMessage(token, portfolio)
				.ifPresentOrElse(notifications::add, () -> fcmService.deleteToken(token)));

		notifications.stream()
			.findAny()
			.ifPresent(item -> {
				NotificationCreateResponse response = this.createPortfolioNotification(
					PortfolioNotificationCreateRequest.builder()
						.portfolioName(portfolio.getName())
						.title(item.getTitle())
						.type(item.getType())
						.referenceId(item.getReferenceId())
						.build(),
					portfolio.getMember().getId());
				log.info("알림 저장 결과 : response={}", response);
			});
		return NotifyPortfolioMessagesResponse.from(notifications);
	}

	private Optional<NotifyMessageItem> notifyPortfolioMaxLossMessage(String token, Portfolio portfolio) {
		String title = "포트폴리오";
		String content = String.format("%s이(가) 최대 손실율에 도달했습니다", portfolio.getName());
		String referenceId = portfolio.getId().toString();
		String link = "/portfolio/" + referenceId;
		Message message = createMessage(
			title,
			content,
			token,
			link
		);
		return firebaseMessagingService.sendNotification(message)
			.map(messageId -> createNotifyMessageItem(
					messageId,
					title,
					NotificationType.PORTFOLIO_MAX_LOSS,
					referenceId
				)
			);
	}

	// 종목 지정가 도달 달성 알림 푸시
	public Optional<NotifyMessageItem> notifyStockAchievedTargetPrice(String token,
		TargetPriceNotification targetPriceNotification) {
		String title = NotificationType.STOCK_TARGET_PRICE.getName();
		String content = targetPriceNotification.toMessageBody();
		String referenceId = targetPriceNotification.getReferenceId();
		String link = "/stock/" + referenceId;
		Message message = createMessage(
			title,
			content,
			token,
			link
		);
		return firebaseMessagingService.sendNotification(message)
			.map(messageId -> createNotifyMessageItem(
					messageId,
					title,
					NotificationType.STOCK_TARGET_PRICE,
					referenceId
				)
			);
	}

	private Message createMessage(String title, String content, String token,
		String link) {
		Map<String, String> data = Map.of(
			"title", title,
			"body", content
		);
		Notification notification = Notification.builder()
			.setTitle(title)
			.setBody(content)
			.build();
		return Message.builder()
			.setToken(token)
			.setNotification(notification)
			.putAllData(data)
			.setWebpushConfig(WebpushConfig.builder()
				.setFcmOptions(WebpushFcmOptions.builder()
					.setLink(link)
					.build())
				.build())
			.build();
	}
}
