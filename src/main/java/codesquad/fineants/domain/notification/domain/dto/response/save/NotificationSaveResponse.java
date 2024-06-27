package codesquad.fineants.domain.notification.domain.dto.response.save;

import codesquad.fineants.domain.notification.domain.dto.response.NotifyMessageItem;

public interface NotificationSaveResponse {
	String getReferenceId();

	NotifyMessageItem toNotifyMessageItemWith(String messageId);
}
