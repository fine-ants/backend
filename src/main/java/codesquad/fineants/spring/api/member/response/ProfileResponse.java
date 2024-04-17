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

	public static ProfileResponse from(Member member, NotificationPreference preference) {
		MemberProfile user = MemberProfile.builder()
			.id(member.getId())
			.nickname(member.getNickname())
			.email(member.getEmail())
			.profileUrl(member.getProfileUrl())
			.provider(member.getProvider())
			.notificationPreferences(preference)
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
		private String provider;
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

		public static NotificationPreference from(
			codesquad.fineants.domain.notification_preference.NotificationPreference preference) {
			return NotificationPreference.builder()
				.browserNotify(preference.isBrowserNotify())
				.targetGainNotify(preference.isTargetGainNotify())
				.maxLossNotify(preference.isMaxLossNotify())
				.targetPriceNotify(preference.isTargetPriceNotify())
				.build();
		}
	}
}
