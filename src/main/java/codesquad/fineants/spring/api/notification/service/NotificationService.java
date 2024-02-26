package codesquad.fineants.spring.api.notification.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
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
import codesquad.fineants.domain.target_price_notification.TargetPriceNotification;
import codesquad.fineants.spring.api.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.NotificationPreferenceErrorCode;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.fcm.service.FcmService;
import codesquad.fineants.spring.api.firebase.FirebaseMessagingService;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.notification.request.NotificationCreateRequest;
import codesquad.fineants.spring.api.notification.response.NotificationCreateResponse;
import codesquad.fineants.spring.api.notification.response.NotifyPortfolioMessageItem;
import codesquad.fineants.spring.api.notification.response.NotifyPortfolioMessagesResponse;
import codesquad.fineants.spring.api.portfolio.PortFolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class NotificationService {

	private final FirebaseMessaging firebaseMessaging;
	private final PortFolioService portfolioService;
	private final FcmService fcmService;
	private final FirebaseMessagingService firebaseMessagingService;
	private final NotificationRepository notificationRepository;
	private final NotificationPreferenceRepository notificationPreferenceRepository;
	private final MemberRepository memberRepository;
	private final CurrentPriceManager currentPriceManager;

	// 알림 저장
	@Transactional
	public NotificationCreateResponse createNotification(NotificationCreateRequest request, Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new NotFoundResourceException(MemberErrorCode.NOT_FOUND_MEMBER));
		codesquad.fineants.domain.notification.Notification saveNotification = notificationRepository.save(
			request.toEntity(member));
		return NotificationCreateResponse.from(saveNotification);
	}

	// 회원에게 포트폴리오의 목표 수익률 달성 알림 푸시
	@Transactional
	public NotifyPortfolioMessagesResponse notifyPortfolioTargetGainMessages(Long portfolioId,
		Long memberId) {
		// 알림 조건
		// 회원의 계정 알림 설정에서 브라우저 알림 설정 및 목표 수익률 도달 알림이 활성화되어야 함
		NotificationPreference preference = notificationPreferenceRepository.findByMemberId(memberId)
			.orElseThrow(
				() -> new NotFoundResourceException(NotificationPreferenceErrorCode.NOT_FOUND_NOTIFICATION_PREFERENCE));
		if (!preference.isPossibleTargetGainNotification()) {
			return NotifyPortfolioMessagesResponse.empty();
		}

		Portfolio portfolio = portfolioService.findPortfolioUsingJoin(portfolioId);
		portfolio.applyCurrentPriceAllHoldingsBy(currentPriceManager);
		if (!portfolio.reachedTargetGain()) {
			return NotifyPortfolioMessagesResponse.empty();
		}

		List<NotifyPortfolioMessageItem> notifications = new ArrayList<>();
		fcmService.findTokens(memberId)
			.forEach(token -> notifyPortfolioTargetGainMessage(token, portfolio)
				.ifPresent(notifications::add));

		return NotifyPortfolioMessagesResponse.from(notifications);
	}

	private Optional<NotifyPortfolioMessageItem> notifyPortfolioTargetGainMessage(String token,
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
			.map(messageId -> {
				NotificationCreateResponse response = this.createNotification(NotificationCreateRequest.builder()
						.portfolioName(portfolio.getName())
						.title(title)
						.type(NotificationType.PORTFOLIO_TARGET_GAIN)
						.referenceId(referenceId)
						.build(),
					portfolio.getMember().getId());
				log.info("알림 저장 결과 : response={}", response);
				return createNotifyPortfolioMessageItem(messageId, response);
			});
	}

	private NotifyPortfolioMessageItem createNotifyPortfolioMessageItem(String messageId,
		NotificationCreateResponse response) {
		return NotifyPortfolioMessageItem.builder()
			.notificationId(response.getNotificationId())
			.title(response.getTitle())
			.isRead(response.getIsRead())
			.type(response.getType())
			.referenceId(response.getReferenceId())
			.messageId(messageId)
			.build();
	}

	// 포트폴리오 최대 손실율 달성 알림 푸시
	@Transactional
	public NotifyPortfolioMessagesResponse notifyPortfolioMaxLossMessages(Long portfolioId, Long memberId) {
		if (!portfolioService.reachedMaxLoss(portfolioId)) {
			return NotifyPortfolioMessagesResponse.empty();
		}
		List<NotifyPortfolioMessageItem> notifications = new ArrayList<>();
		Portfolio portfolio = portfolioService.findPortfolio(portfolioId);
		fcmService.findTokens(memberId)
			.forEach(token -> notifyPortfolioMaxLossMessage(token, portfolio)
				.ifPresent(notifications::add));
		return NotifyPortfolioMessagesResponse.from(notifications);
	}

	private Optional<NotifyPortfolioMessageItem> notifyPortfolioMaxLossMessage(String token, Portfolio portfolio) {
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
			.map(messageId -> {
				NotificationCreateResponse response = this.createNotification(
					NotificationCreateRequest.builder()
						.portfolioName(portfolio.getName())
						.title(title)
						.type(NotificationType.PORTFOLIO_TARGET_GAIN)
						.referenceId(referenceId)
						.build(),
					portfolio.getMember().getId());
				log.info("알림 저장 결과 : response={}", response);
				return createNotifyPortfolioMessageItem(messageId, response);
			});
	}

	// 종목 지정가 도달 달성 알림 푸시
	public Optional<String> notifyStockAchievedTargetPrice(String token,
		TargetPriceNotification targetPriceNotification) {
		Message message = createMessage("종목 지정가", targetPriceNotification.toMessageBody(), token,
			"/stock/" + targetPriceNotification.getReferenceId());
		try {
			String messageId = firebaseMessaging.send(message);
			log.info("푸시 알림 전송 결과 : messageId={}", messageId);
			return Optional.ofNullable(messageId);
		} catch (FirebaseMessagingException e) {
			return Optional.empty();
		}
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
