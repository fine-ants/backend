package codesquad.fineants.global.errors.exception;

public class RequestLimitExceededException extends KisException {
	public RequestLimitExceededException(String returnCode, String messageCode, String message) {
		super(returnCode, messageCode, message);
	}
}
