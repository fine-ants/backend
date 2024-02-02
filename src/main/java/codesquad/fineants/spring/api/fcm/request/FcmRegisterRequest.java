package codesquad.fineants.spring.api.fcm.request;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;

import codesquad.fineants.domain.fcm_token.FcmToken;
import codesquad.fineants.domain.member.Member;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FcmRegisterRequest {
	@NotBlank(message = "FCM 토큰은 필수 정보입니다")
	private String fcmToken;

	public FcmToken toEntity(Member member) {
		return FcmToken.builder()
			.token(fcmToken)
			.member(member)
			.latestActivationTime(LocalDateTime.now())
			.build();
	}
}
