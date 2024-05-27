package codesquad.fineants.domain.member.service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class OauthMemberRedisService {

	private static final String LOGOUT = "logout";

	private final RedisTemplate<String, String> redisTemplate;

	public Optional<String> get(String key) {
		return Optional.ofNullable(redisTemplate.opsForValue().get(key));
	}

	public void banRefreshToken(String token) {
		long expiration = 1000 * 60 * 60 * 24 * 7;
		banToken(token, expiration);
	}

	public void banAccessToken(String token) {
		long expiration = 1000 * 60 * 5;
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
}
