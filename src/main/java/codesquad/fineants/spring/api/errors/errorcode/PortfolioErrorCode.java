package codesquad.fineants.spring.api.errors.errorcode;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PortfolioErrorCode implements ErrorCode {

	TARGET_GAIN_LOSS_IS_EQUAL_LESS_THAN_BUDGET(HttpStatus.BAD_REQUEST, "목표 수익금액은 예산보다 커야 합니다"),
	MAXIMUM_LOSS_IS_EQUAL_GREATER_THAN_BUDGET(HttpStatus.BAD_REQUEST, "최대 손실 금액은 예산 보다 작아야 합니다"),
	DUPLICATE_NAME(HttpStatus.CONFLICT, "포트폴리오 이름이 중복되었습니다"),
	NOT_FOUND_PORTFOLIO(HttpStatus.NOT_FOUND, "포트폴리오를 찾을 수 없습니다"),
	NOT_HAVE_AUTHORIZATION(HttpStatus.FORBIDDEN, "포트폴리오에 대한 권한이 없습니다");

	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String toString() {
		return String.format("%s, %s(name=%s, httpStatus=%s, message=%s)", "포트폴리오 에러 코드",
			this.getClass().getSimpleName(),
			name(),
			httpStatus,
			message);
	}
}
