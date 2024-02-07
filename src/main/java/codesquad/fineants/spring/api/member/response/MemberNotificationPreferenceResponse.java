package codesquad.fineants.spring.api.member.response;

import codesquad.fineants.domain.notification_preference.NotificationPreference;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class MemberNotificationPreferenceResponse {
	private Boolean browserNotify;
	private Boolean targetGainNotify;
	private Boolean maxLossNotify;
	private Boolean targetPriceNotify;

	public static MemberNotificationPreferenceResponse from(NotificationPreference notificationPreference) {
		return MemberNotificationPreferenceResponse.builder()
			.browserNotify(notificationPreference.isBrowserNotify())
			.targetGainNotify(notificationPreference.isTargetGainNotify())
			.maxLossNotify(notificationPreference.isMaxLossNotify())
			.targetPriceNotify(notificationPreference.isTargetPriceNotify())
			.build();
	}
}
