package co.fineants.api.domain.notification.domain.entity.policy.target_price;

import co.fineants.api.domain.notification.domain.entity.policy.NotificationCondition;
import co.fineants.api.domain.stock_target_price.domain.entity.TargetPriceNotification;

public class TargetPriceActiveCondition implements NotificationCondition<TargetPriceNotification> {

	@Override
	public boolean isSatisfiedBy(TargetPriceNotification targetPriceNotification) {
		return targetPriceNotification.isActive();
	}
}
