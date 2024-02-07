package codesquad.fineants.spring.api.member.request;

import javax.validation.constraints.NotNull;

import codesquad.fineants.domain.notification_preference.NotificationPreference;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MemberNotificationPreferenceRequest {
	@NotNull(message = "필수 정보입니다")
	private Boolean browserNotify;
	@NotNull(message = "필수 정보입니다")
	private Boolean targetGainNotify;
	@NotNull(message = "필수 정보입니다")
	private Boolean maxLossNotify;
	@NotNull(message = "필수 정보입니다")
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
