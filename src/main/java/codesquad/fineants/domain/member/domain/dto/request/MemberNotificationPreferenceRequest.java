package codesquad.fineants.domain.member.domain.dto.request;

import codesquad.fineants.domain.notification_preference.domain.entity.NotificationPreference;
import jakarta.validation.constraints.NotNull;
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
	private Long fcmTokenId;

	public NotificationPreference toEntity() {
		return NotificationPreference.builder()
			.browserNotify(browserNotify)
			.targetGainNotify(targetGainNotify)
			.maxLossNotify(maxLossNotify)
			.targetPriceNotify(targetPriceNotify)
			.build();
	}
}
