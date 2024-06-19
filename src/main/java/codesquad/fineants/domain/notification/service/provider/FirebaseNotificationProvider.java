package codesquad.fineants.domain.notification.service.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import codesquad.fineants.domain.fcm.service.FcmService;
import codesquad.fineants.domain.fcm.service.FirebaseMessagingService;
import codesquad.fineants.domain.notification.domain.dto.response.NotifyMessage;
import codesquad.fineants.domain.notification.domain.dto.response.SentNotifyMessage;
import codesquad.fineants.domain.notification.domain.entity.policy.target_gain.TargetGainNotificationPolicy;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class FirebaseNotificationProvider<T> implements NotificationProvider<T> {

	private final FcmService fcmService;
	private final FirebaseMessagingService firebaseMessagingService;
	private final TargetGainNotificationPolicy targetGainNotificationPolicy;

	/**
	 * FCM 토큰을 이용하여 푸시 알림
	 */
	@Override
	public List<SentNotifyMessage> sendNotification(List<T> data) {
		List<SentNotifyMessage> result = new ArrayList<>();
		for (T element : data) {
			if (element instanceof Portfolio portfolio) {
				result.addAll(notifyIfAchieved(portfolio));
			}
		}
		return result;
	}

	@NotNull
	private List<SentNotifyMessage> notifyIfAchieved(Portfolio portfolio) {
		// Portfolio 소유한 회원의 FCM 토큰 리스트 조회
		List<String> tokens = fcmService.findTokens(portfolio.getMember().getId());
		log.debug("tokens : {}", tokens);

		// 알림 조건을 만족하는지 검사
		List<NotifyMessage> notifyMessages = tokens.stream()
			.map(token -> targetGainNotificationPolicy.apply(portfolio, token))
			.flatMap(Optional::stream)
			.toList();
		log.debug("notifyMessages : {}", notifyMessages);

		// 만족하는 포트폴리오를 대상으로 알림 데이터 생성 & 알림 전송
		List<SentNotifyMessage> sentNotifyMessages = new ArrayList<>();
		notifyMessages.forEach(notifyMessage -> {
			String messageId = firebaseMessagingService.send(notifyMessage.toMessage()).orElse(null);
			SentNotifyMessage sentNotifyMessage = SentNotifyMessage.create(notifyMessage, messageId);
			sentNotifyMessages.add(sentNotifyMessage);
		});
		log.debug("sentNotifyMessages : {}", sentNotifyMessages);

		// 알림 전송이 실패한 메시지들을 대상으로 FCM 토큰 삭제
		sentNotifyMessages.stream()
			.filter(sentNotifyMessage -> !sentNotifyMessage.hasMessageId())
			.forEach(sentNotifyMessage -> sentNotifyMessage.deleteToken(fcmService));
		return sentNotifyMessages;
	}
}
