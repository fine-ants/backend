package codesquad.fineants.spring.api.notification.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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

import codesquad.fineants.domain.notification.type.NotificationType;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotification;
import codesquad.fineants.spring.api.fcm.service.FcmService;
import codesquad.fineants.spring.api.firebase.FirebaseMessagingService;
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

	// 회원에게 포트폴리오의 목표 수익률 달성 알림 푸시
	public List<String> notifyPortfolioTargetGainMessages(Long portfolioId, Long memberId) {
		if (!portfolioService.reachedTargetGain(portfolioId)) {
			return Collections.emptyList();
		}
		List<String> messageIds = new ArrayList<>();
		Portfolio portfolio = portfolioService.findPortfolio(portfolioId);
		fcmService.findTokens(memberId)
			.forEach(token -> notifyPortfolioTargetGainMessage(token, portfolio)
				.ifPresent(messageIds::add));
		return messageIds;
	}

	private Optional<String> notifyPortfolioTargetGainMessage(String token, Portfolio portfolio) {
		String content = String.format("%s의 목표 수익률을 달성했습니다", portfolio.getName());
		Message message = createMessage(
			NotificationType.PORTFOLIO,
			"포트폴리오",
			content,
			portfolio.getId().toString(),
			token,
			"/portfolio/" + portfolio.getId()
		);
		return firebaseMessagingService.sendNotification(message);
	}

	// 포트폴리오 최대 손실율 달성 알림 푸시
	public List<String> notifyPortfolioMaxLossMessages(Long portfolioId, Long memberId) {
		if (!portfolioService.reachedMaxLoss(portfolioId)) {
			return Collections.emptyList();
		}
		List<String> messageIds = new ArrayList<>();
		Portfolio portfolio = portfolioService.findPortfolio(portfolioId);
		fcmService.findTokens(memberId)
			.forEach(token -> notifyPortfolioMaxLossMessage(token, portfolio)
				.ifPresent(messageIds::add));
		return messageIds;
	}

	private Optional<String> notifyPortfolioMaxLossMessage(String token, Portfolio portfolio) {
		String content = String.format("%s이(가) 최대 손실율에 도달했습니다", portfolio.getName());
		Message message = createMessage(NotificationType.PORTFOLIO, "포트폴리오", content, portfolio.getId().toString(),
			token,
			"/portfolio/" + portfolio.getId());
		try {
			String messageId = firebaseMessaging.send(message);
			log.info("푸시 알림 전송 결과 : messageId={}", messageId);
			return Optional.ofNullable(messageId);
		} catch (FirebaseMessagingException e) {
			return Optional.empty();
		}
	}

	// 종목 지정가 도달 달성 알림 푸시
	public Optional<String> notifyStockAchievedTargetPrice(String token,
		TargetPriceNotification targetPriceNotification) {
		Message message = createMessage(NotificationType.STOCK, "종목 지정가", targetPriceNotification.toMessageBody(),
			targetPriceNotification.getReferenceId(), token, "/stock/" + targetPriceNotification.getReferenceId());
		try {
			String messageId = firebaseMessaging.send(message);
			log.info("푸시 알림 전송 결과 : messageId={}", messageId);
			return Optional.ofNullable(messageId);
		} catch (FirebaseMessagingException e) {
			return Optional.empty();
		}
	}

	private Message createMessage(NotificationType type, String title, String content, String referenceId, String token,
		String link) {
		Map<String, String> data = Map.of(
			"title", title,
			"body", content,
			"type", type.name().toLowerCase(),
			"referenceId", referenceId,
			"timestamp", LocalDateTime.now().toString()
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
