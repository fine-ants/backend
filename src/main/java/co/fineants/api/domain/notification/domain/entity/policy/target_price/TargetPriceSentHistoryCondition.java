package co.fineants.api.domain.notification.domain.entity.policy.target_price;

import co.fineants.api.domain.notification.domain.entity.policy.NotificationCondition;
import co.fineants.api.domain.notification.repository.NotificationSentRepository;
import co.fineants.api.domain.stock_target_price.domain.entity.TargetPriceNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TargetPriceSentHistoryCondition implements NotificationCondition<TargetPriceNotification> {

	private final NotificationSentRepository manager;

	@Override
	public boolean isSatisfiedBy(TargetPriceNotification targetPriceNotification) {
		boolean result = !manager.hasTargetPriceSendHistory(targetPriceNotification.getId());
		log.debug("TargetPriceSentHistoryCondition.isSatisfiedBy: {}", result);
		return result;
	}
}
