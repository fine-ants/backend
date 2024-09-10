package co.fineants.api.global.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@EnableCaching
@Configuration
public class CacheConfig {
	@Bean
	public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
		// 기본 캐시 설정 (10분 TTL)
		RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(Duration.ofMinutes(10))
			.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));

		// 캐시별 만료 시간 설정
		Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
		cacheConfigurations.put("tickerSymbols",
			RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5))); // 5분 TTL

		return RedisCacheManager.builder(redisConnectionFactory)
			.cacheDefaults(defaultCacheConfig)
			.withInitialCacheConfigurations(cacheConfigurations) // 캐시별 설정 추가
			.build();
	}
}
