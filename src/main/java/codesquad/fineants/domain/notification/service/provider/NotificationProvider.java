package codesquad.fineants.domain.notification.service.provider;

import java.util.List;

public interface NotificationProvider<T> {
	void sendNotification(List<T> data);
}
