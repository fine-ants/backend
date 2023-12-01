package codesquad.fineants.spring.api.member.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import codesquad.fineants.spring.api.errors.errorcode.OauthErrorCode;
import codesquad.fineants.spring.api.errors.exception.UnAuthorizationException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class OauthMemberRedisService {

	private static final String LOGOUT = "logout";

	private final RedisTemplate<String, String> redisTemplate;

	public String get(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	public void banToken(String token, long expiration) {
		redisTemplate.opsForValue().set(token, LOGOUT, expiration, TimeUnit.MILLISECONDS);
	}

	public void validateAlreadyLogout(String token) {
		String logout = redisTemplate.opsForValue().get(token);
		if (LOGOUT.equals(logout)) {
			throw new UnAuthorizationException(OauthErrorCode.NOT_LOGIN_STATE);
		}
	}
}
