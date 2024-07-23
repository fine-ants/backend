package codesquad.fineants.domain.member.domain.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ProfileChangeRequest {
	@Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,10}$", message = "잘못된 입력형식입니다.")
	private String nickname;
}
