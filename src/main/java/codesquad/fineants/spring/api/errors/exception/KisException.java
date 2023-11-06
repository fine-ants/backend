package codesquad.fineants.spring.api.errors.exception;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class KisException extends RuntimeException {
	private final String message;

	public KisException(String message) {
		this.message = message;
	}
}
