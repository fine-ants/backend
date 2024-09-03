package co.fineants.api.domain.notification.domain.entity.policy.target_gain;

import java.util.Optional;

import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessage;
import co.fineants.api.domain.notification.domain.entity.policy.NotificationEvaluator;
import co.fineants.api.domain.notification.domain.entity.policy.NotificationPolicy;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
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
