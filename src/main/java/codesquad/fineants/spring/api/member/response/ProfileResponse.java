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

	private Long id;
	private String nickname;

	private String profileUrl;

	private NotificationPreference notificationPreferences;

	public static ProfileResponse from(Member member) {
		return ProfileResponse.builder()
			.id(member.getId())
			.nickname(member.getNickname())
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
	}

	@Getter
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@Builder
	static class NotificationPreference {
		private Boolean browserNotify;
		private Boolean targetGainNotify;
		private Boolean maxLossNotify;
		private Boolean targetPriceNotify;
	}
}
