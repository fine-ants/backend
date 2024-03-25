package codesquad.fineants.spring.api.member.service.response;

import codesquad.fineants.domain.member.Member;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class SignUpServiceResponse {
	private Long id;
	private String nickname;
	private String email;
	private String profileUrl;
	private String provider;

	public static SignUpServiceResponse from(Member member) {
		return SignUpServiceResponse.builder()
			.id(member.getId())
			.nickname(member.getNickname())
			.email(member.getEmail())
			.profileUrl(member.getProfileUrl())
			.provider(member.getProvider())
			.build();
	}
}
