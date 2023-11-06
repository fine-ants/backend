package codesquad.fineants.spring.api.kis;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class KisRedisService {

	private static final String APPROVAL_KEY = "kis:approvalKey";
	private static final String ACCESS_TOKEN_MAP_KEY = "kis:accessTokenMap";

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;

	public String getApproveKey() {
		if (Boolean.FALSE.equals(redisTemplate.hasKey(APPROVAL_KEY))) {
			return null;
		}
		Object result = redisTemplate.opsForValue().get(APPROVAL_KEY);
		if (result == null) {
			return null;
		}
		return (String)result;
	}

	public void setApproveKey(String approveKey) {
		redisTemplate.opsForValue().set(APPROVAL_KEY, approveKey, 1L, TimeUnit.DAYS);
	}

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

	public void setAccessTokenMap(Map<String, Object> accessTokenMap) {
		long expiresIn = ((Integer)accessTokenMap.get("expires_in")).longValue();
		String json;
		try {
			json = objectMapper.writeValueAsString(accessTokenMap);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		redisTemplate.opsForValue().set(ACCESS_TOKEN_MAP_KEY, json, Duration.ofSeconds(expiresIn));
	}

	public boolean hasCurrentPrice(String tickerSymbol) {
		Object result = redisTemplate.opsForValue().get(tickerSymbol);
		return result != null;
	}

	public void setCurrentPrice(String tickerSymbol, long currentPrice) {
		redisTemplate.opsForValue().set(tickerSymbol, String.valueOf(currentPrice), Duration.ofMinutes(1L));
	}
}
