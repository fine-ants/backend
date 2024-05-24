package codesquad.fineants.domain.member.domain.dto.response;

import codesquad.fineants.global.security.oauth.dto.Token;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OauthMemberLoginResponse {

	private Token jwt;

	public static OauthMemberLoginResponse of(Token token) {
		return new OauthMemberLoginResponse(token);
	}

	@Override
	public String toString() {
		return String.format("%s, %s", "소셜 로그인 응답", this.getClass().getSimpleName());
	}
}
