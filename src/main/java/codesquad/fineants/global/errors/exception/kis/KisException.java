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

	public static KisException expiredAccessToken() {
		return new ExpiredAccessTokenKisException("1", "EGW00123", "기간이 만료된 token 입니다.");
	}

	public static KisException requestLimitExceeded() {
		return new RequestLimitExceededKisException("1", "EGW00201", "초당 거래건수를 초과하였습니다.");
	}

	public static KisException tokenIssuanceRetryLater() {
		return new TokenIssuanceRetryLaterKisException("1", "EGW00133", "접근토큰 발급 잠시 후 다시 시도하세요(1분당 1회)");
	}
}
