package codesquad.fineants.domain.member.service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class OauthMemberRedisService {

	private static final String LOGOUT = "logout";

	private final RedisTemplate<String, String> redisTemplate;

	public Optional<String> get(String key) {
		return Optional.ofNullable(redisTemplate.opsForValue().get(key));
	}

	public void banRefreshToken(String token) {
		long expiration = 1000L * 60L * 60L * 24L * 7L;
		banToken(token, expiration);
	}

	public void banAccessToken(String token) {
		long expiration = 1000L * 60L * 5L;
		banToken(token, expiration);
	}

	public void banToken(String token, long expiration) {
		redisTemplate.opsForValue().set(token, LOGOUT, expiration, TimeUnit.MILLISECONDS);
	}

	public boolean isAlreadyLogout(String token) {
		String logout = redisTemplate.opsForValue().get(token);
		return LOGOUT.equals(logout);
	}

	public void saveEmailVerifCode(String email, String verifCode) {
		long expirationTimeInMinutes = 5; // 5 minutes
		redisTemplate.opsForValue().set(email, verifCode, expirationTimeInMinutes, TimeUnit.MINUTES);
	}

	public void clear() {
		Set<String> keys = redisTemplate.keys("*");
		if (keys == null) {
			return;
		}
		for (String key : keys) {
			redisTemplate.delete(key);
		}
	}
}
