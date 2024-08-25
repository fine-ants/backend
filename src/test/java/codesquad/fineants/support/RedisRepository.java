package codesquad.fineants.support;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RedisRepository {
	private final RedisTemplate<String, String> redisTemplate;

	public void clearAll() {
		// Fetch all keys from Redis
		Set<String> keys = redisTemplate.keys("*");

		if (keys != null && !keys.isEmpty()) {
			// Delete all keys
			redisTemplate.delete(keys);
		}
	}
}
