package codesquad.fineants.global.security.auth.service;

import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import codesquad.fineants.global.security.auth.dto.MemberAuthentication;
import codesquad.fineants.global.security.auth.dto.Token;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
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

	public Token generateToken(MemberAuthentication authentication, String role) {
		Claims claims = Jwts.claims().setSubject(authentication.getEmail());
		claims.put("id", authentication.getId());
		claims.put("nickname", authentication.getNickname());
		claims.put("provider", authentication.getProvider());
		claims.put("profileUrl", authentication.getProfileUrl());
		claims.put("role", role);

		Date now = new Date();
		String accessToken = Jwts.builder()
			.setClaims(claims)
			.setIssuedAt(now)
			.setExpiration(new Date(now.getTime() + tokenPeriod))
			.signWith(SignatureAlgorithm.HS256, secretKey)
			.compact();
		String refreshToken = Jwts.builder()
			.setClaims(claims)
			.setIssuedAt(now)
			.setExpiration(new Date(now.getTime() + refreshPeriod))
			.signWith(SignatureAlgorithm.HS256, secretKey)
			.compact();
		return Token.create(accessToken, refreshToken);
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
		return MemberAuthentication.create(id, email, nickname, provider, profileUrl);
	}
}
