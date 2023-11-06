package codesquad.fineants.spring.api.member.service;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.logging.log4j.util.Strings;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import codesquad.fineants.domain.jwt.Jwt;
import codesquad.fineants.spring.api.errors.errorcode.JwtErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.OauthErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.errors.exception.UnAuthorizationException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class OauthMemberRedisService {

	private static final String LOGOUT = "logout";
	private static final String REFRESH_TOKEN_PREFIX = "RT:";
	private static final Pattern REFRESH_TOKEN_PATTERN = Pattern.compile("RT:*");

	private final RedisTemplate<String, String> redisTemplate;

	public String get(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	public String findEmailBy(String refreshToken) {
		Set<String> keys = redisTemplate.keys(REFRESH_TOKEN_PATTERN.pattern());
		if (keys == null) {
			throw new UnAuthorizationException(JwtErrorCode.EMPTY_TOKEN);
		}
		return keys.stream()
			.filter(key -> Objects.equals(redisTemplate.opsForValue().get(key), refreshToken))
			.findAny()
			.map(email -> email.replace(REFRESH_TOKEN_PREFIX, Strings.EMPTY))
			.orElseThrow(() -> new BadRequestException(JwtErrorCode.INVALID_TOKEN));
	}

	public void saveRefreshToken(String key, Jwt jwt) {
		redisTemplate.opsForValue().set(key,
			jwt.getRefreshToken(),
			jwt.convertExpireDateRefreshTokenTimeWithLong(),
			TimeUnit.MILLISECONDS);
	}

	public boolean deleteRefreshToken(String key) {
		if (key == null) {
			return false;
		}
		return Boolean.TRUE.equals(redisTemplate.delete(key));
	}

	public void banAccessToken(String accessToken, long expiration) {
		redisTemplate.opsForValue().set(accessToken, LOGOUT, expiration, TimeUnit.MILLISECONDS);
	}

	public void validateAlreadyLogout(String token) {
		String logout = redisTemplate.opsForValue().get(token);
		if (LOGOUT.equals(logout)) {
			throw new UnAuthorizationException(OauthErrorCode.NOT_LOGIN_STATE);
		}
	}
}
