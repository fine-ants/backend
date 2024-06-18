package codesquad.fineants.domain.notification.service.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import codesquad.fineants.domain.fcm.service.FcmService;
import codesquad.fineants.domain.fcm.service.FirebaseMessagingService;
import codesquad.fineants.domain.notification.domain.dto.response.NotifyMessage;
import codesquad.fineants.domain.notification.domain.dto.response.SentNotifyMessage;
import codesquad.fineants.domain.notification.domain.entity.policy.target_gain.TargetGainNotificationPolicy;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class FirebaseNotificationProvider<T> implements NotificationProvider<T> {

	private final FcmService fcmService;
	private final FirebaseMessagingService firebaseMessagingService;
	private final TargetGainNotificationPolicy targetGainNotificationPolicy;

	/**
	 * FCM 토큰을 이용하여 푸시 알림
	 */
	@Override
	public void sendNotification(List<T> data) {
		for (T element : data) {
			if (element instanceof Portfolio portfolio) {
				// Portfolio 소유한 회원의 FCM 토큰 리스트 조회
				List<String> tokens = fcmService.findTokens(portfolio.getMember().getId());

				// 알림 조건을 만족하는지 검사
				List<NotifyMessage> notifyMessages = new ArrayList<>();
				for (String token : tokens) {
					targetGainNotificationPolicy.apply(portfolio, portfolio.getMember().getNotificationPreference(),
						token).ifPresent(notifyMessages::add);
				}

				// 만족하는 포트폴리오를 대상으로 알림 데이터 생성 & 알림 전송
				Map<String, String> messageIdMap = new HashMap<>();
				List<SentNotifyMessage> sentNotifyMessages = new ArrayList<>();
				for (NotifyMessage notifyMessage : notifyMessages) {
					firebaseMessagingService.send(notifyMessage.toMessage())
						.ifPresentOrElse(
							messageId -> {
								messageIdMap.put(notifyMessage.getReferenceId(), messageId);
								sentNotifyMessages.add(SentNotifyMessage.create(notifyMessage, messageId));
							},
							() -> fcmService.deleteToken(notifyMessage.getToken()));
				}
				log.info("포트폴리오 알림 전송 결과, sentNotifyMessage={}", sentNotifyMessages);
			} else {
				return;
			}
		}
	}
}
