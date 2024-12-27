package co.fineants.api.global.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@EnableCaching
@Configuration
@RequiredArgsConstructor
public class CacheConfig {

	@Bean
	public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory,
		@Qualifier("cacheObjectMapper") ObjectMapper objectMapper) {
		RedisSerializationContext.SerializationPair<String> keySerializer = RedisSerializationContext.SerializationPair.fromSerializer(
			new StringRedisSerializer());
		RedisSerializationContext.SerializationPair<Object> serializer = RedisSerializationContext.SerializationPair.fromSerializer(
			new GenericJackson2JsonRedisSerializer(objectMapper));
		RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(Duration.ofMinutes(10))
			.serializeKeysWith(keySerializer)
			.serializeValuesWith(serializer);

		// 캐시별 만료 시간 설정
		Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
		cacheConfigurations.put("tickerSymbols", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));
		cacheConfigurations.put("lineChartCache", defaultCacheConfig.entryTtl(Duration.ofHours(24)));
		cacheConfigurations.put("myAllPortfolioNames", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));
		cacheConfigurations.put("holidayCache", defaultCacheConfig.entryTtl(Duration.ofDays(1)));

		return RedisCacheManager.builder(redisConnectionFactory)
			.cacheDefaults(defaultCacheConfig)
			.withInitialCacheConfigurations(cacheConfigurations) // 캐시별 설정 추가
			.build();
	}
}
