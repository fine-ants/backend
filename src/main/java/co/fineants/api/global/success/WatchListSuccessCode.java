package co.fineants.api.global.success;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WatchListSuccessCode implements SuccessCode {
	CREATED_WATCH_LIST(HttpStatus.OK, "관심종목 목록이 추가되었습니다"),
	CREATED_WATCH_STOCK(HttpStatus.OK, "관심종목 목록에 종목이 추가되었습니다"),
	DELETED_WATCH_STOCK(HttpStatus.OK, "관심목록 종목이 삭제되었습니다"),
	DELETED_WATCH_LIST(HttpStatus.OK, "관심종목 목록이 삭제가 완료되었습니다"),
	READ_WATCH_LISTS(HttpStatus.OK, "관심목록 목록 조회가 완료되었습니다"),
	READ_WATCH_LIST(HttpStatus.OK, "관심종목 단일 목록 조회가 완료되었습니다"),
	CHANGE_WATCH_LIST_NAME(HttpStatus.OK, "관심종목 목록 이름이 변경되었습니다"),
	HAS_STOCK(HttpStatus.OK, "관심목록의 주식 포함 여부 조회가 완료되었습니다");

	private final HttpStatus httpStatus;
	private final String message;

}
