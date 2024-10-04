package co.fineants.api.global.errors.errorcode;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PortfolioErrorCode implements ErrorCode {

	INVALID_PORTFOLIO_FINANCIAL_INFO(HttpStatus.BAD_REQUEST, "포트폴리오 금융 정보는 음수가 될수 없습니다"),
	TARGET_GAIN_LOSS_IS_EQUAL_LESS_THAN_BUDGET(HttpStatus.BAD_REQUEST, "목표 수익금액은 예산보다 커야 합니다"),
	MAXIMUM_LOSS_IS_EQUAL_GREATER_THAN_BUDGET(HttpStatus.BAD_REQUEST, "최대 손실 금액은 예산 보다 작아야 합니다"),
	SECURITIES_FIRM_IS_NOT_CONTAINS(HttpStatus.BAD_REQUEST, "해당 증권사는 포함되어 있지 않습니다"),
	DUPLICATE_NAME(HttpStatus.CONFLICT, "포트폴리오 이름이 중복되었습니다"),
	NOT_FOUND_PORTFOLIO(HttpStatus.NOT_FOUND, "포트폴리오를 찾을 수 없습니다"),
	NOT_HAVE_AUTHORIZATION(HttpStatus.FORBIDDEN, "포트폴리오에 대한 권한이 없습니다"),
	TOTAL_INVESTMENT_PRICE_EXCEEDS_BUDGET(HttpStatus.BAD_REQUEST, "매입 실패, 현금이 부족합니다"),
	TARGET_GAIN_IS_ZERO_WITH_NOTIFY_UPDATE(HttpStatus.BAD_REQUEST, "목표 수익금액이 0원이어서 알림을 수정할 수 없습니다"),
	MAX_LOSS_IS_ZERO_WITH_NOTIFY_UPDATE(HttpStatus.BAD_REQUEST, "최대 손실금액이 0원이어서 알림을 수정할 수 없습니다"),
	FORBIDDEN_PORTFOLIO(HttpStatus.FORBIDDEN, "권한이 없습니다");
	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String toString() {
		return String.format("PortfolioErrorCode(httpStatus=%s, message=%s)", httpStatus, message);
	}
}
