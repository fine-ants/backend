package codesquad.fineants.spring.api.notification.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import codesquad.fineants.domain.notification.policy.max_loss.MaxLossAccountPreferenceCondition;
import codesquad.fineants.domain.notification.policy.max_loss.MaxLossActiveCondition;
import codesquad.fineants.domain.notification.policy.max_loss.MaxLossCondition;
import codesquad.fineants.domain.notification.policy.max_loss.MaxLossNotificationPolicy;
import codesquad.fineants.domain.notification.policy.max_loss.MaxLossSentHistoryCondition;
import codesquad.fineants.domain.notification.policy.target_gain.TargetGainAccountPreferenceCondition;
import codesquad.fineants.domain.notification.policy.target_gain.TargetGainActiveCondition;
import codesquad.fineants.domain.notification.policy.target_gain.TargetGainCondition;
import codesquad.fineants.domain.notification.policy.target_gain.TargetGainNotificationPolicy;
import codesquad.fineants.domain.notification.policy.target_gain.TargetGainSentHistoryCondition;
import codesquad.fineants.domain.notification.policy.target_price.TargetPriceAccountPreferenceCondition;
import codesquad.fineants.domain.notification.policy.target_price.TargetPriceActiveCondition;
import codesquad.fineants.domain.notification.policy.target_price.TargetPriceCondition;
import codesquad.fineants.domain.notification.policy.target_price.TargetPriceNotificationPolicy;
import codesquad.fineants.domain.notification.policy.target_price.TargetPriceSentHistoryCondition;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.notification.manager.NotificationSentManager;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class NotificationConfig {

	private final NotificationSentManager sentManager;
	private final CurrentPriceManager currentPriceManager;

	@Bean
	public TargetGainNotificationPolicy targetGainNotificationPolicy() {
		return new TargetGainNotificationPolicy(
			List.of(
				new TargetGainCondition(),
				new TargetGainActiveCondition(),
				new TargetGainSentHistoryCondition(sentManager)
			),
			List.of(
				new TargetGainAccountPreferenceCondition()
			)
		);
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
				new TargetPriceCondition(currentPriceManager),
				new TargetPriceActiveCondition(),
				new TargetPriceSentHistoryCondition(sentManager)
			),
			List.of(
				new TargetPriceAccountPreferenceCondition()
			)
		);
	}
}
