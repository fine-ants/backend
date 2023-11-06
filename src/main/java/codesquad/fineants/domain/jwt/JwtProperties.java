package codesquad.fineants.domain.jwt;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Component
public class JwtProperties {

	private final Key key;
	private final Long accessTokenExpirationMillisecond;
	private final Long refreshTokenExpirationMillisecond;

	public JwtProperties(@Value("${jwt.secret-key}") String jwtSecret,
		@Value("${jwt.accesstoken-expiration-milliseconds}") Long accessTokenExpirationMillisecond,
		@Value("${jwt.refreshtoken-expiration-milliseconds}") Long refreshTokenExpirationMillisecond) {
		byte[] secret = jwtSecret.getBytes();
		key = Keys.hmacShaKeyFor(secret);
		this.accessTokenExpirationMillisecond = accessTokenExpirationMillisecond;
		this.refreshTokenExpirationMillisecond = refreshTokenExpirationMillisecond;
	}

	public Date createExpireAccessTokenDate(LocalDateTime now) {
		long accessTokenExpireTime = toEpochMilli(now);
		return new Date(accessTokenExpireTime + accessTokenExpirationMillisecond);
	}

	public Date getExpireDateRefreshToken(LocalDateTime now) {
		long refreshTokenExpireTime = toEpochMilli(now);
		return new Date(refreshTokenExpireTime + refreshTokenExpirationMillisecond);
	}

	private long toEpochMilli(LocalDateTime localDateTime) {
		return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}
}
