package co.fineants.api.global.success;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StockSuccessCode implements SuccessCode {
	OK_SEARCH_STOCKS(HttpStatus.OK, "종목 검색이 완료되었습니다"),
	OK_SEARCH_DETAIL_STOCK(HttpStatus.OK, "종목 상세정보 조회가 완료되었습니다"),
	OK_CREATE_TARGET_PRICE_NOTIFICATION(HttpStatus.CREATED, "해당 종목 지정가 알림을 추가했습니다"),
	OK_CREATE_TARGET_PRICE_SEND_NOTIFICATION(HttpStatus.CREATED, "종목 지정가 알림을 발송하였습니다"),
	OK_DELETE_STOCK_TARGET_PRICE(HttpStatus.OK, "해당 종목 지정가를 제거했습니다"),
	OK_DELETE_TARGET_PRICE_NOTIFICATIONS(HttpStatus.OK, "해당 종목 지정가 알림을 제거했습니다"),
	OK_SEARCH_TARGET_PRICE_NOTIFICATIONS(HttpStatus.OK, "모든 알림 조회를 성공했습니다"),
	OK_SEARCH_SPECIFIC_TARGET_PRICE_NOTIFICATIONS(HttpStatus.OK, "종목 지정가 알림 특정 조회를 성공했습니다"),
	OK_UPDATE_TARGET_PRICE_NOTIFICATION_ACTIVE(HttpStatus.OK, "종목 지정가 알림을 활성화하였습니다"),
	OK_UPDATE_TARGET_PRICE_NOTIFICATION_INACTIVE(HttpStatus.OK, "종목 지정가 알림을 비 활성화하였습니다"),
	OK_REFRESH_STOCKS(HttpStatus.OK, "종목 최신화가 완료되었습니다"),
	OK_INIT_STOCKS(HttpStatus.OK, "종목 초기화가 완료되었습니다");
	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String toString() {
		return String.format("%s, %s(name=%s, httpStatus=%s, message=%s)", "종목 성공 코드",
			this.getClass().getSimpleName(),
			name(),
			httpStatus,
			message);
	}
}
