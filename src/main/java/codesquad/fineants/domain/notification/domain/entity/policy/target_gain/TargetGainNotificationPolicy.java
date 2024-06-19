package codesquad.fineants.domain.notification.domain.entity.policy.target_gain;

import java.util.Optional;

import codesquad.fineants.domain.notification.domain.dto.response.NotifyMessage;
import codesquad.fineants.domain.notification.domain.entity.policy.NotificationEvaluator;
import codesquad.fineants.domain.notification.domain.entity.policy.NotificationPolicy;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TargetGainNotificationPolicy implements NotificationPolicy<Portfolio> {

	private final NotificationEvaluator<Portfolio> evaluator;

	@Override
	public Optional<NotifyMessage> apply(Portfolio portfolio, String token) {
		if (evaluator.isSatisfiedBy(portfolio)) {
			return Optional.of(portfolio.createTargetGainMessage(token));
		}
		return Optional.empty();
	}
}
