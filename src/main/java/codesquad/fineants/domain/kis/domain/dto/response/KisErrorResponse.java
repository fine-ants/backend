package codesquad.fineants.domain.kis.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import codesquad.fineants.global.errors.exception.KisException;
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
		return new KisException(returnCode, messageCode, message);
	}
}
