package codesquad.fineants.global.errors.exception;

public class ExpiredKisAccessTokenException extends KisException {

	public ExpiredKisAccessTokenException(String returnCode, String messageCode, String message) {
		super(returnCode, messageCode, message);
	}
}
