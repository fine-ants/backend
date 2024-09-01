package co.fineants.api.domain.member.domain.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ModifyPasswordRequest {
	private String currentPassword;
	private String newPassword;
	private String newPasswordConfirm;
}
