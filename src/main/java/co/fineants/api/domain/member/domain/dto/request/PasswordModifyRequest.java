package co.fineants.api.domain.member.domain.dto.request;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModifyPasswordRequest {
	@JsonProperty
	@NotNull(value = "필수 정보입니다")
	private final String currentPassword;
	@JsonProperty
	@NotNull(value = "필수 정보입니다")
	private final String newPassword;
	@JsonProperty
	@NotNull(value = "필수 정보입니다")
	private final String newPasswordConfirm;

	@JsonCreator
	public ModifyPasswordRequest(
		@JsonProperty("currentPassword") @NotNull String currentPassword,
		@JsonProperty("newPassword") @NotNull String newPassword,
		@JsonProperty("newPasswordConfirm") @NotNull String newPasswordConfirm) {
		this.currentPassword = currentPassword;
		this.newPassword = newPassword;
		this.newPasswordConfirm = newPasswordConfirm;
	}

	public String currentPassword() {
		return currentPassword;
	}

	public String newPassword() {
		return newPassword;
	}

	public boolean matchPassword() {
		return newPassword.equals(newPasswordConfirm);
	}
}
