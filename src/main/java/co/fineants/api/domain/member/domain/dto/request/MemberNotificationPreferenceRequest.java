package co.fineants.api.domain.member.domain.dto.request;

import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
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
		return NotificationPreference.create(browserNotify, targetGainNotify, maxLossNotify, targetPriceNotify);
	}
}
