package codesquad.fineants.domain.notification.domain.entity.policy.target_price;

import codesquad.fineants.domain.notification.domain.entity.policy.NotificationCondition;
import codesquad.fineants.domain.stock_target_price.domain.entity.TargetPriceNotification;

public class TargetPriceActiveCondition implements NotificationCondition<TargetPriceNotification> {

	@Override
	public boolean isSatisfiedBy(TargetPriceNotification targetPriceNotification) {
		return targetPriceNotification.isActive();
	}
}
