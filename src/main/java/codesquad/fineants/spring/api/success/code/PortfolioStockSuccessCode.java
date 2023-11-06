package codesquad.fineants.spring.api.success.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PortfolioStockSuccessCode implements SuccessCode {
	CREATED_ADD_PORTFOLIO_STOCK(HttpStatus.CREATED, "포트폴리오 종목이 추가되었습니다"),
	OK_DELETE_PORTFOLIO_STOCK(HttpStatus.OK, "포트폴리오 종목이 삭제되었습니다"),
	OK_READ_PORTFOLIO_STOCKS(HttpStatus.OK, "포트폴리오 상세 정보 및 포트폴리오 종목 목록 조회가 완료되었습니다");

	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String toString() {
		return String.format("%s, %s(name=%s, httpStatus=%s, message=%s)", "포트폴리오 종목 성공 코드",
			this.getClass().getSimpleName(),
			name(),
			httpStatus,
			message);
	}
}
