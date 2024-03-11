package codesquad.fineants.spring.api.stock_target_price.manager;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class TargetPriceNotificationSentManager {
	private static final String format = "targetPriceNotificationSent:%d";
	private final RedisTemplate<String, String> redisTemplate;
	private final Duration TIMEOUT = Duration.ofHours(24L);

	public void addTargetPriceNotification(Long targetPriceNotificationId) {
		redisTemplate.opsForValue()
			.set(String.format(format, targetPriceNotificationId), "true", TIMEOUT);
	}

	public boolean hasTargetPriceNotificationSent(Long targetPriceNotificationId) {
		String result = redisTemplate.opsForValue().get(String.format(format, targetPriceNotificationId));
		return result != null;
	}
}
