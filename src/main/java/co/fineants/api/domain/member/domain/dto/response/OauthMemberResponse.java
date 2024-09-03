package co.fineants.api.domain.member.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import co.fineants.api.domain.member.domain.entity.Member;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"id", "nickname", "email", "profileUrl"})
public class OauthMemberResponse {
	private Long id;
	private String nickname;
	private String email;
	private String profileUrl;
	private String provider;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private MemberNotificationPreferenceResponse notificationPreferences;

	public static OauthMemberResponse from(Member member) {
		return from(member, null);
	}

	public static OauthMemberResponse from(Member member, MemberNotificationPreferenceResponse response) {
		return new OauthMemberResponse(
			member.getId(),
			member.getNickname(),
			member.getEmail(),
			member.getProfileUrl(),
			member.getProvider(),
			response);
	}

	@Override
	public String toString() {
		return String.format("%s, %s(id=%d, nickname=%s, email=%s, profileUrl=%s)", "로그인 회원정보 응답",
			this.getClass().getSimpleName(), id, nickname, email, profileUrl);
	}
}
