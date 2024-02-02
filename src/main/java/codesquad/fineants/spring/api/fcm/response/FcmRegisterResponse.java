package codesquad.fineants.spring.api.fcm.response;

import codesquad.fineants.domain.fcm_token.FcmToken;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FcmRegisterResponse {
	private Long fcmTokenId;

	public static FcmRegisterResponse from(FcmToken fcmTOkenn) {
		return new FcmRegisterResponse(fcmTOkenn.getId());
	}
}
