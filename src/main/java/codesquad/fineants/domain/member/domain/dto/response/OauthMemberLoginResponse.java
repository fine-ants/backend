package codesquad.fineants.domain.member.domain.dto.response;

import codesquad.fineants.domain.jwt.domain.Jwt;
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

	private Jwt jwt;

	public static OauthMemberLoginResponse of(Jwt jwt) {
		return new OauthMemberLoginResponse(jwt);
	}

	@Override
	public String toString() {
		return String.format("%s, %s", "소셜 로그인 응답", this.getClass().getSimpleName());
	}
}
