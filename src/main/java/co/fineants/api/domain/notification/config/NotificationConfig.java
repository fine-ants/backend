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

	// TODO: preferencePredicate를 제외한 conditions 부분은 공통적, 하나의 클래스 타입으로 합칠수 있도록 개선
	@Bean
	public TargetGainNotificationPolicy targetGainNotificationPolicy() {
		List<Predicate<Notifiable>> conditions = List.of(
			Notifiable::isReached,
			Notifiable::isActive,
			notifiable -> notifiable.emptySentHistory(sentManager)
		);
		Predicate<NotificationPreference> preference =
			NotificationPreference::isPossibleTargetGainNotification;
		return new TargetGainNotificationPolicy(conditions, preference);
	}

	@Bean
	public MaxLossNotificationPolicy maxLossNotificationPolicy() {
		List<Predicate<Notifiable>> conditions = List.of(
			Notifiable::isReached,
			Notifiable::isActive,
			notifiable -> notifiable.emptySentHistory(sentManager)
		);
		Predicate<NotificationPreference> preference = NotificationPreference::isPossibleMaxLossNotification;
		return new MaxLossNotificationPolicy(conditions, preference);
	}

	@Bean
	public TargetPriceNotificationPolicy targetPriceNotificationPolicy() {
		List<Predicate<Notifiable>> conditions = List.of(
			Notifiable::isReached,
			Notifiable::isActive,
			notifiable -> notifiable.emptySentHistory(sentManager)
		);
		Predicate<NotificationPreference> preference = NotificationPreference::isPossibleStockTargetPriceNotification;
		return new TargetPriceNotificationPolicy(conditions, preference);
	}
}
