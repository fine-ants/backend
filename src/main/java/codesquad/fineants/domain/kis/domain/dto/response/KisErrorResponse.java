package codesquad.fineants.domain.kis.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import codesquad.fineants.global.errors.exception.kis.CredentialsTypeKisException;
import codesquad.fineants.global.errors.exception.kis.ExpiredAccessTokenKisException;
import codesquad.fineants.global.errors.exception.kis.KisException;
import codesquad.fineants.global.errors.exception.kis.RequestLimitExceededKisException;
import codesquad.fineants.global.errors.exception.kis.TokenIssuanceRetryLaterKisException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class KisErrorResponse {
	@JsonProperty("rt_cd")
	private String returnCode;
	@JsonProperty("msg_cd")
	private String messageCode;
	@JsonProperty("msg1")
	private String message;

	public KisException toException() {
		return switch (messageCode) {
			case "EGW00201" -> new RequestLimitExceededKisException(returnCode, messageCode, message);
			case "EGW00133" -> new TokenIssuanceRetryLaterKisException(returnCode, messageCode, message);
			case "EGW00123" -> new ExpiredAccessTokenKisException(returnCode, messageCode, message);
			case "EGW00205" -> new CredentialsTypeKisException(returnCode, messageCode, message);
			default -> new KisException(returnCode, messageCode, message);
		};
	}
}
