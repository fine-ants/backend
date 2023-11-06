package codesquad.fineants.spring.api.success.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PurchaseHistorySuccessCode implements SuccessCode {
	CREATED_ADD_PURCHASE_HISTORY(HttpStatus.CREATED, "매입 이력이 추가되었습니다"),
	OK_MODIFY_PURCHASE_HISTORY(HttpStatus.OK, "매입 이력이 수정되었습니다"),
	OK_DELETE_PURCHASE_HISTORY(HttpStatus.OK, "매입 이력이 삭제되었습니다");

	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String toString() {
		return String.format("%s, %s(name=%s, httpStatus=%s, message=%s)", "매입이력 성공 코드",
			this.getClass().getSimpleName(),
			name(),
			httpStatus,
			message);
	}
}
