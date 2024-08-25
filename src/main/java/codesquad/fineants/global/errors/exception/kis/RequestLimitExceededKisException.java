package codesquad.fineants.global.errors.exception.kis;

public class RequestLimitExceededKisException extends KisException {
	public RequestLimitExceededKisException(String returnCode, String messageCode, String message) {
		super(returnCode, messageCode, message);
	}
}
