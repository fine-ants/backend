package codesquad.fineants.domain.notification.policy.max_loss;

import codesquad.fineants.domain.notification.policy.NotificationCondition;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.spring.api.notification.manager.NotificationSentManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MaxLossSentHistoryCondition implements NotificationCondition<Portfolio> {

	private final NotificationSentManager manager;

	@Override
	public boolean isSatisfiedBy(Portfolio portfolio) {
		boolean result = portfolio.hasMaxLossSentHistory(manager);
		log.debug("SentHistoryCondition isSatisfiedBy: {}", result);
		return !result;
	}
}
