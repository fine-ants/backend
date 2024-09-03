package co.fineants.api.domain.notification.service.provider;

import java.util.List;

import co.fineants.api.domain.notification.domain.dto.response.SentNotifyMessage;
import co.fineants.api.domain.notification.domain.entity.policy.NotificationPolicy;

/**
 * 사용자에게 알림 메시지를 전달하는 인터페이스
 * ex) FCM , Email, Slack, SMS 등
 * @param <T> 전송 데이터 타입
 */
public interface NotificationProvider<T> {
	List<SentNotifyMessage> sendNotification(List<T> data, NotificationPolicy<T> policy);
}
