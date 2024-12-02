package co.fineants.api.domain.notification.domain.dto.response.save;

import java.util.List;

import co.fineants.api.domain.notification.domain.dto.response.NotifyMessageItem;

public interface NotificationSaveResponse {
	String getReferenceId();

	String getIdToSentHistory();

	NotifyMessageItem toNotifyMessageItemWith(List<String> messageIds);
}
