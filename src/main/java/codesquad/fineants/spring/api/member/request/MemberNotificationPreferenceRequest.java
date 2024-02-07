package codesquad.fineants.spring.api.member.request;

import codesquad.fineants.domain.notification_preference.NotificationPreference;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberNotificationPreferenceRequest {
	private Boolean browserNotify;
	private Boolean targetGainNotify;
	private Boolean maxLossNotify;
	private Boolean targetPriceNotify;

	public NotificationPreference toEntity() {
		return NotificationPreference.builder()
			.browserNotify(browserNotify)
			.targetGainNotify(targetGainNotify)
			.maxLossNotify(maxLossNotify)
			.targetPriceNotify(targetPriceNotify)
			.build();
	}
}
