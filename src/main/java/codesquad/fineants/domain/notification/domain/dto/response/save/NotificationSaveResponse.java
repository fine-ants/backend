package codesquad.fineants.domain.notification.domain.dto.response.save;

import codesquad.fineants.domain.notification.domain.dto.response.PortfolioNotifyMessageItem;

public interface NotificationSaveResponse {
	String getReferenceId();

	PortfolioNotifyMessageItem toNotifyMessageItemWith(String messageId);
}
