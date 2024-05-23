package codesquad.fineants.global.security.auth.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import codesquad.fineants.global.errors.errorcode.JwtErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;
import codesquad.fineants.global.security.auth.dto.MemberAuthentication;
import codesquad.fineants.global.security.auth.dto.Token;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TokenService {
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

	public Token generateToken(MemberAuthentication authentication) {
		Claims claims = generateClaims(authentication);

		Date now = new Date();
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
			return Token.create(accessToken, refreshToken);
		}
		throw new FineAntsException(JwtErrorCode.INVALID_TOKEN);
	}
}
