package codesquad.fineants.spring.api.member.request;

import javax.validation.constraints.NotBlank;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OauthMemberRefreshRequest {

	@NotBlank(message = "리프레쉬 토큰은 필수 정보입니다.")
	private String refreshToken;

	@Override
	public String toString() {
		return String.format("%s, %s(refreshToken=%s)", "액세스 토큰 갱신 요청", this.getClass().getSimpleName(), refreshToken);
	}
}
