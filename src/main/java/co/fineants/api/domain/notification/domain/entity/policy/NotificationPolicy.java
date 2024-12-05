package co.fineants.api.domain.notification.domain.entity.policy;

import co.fineants.api.domain.common.notification.Notifiable;

public interface NotificationPolicy {

	boolean isSatisfied(Notifiable target);
}
