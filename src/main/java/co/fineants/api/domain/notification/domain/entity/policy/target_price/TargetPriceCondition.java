package co.fineants.api.domain.notification.domain.entity.policy.target_price;

import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.notification.domain.entity.policy.NotificationCondition;
import co.fineants.api.domain.stock_target_price.domain.entity.TargetPriceNotification;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TargetPriceCondition implements NotificationCondition<TargetPriceNotification> {

	private final CurrentPriceRedisRepository manager;

	@Override
	public boolean isSatisfiedBy(TargetPriceNotification targetPriceNotification) {
		return targetPriceNotification.isSameTargetPrice(manager);
	}
}
