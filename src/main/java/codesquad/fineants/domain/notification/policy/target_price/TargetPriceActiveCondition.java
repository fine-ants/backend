package codesquad.fineants.domain.notification.policy.target_price;

import codesquad.fineants.domain.notification.policy.NotificationCondition;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotification;

public class TargetPriceActiveCondition implements NotificationCondition<TargetPriceNotification> {

	@Override
	public boolean isSatisfiedBy(TargetPriceNotification targetPriceNotification) {
		return targetPriceNotification.isActive();
	}
}
