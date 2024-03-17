package codesquad.fineants.domain.notification.policy.target_price;

import codesquad.fineants.domain.notification.policy.NotificationCondition;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotification;
import codesquad.fineants.spring.api.notification.manager.NotificationSentManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TargetPriceSentHistoryCondition implements NotificationCondition<TargetPriceNotification> {

	private final NotificationSentManager manager;

	@Override
	public boolean isSatisfiedBy(TargetPriceNotification targetPriceNotification) {
		boolean result = !manager.hasTargetPriceSendHistory(targetPriceNotification.getId());
		log.debug("TargetPriceSentHistoryCondition.isSatisfiedBy: {}", result);
		return result;
	}
}
