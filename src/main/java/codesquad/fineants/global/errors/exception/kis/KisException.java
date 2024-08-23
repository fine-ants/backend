package codesquad.fineants.global.errors.exception.kis;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class KisException extends RuntimeException {
	private final String returnCode;
	private final String messageCode;
	private final String message;

	public KisException(String returnCode, String messageCode, String message) {
		super(message);
		this.returnCode = returnCode;
		this.messageCode = messageCode;
		this.message = message;
	}
}
