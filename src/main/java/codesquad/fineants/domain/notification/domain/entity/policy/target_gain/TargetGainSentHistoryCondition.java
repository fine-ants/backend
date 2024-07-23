package codesquad.fineants.domain.notification.domain.entity.policy.target_gain;

import codesquad.fineants.domain.notification.domain.entity.policy.NotificationCondition;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.notification.repository.NotificationSentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TargetGainSentHistoryCondition implements NotificationCondition<Portfolio> {

	private final NotificationSentRepository manager;

	@Override
	public boolean isSatisfiedBy(Portfolio portfolio) {
		boolean result = portfolio.hasTargetGainSentHistory(manager);
		log.debug("SentHistoryCondition isSatisfiedBy: {}", result);
		return !result;
	}
}
