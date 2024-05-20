package codesquad.fineants.global.security.auth.service;

import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import codesquad.fineants.global.security.auth.dto.Token;
import codesquad.fineants.global.security.auth.dto.UserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class TokenService {
	private final String secretKey;

	public TokenService(@Value("${jwt.secret-key}") String secretKey) {
		this.secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
	}

	public Token generateToken(String uid, String role) {
		long tokenPeriod = 1000L * 60L * 5L; // 5 minute
		long refreshPeriod = 1000L * 60L * 60L * 24L * 30L; // 30 days

		Claims claims = Jwts.claims().setSubject(uid);
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
		Jws<Claims> claims = Jwts.parserBuilder()
			.setSigningKey(secretKey)
			.build()
			.parseClaimsJws(token);
		try {
			return claims.getBody()
				.getExpiration()
				.after(new Date());
		} catch (Exception e) {
			return false;
		}
	}

	public UserDto parseUserDtoFrom(String token) {
		Claims body = Jwts.parserBuilder()
			.setSigningKey(secretKey.getBytes())
			.build()
			.parseClaimsJws(token)
			.getBody();
		String email = body.getSubject();
		String nickname = body.get("nickname", String.class);
		String provider = body.get("provider", String.class);
		String profileUrl = body.get("profileUrl", String.class);
		return UserDto.create(email, nickname, provider, profileUrl);
	}
}
