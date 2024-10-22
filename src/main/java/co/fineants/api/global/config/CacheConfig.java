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
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@EnableCaching
@Configuration
@RequiredArgsConstructor
public class CacheConfig {

	private final ObjectMapper objectMapper;

	@Bean
	public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
		ObjectMapper cacheObjectMapper = new ObjectMapper();
		cacheObjectMapper.setConfig(objectMapper.getSerializationConfig());
		cacheObjectMapper.activateDefaultTyping(
			cacheObjectMapper.getPolymorphicTypeValidator(),
			ObjectMapper.DefaultTyping.EVERYTHING
		);

		RedisSerializationContext.SerializationPair<String> keySerializer = RedisSerializationContext.SerializationPair.fromSerializer(
			new StringRedisSerializer());
		RedisSerializationContext.SerializationPair<Object> serializer = RedisSerializationContext.SerializationPair.fromSerializer(
			new GenericJackson2JsonRedisSerializer(cacheObjectMapper));
		RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(Duration.ofMinutes(10))
			.serializeKeysWith(keySerializer)
			.serializeValuesWith(serializer);

		// 캐시별 만료 시간 설정
		Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
		cacheConfigurations.put("tickerSymbols", defaultCacheConfig.entryTtl(Duration.ofMinutes(5))); // 5 minute TTL
		cacheConfigurations.put("lineChartCache", defaultCacheConfig.entryTtl(Duration.ofHours(24))); // 24 hourTTL

		return RedisCacheManager.builder(redisConnectionFactory)
			.cacheDefaults(defaultCacheConfig)
			.withInitialCacheConfigurations(cacheConfigurations) // 캐시별 설정 추가
			.build();
	}
}
