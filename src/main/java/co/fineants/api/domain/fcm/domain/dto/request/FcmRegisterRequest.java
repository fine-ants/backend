package co.fineants.api.domain.fcm.domain.dto.request;

import co.fineants.api.domain.fcm.domain.entity.FcmToken;
import co.fineants.api.domain.member.domain.entity.Member;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FcmRegisterRequest {
	@NotBlank(message = "FCM 토큰은 필수 정보입니다")
	private String fcmToken;

	public FcmToken toEntity(Member member) {
		return FcmToken.create(member, fcmToken);
	}
}
