package codesquad.fineants.global.errors.exception.kis;

public class ExpiredAccessTokenKisException extends KisException {

	public ExpiredAccessTokenKisException(String returnCode, String messageCode, String message) {
		super(returnCode, messageCode, message);
	}
}
