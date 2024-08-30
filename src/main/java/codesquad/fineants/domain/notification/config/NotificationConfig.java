package codesquad.fineants.domain.notification.config;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import codesquad.fineants.domain.kis.repository.CurrentPriceRedisRepository;
import codesquad.fineants.domain.notification.domain.entity.policy.ConditionEvaluator;
import codesquad.fineants.domain.notification.domain.entity.policy.TargetGainNotificationEvaluator;
import codesquad.fineants.domain.notification.domain.entity.policy.maxloss.MaxLossAccountPreferenceCondition;
import codesquad.fineants.domain.notification.domain.entity.policy.maxloss.MaxLossActiveCondition;
import codesquad.fineants.domain.notification.domain.entity.policy.maxloss.MaxLossCondition;
import codesquad.fineants.domain.notification.domain.entity.policy.maxloss.MaxLossNotificationPolicy;
import codesquad.fineants.domain.notification.domain.entity.policy.maxloss.MaxLossSentHistoryCondition;
import codesquad.fineants.domain.notification.domain.entity.policy.target_gain.TargetGainAccountPreferenceCondition;
import codesquad.fineants.domain.notification.domain.entity.policy.target_gain.TargetGainActiveCondition;
import codesquad.fineants.domain.notification.domain.entity.policy.target_gain.TargetGainCondition;
import codesquad.fineants.domain.notification.domain.entity.policy.target_gain.TargetGainNotificationPolicy;
import codesquad.fineants.domain.notification.domain.entity.policy.target_gain.TargetGainSentHistoryCondition;
import codesquad.fineants.domain.notification.domain.entity.policy.target_price.TargetPriceAccountPreferenceCondition;
import codesquad.fineants.domain.notification.domain.entity.policy.target_price.TargetPriceActiveCondition;
import codesquad.fineants.domain.notification.domain.entity.policy.target_price.TargetPriceCondition;
import codesquad.fineants.domain.notification.domain.entity.policy.target_price.TargetPriceNotificationPolicy;
import codesquad.fineants.domain.notification.domain.entity.policy.target_price.TargetPriceSentHistoryCondition;
import codesquad.fineants.domain.notification.repository.NotificationSentRepository;
import codesquad.fineants.domain.notification.service.disptacher.NotificationDispatcher;
import codesquad.fineants.domain.notification.service.provider.FirebaseNotificationProvider;
import codesquad.fineants.domain.notificationpreference.domain.entity.NotificationPreference;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class NotificationConfig {

	private final NotificationSentRepository sentManager;
	private final CurrentPriceRedisRepository currentPriceRedisRepository;
	private final FirebaseNotificationProvider firebaseNotificationProvider;

	@Bean
	public TargetGainNotificationPolicy targetGainNotificationPolicy() {
		return new TargetGainNotificationPolicy(targetGainNotificationEvaluator());
	}

	@Bean
	public TargetGainNotificationEvaluator targetGainNotificationEvaluator() {
		return new TargetGainNotificationEvaluator(portfolioConditionEvaluator(),
			notificationPreferenceConditionEvaluator());
	}

	@Bean
	public ConditionEvaluator<Portfolio> portfolioConditionEvaluator() {
		return new ConditionEvaluator<>(List.of(
			new TargetGainCondition(),
			new TargetGainActiveCondition(),
			new TargetGainSentHistoryCondition(sentManager)
		));
	}

	@Bean
	public ConditionEvaluator<NotificationPreference> notificationPreferenceConditionEvaluator() {
		return new ConditionEvaluator<>(List.of(new TargetGainAccountPreferenceCondition()));
	}

	@Bean
	public MaxLossNotificationPolicy maxLossNotificationPolicy() {
		return new MaxLossNotificationPolicy(
			List.of(
				new MaxLossCondition(),
				new MaxLossActiveCondition(),
				new MaxLossSentHistoryCondition(sentManager)
			),
			List.of(
				new MaxLossAccountPreferenceCondition()
			)
		);
	}

	@Bean
	public TargetPriceNotificationPolicy targetPriceNotificationPolicy() {
		return new TargetPriceNotificationPolicy(
			List.of(
				new TargetPriceCondition(currentPriceRedisRepository),
				new TargetPriceActiveCondition(),
				new TargetPriceSentHistoryCondition(sentManager)
			),
			List.of(
				new TargetPriceAccountPreferenceCondition()
			)
		);
	}

	@Bean
	public NotificationDispatcher notificationDispatcher() {
		return new NotificationDispatcher(
			Collections.singletonList(firebaseNotificationProvider)
		);
	}
}
