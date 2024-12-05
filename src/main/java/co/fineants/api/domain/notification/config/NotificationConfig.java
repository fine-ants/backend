package co.fineants.api.domain.notification.config;

import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.notification.domain.entity.policy.AbstractNotificationPolicy;
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
		return AbstractNotificationPolicy.targetGainNotificationPolicy(
			commonNotificationConditions(),
			NotificationPreference::isPossibleTargetGainNotification
		);
	}

	@NotNull
	private List<Predicate<Notifiable>> commonNotificationConditions() {
		return List.of(
			Notifiable::isReached,
			Notifiable::isActive,
			notifiable -> notifiable.emptySentHistory(sentManager)
		);
	}

	@Bean
	public MaxLossNotificationPolicy maxLossNotificationPolicy() {
		return AbstractNotificationPolicy.maxLossNotificationPolicy(
			commonNotificationConditions(),
			NotificationPreference::isPossibleMaxLossNotification
		);
	}

	@Bean
	public TargetPriceNotificationPolicy targetPriceNotificationPolicy() {
		return AbstractNotificationPolicy.targetPriceNotificationPolicy(
			commonNotificationConditions(),
			NotificationPreference::isPossibleStockTargetPriceNotification
		);
	}
}
