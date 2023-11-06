package codesquad.fineants.domain.oauth.support;

import java.util.Optional;

import codesquad.fineants.domain.member.Member;
import io.jsonwebtoken.Claims;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AuthMember {

	private Long memberId;
	private String email;
	private Long expireDateAccessToken;
	private String accessToken;

	@Builder
	public AuthMember(Long memberId, String email, Long expireDateAccessToken, String accessToken) {
		this.memberId = memberId;
		this.email = email;
		this.expireDateAccessToken = expireDateAccessToken;
		this.accessToken = accessToken;
	}

	public static AuthMember from(Claims claims, String accessToken) {
		AuthMemberBuilder authMember = AuthMember.builder();
		Optional.ofNullable(claims.get("memberId"))
			.ifPresent(memberId -> authMember.memberId(Long.valueOf(memberId.toString())));
		Optional.ofNullable(claims.get("email"))
			.ifPresent(email -> authMember.email((String)email));
		Optional.ofNullable(claims.get("exp"))
			.ifPresent(expireDateAccessToken -> authMember.expireDateAccessToken(
				Long.parseLong(expireDateAccessToken.toString())));
		Optional.ofNullable(accessToken)
			.ifPresent(token -> authMember.accessToken(accessToken));
		return authMember.build();
	}

	public static AuthMember from(Member member) {
		return AuthMember.builder()
			.memberId(member.getId())
			.email(member.getEmail())
			.build();
	}

	@Override
	public String toString() {
		return String.format("%s(id=%d, email=%s)", this.getClass().getSimpleName(), memberId, email);
	}

}
