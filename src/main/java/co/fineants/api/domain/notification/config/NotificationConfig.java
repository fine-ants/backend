package co.fineants.api.domain.notification.config;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.notification.domain.entity.policy.MaxLossNotificationPolicy;
import co.fineants.api.domain.notification.domain.entity.policy.TargetGainNotificationPolicy;
import co.fineants.api.domain.notification.domain.entity.policy.TargetPriceNotificationPolicy;
import co.fineants.api.domain.notification.repository.NotificationSentRepository;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class NotificationConfig {

	private final NotificationSentRepository sentManager;

	@Bean
	public TargetGainNotificationPolicy targetGainNotificationPolicy() {
		List<Predicate<Notifiable>> portfolioConditions = List.of(
			Notifiable::isReached,
			Notifiable::isActive,
			notifiable -> notifiable.emptySentHistory(sentManager)
		);
		Predicate<NotificationPreference> preferencePredicate =
			NotificationPreference::isPossibleTargetGainNotification;
		return new TargetGainNotificationPolicy(portfolioConditions, preferencePredicate);
	}

	@Bean
	public MaxLossNotificationPolicy maxLossNotificationPolicy() {
		List<Predicate<Notifiable>> conditions = List.of(
			Notifiable::isReached,
			Notifiable::isActive,
			notifiable -> notifiable.emptySentHistory(sentManager)
		);
		return new MaxLossNotificationPolicy(conditions, NotificationPreference::isPossibleMaxLossNotification);
	}

	@Bean
	public TargetPriceNotificationPolicy targetPriceNotificationPolicy() {
		return new TargetPriceNotificationPolicy(
			List.of(
				Notifiable::isReached,
				Notifiable::isActive,
				notifiable -> notifiable.emptySentHistory(sentManager)
			),
			NotificationPreference::isPossibleStockTargetPriceNotification
		);
	}
}
