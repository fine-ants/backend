package codesquad.fineants.spring.api.success.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DashboardSuccessCode implements SuccessCode {
	OK_OVERVIEW(HttpStatus.OK, "오버뷰 데이터 조회가 완료되었습니다."),
	OK_PORTFOLIO_PIE_CHART(HttpStatus.OK, "포트폴리오 파이 차트 데이터 조회가 완료되었습니다."),
	OK_LINE_CHART(HttpStatus.OK, "전체 평가액 데이터 조회가 완료되었습니다.");
	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String toString() {
		return String.format("%s, %s(name=%s, httpStatus=%s, message=%s)", "대시 보드 성공 코드",
			this.getClass().getSimpleName(),
			name(),
			httpStatus,
			message);
	}
}
