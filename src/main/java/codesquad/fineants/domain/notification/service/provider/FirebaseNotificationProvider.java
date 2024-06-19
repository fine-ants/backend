package codesquad.fineants.domain.notification.service.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import codesquad.fineants.domain.fcm.service.FcmService;
import codesquad.fineants.domain.fcm.service.FirebaseMessagingService;
import codesquad.fineants.domain.notification.domain.dto.response.NotifyMessage;
import codesquad.fineants.domain.notification.domain.dto.response.SentNotifyMessage;
import codesquad.fineants.domain.notification.domain.entity.policy.NotificationPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class FirebaseNotificationProvider<T> implements NotificationProvider<T> {

	final FcmService fcmService;
	final FirebaseMessagingService firebaseMessagingService;

	/**
	 * FCM 토큰을 이용하여 푸시 알림
	 */
	@Override
	public List<SentNotifyMessage> sendNotification(List<T> data, NotificationPolicy<T> policy) {
		List<SentNotifyMessage> result = new ArrayList<>();
		for (T element : data) {
			result.addAll(notifyIfAchieved(element, policy));
		}
		return result;
	}

	@NotNull
	private List<SentNotifyMessage> notifyIfAchieved(T target, NotificationPolicy<T> policy) {
		// Portfolio 소유한 회원의 FCM 토큰 리스트 조회
		List<String> tokens = findTokens(target);
		log.debug("tokens : {}", tokens);

		// 알림 조건을 만족하는지 검사
		List<NotifyMessage> notifyMessages = tokens.stream()
			.map(token -> policy.apply(target, token))
			.flatMap(Optional::stream)
			.toList();
		log.debug("notifyMessages : {}", notifyMessages);

		// 만족하는 포트폴리오를 대상으로 알림 데이터 생성 & 알림 전송
		List<SentNotifyMessage> result = new ArrayList<>();
		notifyMessages.forEach(notifyMessage -> {
			String messageId = firebaseMessagingService.send(notifyMessage.toMessage()).orElse(null);
			SentNotifyMessage sentNotifyMessage = SentNotifyMessage.create(notifyMessage, messageId);
			result.add(sentNotifyMessage);
		});
		log.debug("sentNotifyMessages : {}", result);

		// 알림 전송이 실패한 메시지들을 대상으로 FCM 토큰 삭제
		result.stream()
			.filter(sentNotifyMessage -> !sentNotifyMessage.hasMessageId())
			.forEach(sentNotifyMessage -> sentNotifyMessage.deleteToken(fcmService));
		return result;
	}

	public abstract List<String> findTokens(T target);
}
