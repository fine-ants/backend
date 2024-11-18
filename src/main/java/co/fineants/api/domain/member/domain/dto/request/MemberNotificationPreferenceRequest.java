package co.fineants.api.domain.member.domain.dto.request;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class MemberNotificationPreferenceRequest {
	@NotNull(message = "필수 정보입니다")
	@JsonProperty
	private final Boolean browserNotify;
	@NotNull(message = "필수 정보입니다")
	@JsonProperty
	private final Boolean targetGainNotify;
	@NotNull(message = "필수 정보입니다")
	@JsonProperty
	private final Boolean maxLossNotify;
	@NotNull(message = "필수 정보입니다")
	@JsonProperty
	private final Boolean targetPriceNotify;
	@Nullable
	@JsonProperty
	private final Long fcmTokenId;

	@JsonCreator
	@Builder
	private MemberNotificationPreferenceRequest(
		@JsonProperty("browserNotify") Boolean browserNotify,
		@JsonProperty("targetGainNotify") Boolean targetGainNotify,
		@JsonProperty("maxLossNotify") Boolean maxLossNotify,
		@JsonProperty("targetPriceNotify") Boolean targetPriceNotify,
		@Nullable @JsonProperty("fcmTokenId") Long fcmTokenId) {
		this.browserNotify = browserNotify;
		this.targetGainNotify = targetGainNotify;
		this.maxLossNotify = maxLossNotify;
		this.targetPriceNotify = targetPriceNotify;
		this.fcmTokenId = fcmTokenId;
	}

	public NotificationPreference toEntity() {
		return NotificationPreference.create(browserNotify, targetGainNotify, maxLossNotify, targetPriceNotify);
	}

	public boolean hasFcmTokenId() {
		return fcmTokenId != null;
	}

	public Long fcmTokenId() {
		return fcmTokenId;
	}
}
