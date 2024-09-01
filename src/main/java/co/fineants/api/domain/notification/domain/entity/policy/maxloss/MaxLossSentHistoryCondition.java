package co.fineants.api.domain.notification.domain.entity.policy.maxloss;

import co.fineants.api.domain.notification.domain.entity.policy.NotificationCondition;
import co.fineants.api.domain.notification.repository.NotificationSentRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MaxLossSentHistoryCondition implements NotificationCondition<Portfolio> {

	private final NotificationSentRepository manager;

	@Override
	public boolean isSatisfiedBy(Portfolio portfolio) {
		boolean result = portfolio.hasMaxLossSentHistory(manager);
		log.debug("SentHistoryCondition isSatisfiedBy: {}", result);
		return !result;
	}
}
