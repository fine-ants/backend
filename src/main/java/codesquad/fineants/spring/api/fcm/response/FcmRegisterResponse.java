package codesquad.fineants.spring.api.fcm.response;

import codesquad.fineants.domain.fcm_token.FcmToken;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class FcmRegisterResponse {
	private Long fcmTokenId;

	public static FcmRegisterResponse from(FcmToken fcmToken) {
		return new FcmRegisterResponse(fcmToken.getId());
	}
}
