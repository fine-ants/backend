package codesquad.fineants.global.security.oauth.service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import codesquad.fineants.global.errors.errorcode.JwtErrorCode;
import codesquad.fineants.global.security.oauth.dto.MemberAuthentication;
import codesquad.fineants.global.security.oauth.dto.Token;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TokenService {

	private static final Duration EXPIRY_IMMINENT_TIME = Duration.ofHours(1);
	private final String secretKey;
	private final Long tokenPeriod;
	private final Long refreshPeriod;

	public TokenService(
		@Value("${jwt.secret-key}") String secretKey,
		@Value("${jwt.accesstoken-expiration-milliseconds}") Long tokenPeriod,
		@Value("${jwt.refreshtoken-expiration-milliseconds}") Long refreshPeriod
	) {
		this.secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
		this.tokenPeriod = tokenPeriod;
		this.refreshPeriod = refreshPeriod;
	}

	public Token generateToken(MemberAuthentication authentication, Date now) {
		Claims claims = generateClaims(authentication);
		String accessToken = generateAccessToken(claims, now);
		String refreshToken = generateRefreshToken(claims, now);
		return Token.create(accessToken, refreshToken);
	}

	private Claims generateClaims(MemberAuthentication authentication) {
		Claims claims = Jwts.claims().setSubject(authentication.getEmail());
		claims.put("id", authentication.getId());
		claims.put("nickname", authentication.getNickname());
		claims.put("provider", authentication.getProvider());
		claims.put("profileUrl", authentication.getProfileUrl());
		claims.put("roleSet", String.join(" ", authentication.getRoleSet()));
		return claims;
	}

	private String generateAccessToken(Claims claims, Date now) {
		return Jwts.builder()
			.setClaims(claims)
			.setIssuedAt(now)
			.setExpiration(new Date(now.getTime() + tokenPeriod))
			.signWith(SignatureAlgorithm.HS256, secretKey)
			.compact();
	}

	private String generateRefreshToken(Claims claims, Date now) {
		return Jwts.builder()
			.setClaims(claims)
			.setIssuedAt(now)
			.setExpiration(new Date(now.getTime() + refreshPeriod))
			.signWith(SignatureAlgorithm.HS256, secretKey)
			.compact();
	}

	public boolean verifyToken(String token) {
		try {
			Jws<Claims> claims = Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token);
			return claims.getBody()
				.getExpiration()
				.after(new Date());
		} catch (Exception e) {
			return false;
		}
	}

	public MemberAuthentication parseMemberAuthenticationToken(String token) {
		Claims body = Jwts.parserBuilder()
			.setSigningKey(secretKey)
			.build()
			.parseClaimsJws(token)
			.getBody();
		Long id = body.get("id", Long.class);
		String email = body.getSubject();
		String nickname = body.get("nickname", String.class);
		String provider = body.get("provider", String.class);
		String profileUrl = body.get("profileUrl", String.class);
		Set<String> roleSet = Arrays.stream(body.get("roleSet", String.class).split(" "))
			.collect(Collectors.toSet());
		return MemberAuthentication.create(id, email, nickname, provider, profileUrl, roleSet);
	}

	public Token refreshToken(String refreshToken, LocalDateTime now) {
		if (verifyToken(refreshToken)) {
			MemberAuthentication memberAuthentication = parseMemberAuthenticationToken(refreshToken);
			Claims claims = generateClaims(memberAuthentication);
			String accessToken = generateAccessToken(claims, Timestamp.valueOf(now));
			String newRefreshToken = refreshToken;
			if (isRefreshTokenNearExpiry(refreshToken)) {
				newRefreshToken = generateRefreshToken(claims, Timestamp.valueOf(now));
			}
			return Token.create(accessToken, newRefreshToken);
		}
		throw new BadCredentialsException(JwtErrorCode.INVALID_TOKEN.getMessage());
	}

	/**
	 * 리프레시 토큰의 만료 임박 시간 체크
	 * @param token 리프레시 토큰
	 * @return 만료 임박 시간 체크 결과
	 */
	public boolean isRefreshTokenNearExpiry(String token) {
		Date expiration;
		try {
			Jws<Claims> claims = Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token);
			expiration = claims.getBody()
				.getExpiration();
		} catch (Exception e) {
			return false;
		}

		// 현재 시간
		Instant now = Instant.now();

		// 만료 시간
		Instant expirationInstant = expiration.toInstant();

		// 만료 까지의 시간 간격
		Duration duration = Duration.between(now, expirationInstant);

		// 만료 까지의 시간 간격이 EXPIRY_IMMINENT_TIME 미만 인지 확인
		return duration.compareTo(EXPIRY_IMMINENT_TIME) < 0;
	}

	/**
	 * 토큰 만료 여부 확인
	 * @param token JWT 토큰
	 * @return 토큰 만료 여부
	 */
	public boolean isExpiredToken(String token) {
		try {
			Jws<Claims> claims = Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token);
			return claims.getBody()
				.getExpiration()
				.before(new Date());
		} catch (ExpiredJwtException e) {
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
