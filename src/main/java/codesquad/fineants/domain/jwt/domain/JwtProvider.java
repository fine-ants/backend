package codesquad.fineants.domain.jwt.domain;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Component;

import codesquad.fineants.domain.jwt.properties.JwtProperties;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.global.errors.errorcode.JwtErrorCode;
import codesquad.fineants.global.errors.exception.BadRequestException;
import codesquad.fineants.global.errors.exception.UnAuthorizationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtProvider {

	private final JwtProperties jwtProperties;

	public Jwt createJwtWithRefreshToken(Claims claims, String refreshToken, LocalDateTime now) {
		Date expireDateAccessToken = jwtProperties.createExpireAccessTokenDate(now);
		Date expireDateRefreshToken = getRefreshTokenClaims(refreshToken).getExpiration();
		return createJwt(claims, expireDateAccessToken, expireDateRefreshToken);
	}

	public Jwt createJwtBasedOnMember(Member member, LocalDateTime now) {
		Map<String, Object> claims = member.createClaims();
		Date expireDateAccessToken = jwtProperties.createExpireAccessTokenDate(now);
		Date expireDateRefreshToken = jwtProperties.getExpireDateRefreshToken(now);
		return createJwt(claims, expireDateAccessToken, expireDateRefreshToken);
	}

	private Jwt createJwt(Map<String, Object> claims, Date expireDateAccessToken, Date expireDateRefreshToken) {
		String accessToken = createToken(claims, expireDateAccessToken);
		String refreshToken = createToken(claims, expireDateRefreshToken);
		return new Jwt(accessToken, refreshToken, expireDateAccessToken, expireDateRefreshToken);
	}

	private String createToken(Map<String, Object> claims, Date expireDate) {
		return Jwts.builder()
			.setClaims(claims)
			.setExpiration(expireDate)
			.signWith(jwtProperties.getKey(), SignatureAlgorithm.HS256)
			.compact();
	}

	public Claims getRefreshTokenClaims(String token) {
		// token을 비밀키로 복호화하여 Claims 추출
		try {
			return Jwts.parserBuilder()
				.setSigningKey(jwtProperties.getKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
		} catch (ExpiredJwtException e) {
			throw new UnAuthorizationException(JwtErrorCode.REFRESH_TOKEN_EXPIRE_TOKEN);
		} catch (JwtException e) {
			throw new BadRequestException(JwtErrorCode.INVALID_TOKEN);
		}
	}
}
