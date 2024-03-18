package codesquad.fineants.domain.notification.policy.target_price;

import codesquad.fineants.domain.notification.policy.NotificationCondition;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotification;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TargetPriceCondition implements NotificationCondition<TargetPriceNotification> {

	private final CurrentPriceManager manager;

	@Override
	public boolean isSatisfiedBy(TargetPriceNotification targetPriceNotification) {
		return targetPriceNotification.isSameTargetPrice(manager);
	}
}
