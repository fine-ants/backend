package codesquad.fineants.domain.notification.service.provider;

import java.util.List;

import codesquad.fineants.domain.notification.domain.dto.response.SentNotifyMessage;
import codesquad.fineants.domain.notification.domain.entity.policy.NotificationPolicy;

public interface NotificationProvider<T> {
	List<SentNotifyMessage> sendNotification(List<T> data, NotificationPolicy<T> policy);
}
