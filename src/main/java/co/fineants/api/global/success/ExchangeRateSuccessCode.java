package co.fineants.api.global.success;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExchangeRateSuccessCode implements SuccessCode {
	CREATE_EXCHANGE_RATE(HttpStatus.CREATED, "환율 생성을 완료하였습니다"),
	READ_EXCHANGE_RATE(HttpStatus.OK, "환율 목록 조회를 완료하였습니다"),
	UPDATE_EXCHANGE_RATE(HttpStatus.OK, "환율 갱신을 완료하였습니다"),
	DELETE_EXCHANGE_RATE(HttpStatus.OK, "환율 삭제를 완료하였습니다"),
	PATCH_EXCHANGE_RATE(HttpStatus.OK, "기준 통화 변경을 완료하였습니다.");

	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String toString() {
		return String.format("%s, %s(name=%s, httpStatus=%s, message=%s)", "환율 성공 코드",
			this.getClass().getSimpleName(),
			name(),
			httpStatus,
			message);
	}
}
