package codesquad.fineants.domain.notification.domain.entity.policy.target_gain;

import java.util.Optional;

import codesquad.fineants.domain.common.notification.Notifiable;
import codesquad.fineants.domain.notification.domain.dto.response.NotifyMessage;
import codesquad.fineants.domain.notification.domain.entity.policy.NotificationEvaluator;
import codesquad.fineants.domain.notification.domain.entity.policy.NotificationPolicy;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TargetGainNotificationPolicy implements NotificationPolicy<Notifiable> {

	private final NotificationEvaluator<Portfolio> evaluator;

	@Override
	public Optional<NotifyMessage> apply(Notifiable notifiable, String token) {
		if (evaluator.isSatisfiedBy((Portfolio)notifiable)) {
			return Optional.of(notifiable.createTargetGainMessageWith(token));
		}
		return Optional.empty();
	}
}
