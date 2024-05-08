package codesquad.fineants.domain.notification.repository;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class NotificationSentRepository {
	private static final String TARGET_GAIN_FORMAT = "targetGainSent:%d";
	private static final String MAX_LOSS_FORMAT = "maxLossSent:%d";
	private static final String TARGET_PRICE_FORMAT = "targetPriceSent:%d";
	private final RedisTemplate<String, String> redisTemplate;
	private final Duration TIMEOUT = Duration.ofHours(24L);

	public void addTargetGainSendHistory(Long portfolioId) {
		redisTemplate.opsForValue()
			.set(String.format(TARGET_GAIN_FORMAT, portfolioId), "true", TIMEOUT);
	}

	public void addMaxLossSendHistory(Long portfolioId) {
		redisTemplate.opsForValue()
			.set(String.format(MAX_LOSS_FORMAT, portfolioId), "true", TIMEOUT);
	}

	public void addTargetPriceSendHistory(Long targetPriceNotificationId) {
		redisTemplate.opsForValue()
			.set(String.format(TARGET_PRICE_FORMAT, targetPriceNotificationId), "true", TIMEOUT);
	}

	public boolean hasTargetGainSendHistory(Long portfolioId) {
		String result = redisTemplate.opsForValue().get(String.format(TARGET_GAIN_FORMAT, portfolioId));
		return result != null;
	}

	public boolean hasMaxLossSendHistory(Long portfolioId) {
		String result = redisTemplate.opsForValue().get(String.format(MAX_LOSS_FORMAT, portfolioId));
		return result != null;
	}

	public boolean hasTargetPriceSendHistory(Long targetPriceNotificationId) {
		String result = redisTemplate.opsForValue().get(String.format(TARGET_PRICE_FORMAT, targetPriceNotificationId));
		return result != null;
	}
}
