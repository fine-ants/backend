package co.fineants.api.domain.fcm.domain.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FcmDeleteResponse {
	private Long fcmTokenId;

	public static FcmDeleteResponse from(Long fcmTokenId) {
		return FcmDeleteResponse.builder()
			.fcmTokenId(fcmTokenId)
			.build();
	}
}
