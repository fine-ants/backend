package codesquad.fineants.domain.notification.service.provider;

import java.util.List;

import codesquad.fineants.domain.notification.domain.dto.response.SentNotifyMessage;

public interface NotificationProvider<T> {
	List<SentNotifyMessage> sendNotification(List<T> data);
}
