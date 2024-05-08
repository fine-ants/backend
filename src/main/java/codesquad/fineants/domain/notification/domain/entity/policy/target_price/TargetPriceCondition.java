package codesquad.fineants.domain.notification.domain.entity.policy.target_price;

import codesquad.fineants.domain.notification.domain.entity.policy.NotificationCondition;
import codesquad.fineants.domain.stock_target_price.domain.entity.TargetPriceNotification;
import codesquad.fineants.domain.kis.repository.CurrentPriceRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TargetPriceCondition implements NotificationCondition<TargetPriceNotification> {

	private final CurrentPriceRepository manager;

	@Override
	public boolean isSatisfiedBy(TargetPriceNotification targetPriceNotification) {
		return targetPriceNotification.isSameTargetPrice(manager);
	}
}
