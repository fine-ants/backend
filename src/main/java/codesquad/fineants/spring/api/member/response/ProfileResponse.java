package codesquad.fineants.spring.api.member.response;

import codesquad.fineants.domain.member.Member;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProfileResponse {

	private MemberProfile user;

	public static ProfileResponse from(Member member) {
		MemberProfile user = MemberProfile.builder()
			.id(member.getId())
			.nickname(member.getNickname())
			.email(member.getEmail())
			.profileUrl(member.getProfileUrl())
			.notificationPreferences(
				NotificationPreference.builder()
					.browserNotify(false)
					.targetGainNotify(true)
					.maxLossNotify(true)
					.targetPriceNotify(true)
					.build()
			)
			.build();
		return ProfileResponse.builder()
			.user(user)
			.build();
	}

	@Getter
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@Builder
	public static class MemberProfile {
		private Long id;
		private String nickname;
		private String email;
		private String profileUrl;
		private NotificationPreference notificationPreferences;
	}

	@Getter
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@Builder
	public static class NotificationPreference {
		private Boolean browserNotify;
		private Boolean targetGainNotify;
		private Boolean maxLossNotify;
		private Boolean targetPriceNotify;
	}
}
