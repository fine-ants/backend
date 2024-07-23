package codesquad.fineants.domain.notification.domain.entity.policy.maxloss;

import codesquad.fineants.domain.notification.domain.entity.policy.NotificationCondition;
import codesquad.fineants.domain.notification.repository.NotificationSentRepository;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
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
