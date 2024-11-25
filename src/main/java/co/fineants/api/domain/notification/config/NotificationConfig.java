package co.fineants.api.domain.notification.config;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.notification.domain.entity.policy.TargetGainNotificationEvaluator;
import co.fineants.api.domain.notification.domain.entity.policy.maxloss.MaxLossNotificationPolicy;
import co.fineants.api.domain.notification.domain.entity.policy.target_gain.TargetGainNotificationPolicy;
import co.fineants.api.domain.notification.domain.entity.policy.target_price.TargetPriceNotificationPolicy;
import co.fineants.api.domain.notification.repository.NotificationSentRepository;
import co.fineants.api.domain.notification.service.disptacher.NotificationDispatcher;
import co.fineants.api.domain.notification.service.provider.FirebaseNotificationProvider;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.stock_target_price.domain.entity.TargetPriceNotification;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class NotificationConfig {

	private final NotificationSentRepository sentManager;
	private final CurrentPriceRedisRepository currentPriceRedisRepository;
	private final FirebaseNotificationProvider firebaseNotificationProvider;
	private final PortfolioCalculator calculator;

	@Bean
	public TargetGainNotificationPolicy targetGainNotificationPolicy() {
		return new TargetGainNotificationPolicy(targetGainNotificationEvaluator());
	}

	@Bean
	public TargetGainNotificationEvaluator targetGainNotificationEvaluator() {
		List<Predicate<Portfolio>> portfolioConditions = List.of(
			calculator::reachedTargetGainBy,
			Portfolio::targetGainIsActive,
			portfolio -> !sentManager.hasTargetGainSentHistoryBy(portfolio)
		);
		return new TargetGainNotificationEvaluator(
			portfolioConditions,
			NotificationPreference::isPossibleTargetGainNotification);
	}

	@Bean
	public MaxLossNotificationPolicy maxLossNotificationPolicy() {
		List<Predicate<Portfolio>> conditions = List.of(
			calculator::reachedMaximumLossBy,
			Portfolio::maximumLossIsActive,
			portfolio -> !sentManager.hasMaxLossSentHistoryBy(portfolio)
		);
		return new MaxLossNotificationPolicy(conditions, NotificationPreference::isPossibleMaxLossNotification);
	}

	@Bean
	public TargetPriceNotificationPolicy targetPriceNotificationPolicy() {
		return new TargetPriceNotificationPolicy(
			List.of(
				targetPriceNotification -> targetPriceNotification.isSameTargetPrice(currentPriceRedisRepository),
				TargetPriceNotification::isActive,
				targetPriceNotification -> !sentManager.hasTargetPriceSendHistory(targetPriceNotification)
			),
			List.of(
				NotificationPreference::isPossibleStockTargetPriceNotification
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
