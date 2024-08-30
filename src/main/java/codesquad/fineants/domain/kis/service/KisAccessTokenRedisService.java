package codesquad.fineants.domain.kis.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import codesquad.fineants.domain.kis.client.KisAccessToken;
import codesquad.fineants.global.util.ObjectMapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class KisAccessTokenRedisService {
	private static final String ACCESS_TOKEN_MAP_KEY = "kis:accessTokenMap";
	public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final RedisTemplate<String, Object> redisTemplate;

	public Optional<KisAccessToken> getAccessTokenMap() {
		Object result = redisTemplate.opsForValue().get(ACCESS_TOKEN_MAP_KEY);
		if (result == null) {
			return Optional.empty();
		}
		return Optional.of(ObjectMapperUtil.deserialize((String)result, KisAccessToken.class));
	}

	public void setAccessTokenMap(KisAccessToken accessToken, LocalDateTime now) {
		try {
			redisTemplate.opsForValue().set(ACCESS_TOKEN_MAP_KEY,
				ObjectMapperUtil.serialize(accessToken),
				accessToken.betweenSecondFrom(now));
		} catch (RedisSystemException e) {
			log.error(e.getMessage(), e);
		}
	}

	public void deleteAccessTokenMap() {
		redisTemplate.delete(ACCESS_TOKEN_MAP_KEY);
	}
}
