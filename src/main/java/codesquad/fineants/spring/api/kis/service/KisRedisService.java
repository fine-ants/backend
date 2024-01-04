package codesquad.fineants.spring.api.kis.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class KisRedisService {
	private static final String ACCESS_TOKEN_MAP_KEY = "kis:accessTokenMap";
	public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;

	public Optional<Map<String, Object>> getAccessTokenMap() {
		if (Boolean.FALSE.equals(redisTemplate.hasKey(ACCESS_TOKEN_MAP_KEY))) {
			return Optional.empty();
		}

		Object result = redisTemplate.opsForValue().get(ACCESS_TOKEN_MAP_KEY);
		if (result == null) {
			return Optional.empty();
		}
		try {
			Map<String, Object> accessTokenMap = objectMapper.readValue((String)result, new TypeReference<>() {
			});
			return Optional.ofNullable(accessTokenMap);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public void setAccessTokenMap(Map<String, Object> accessTokenMap, LocalDateTime now) {
		String json;
		try {
			json = objectMapper.writeValueAsString(accessTokenMap);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		String access_token_token_expired = (String)accessTokenMap.get("access_token_token_expired");
		long exp = Duration.between(now,
				LocalDateTime.parse(access_token_token_expired, formatter))
			.toSeconds();
		try {
			redisTemplate.opsForValue().set(ACCESS_TOKEN_MAP_KEY, json, Duration.ofSeconds(exp));
		} catch (RedisSystemException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage());
		}

	}

	public void deleteAccessTokenMap() {
		redisTemplate.delete(ACCESS_TOKEN_MAP_KEY);
	}
}
