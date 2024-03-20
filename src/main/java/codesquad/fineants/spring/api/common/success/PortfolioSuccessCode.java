package codesquad.fineants.spring.api.common.success;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PortfolioSuccessCode implements SuccessCode {
	CREATED_ADD_PORTFOLIO(HttpStatus.CREATED, "포트폴리오가 추가되었습니다"),
	OK_MODIFY_PORTFOLIO(HttpStatus.OK, "포트폴리오가 수정되었습니다"),
	OK_DELETE_PORTFOLIO(HttpStatus.OK, "포트폴리오 삭제가 완료되었습니다"),
	OK_SEARCH_PORTFOLIOS(HttpStatus.OK, "포트폴리오 목록 조회가 완료되었습니다"),
	OK_MODIFY_PORTFOLIO_TARGET_GAIN_ACTIVE_NOTIFICATION(HttpStatus.OK, "목표 수익률 알림이 활성화되었습니다"),
	OK_MODIFY_PORTFOLIO_TARGET_GAIN_INACTIVE_NOTIFICATION(HttpStatus.OK, "목표 수익률 알림이 비 활성화되었습니다"),
	OK_MODIFY_PORTFOLIO_MAXIMUM_LOSS_ACTIVE_NOTIFICATION(HttpStatus.OK, "최대 손실율 알림이 활성화되었습니다"),
	OK_MODIFY_PORTFOLIO_MAXIMUM_LOSS_INACTIVE_NOTIFICATION(HttpStatus.OK, "최대 손실율 알림이 비 활성화되었습니다"),
	OK_SEARCH_PORTFOLIO_NOTIFICATION_SETTINGS(HttpStatus.OK, "모든 알림 조회를 성공했습니다");

	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String toString() {
		return String.format("%s, %s(name=%s, httpStatus=%s, message=%s)", "포트폴리오 성공 코드",
			this.getClass().getSimpleName(),
			name(),
			httpStatus,
			message);
	}
}
