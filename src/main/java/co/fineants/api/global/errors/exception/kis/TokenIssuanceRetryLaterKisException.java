package co.fineants.api.global.errors.exception.kis;

public class TokenIssuanceRetryLaterKisException extends KisException {
	public TokenIssuanceRetryLaterKisException(String returnCode, String messageCode, String message) {
		super(returnCode, messageCode, message);
	}
}
