package codesquad.fineants.spring.api.success.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WatchListSuccessCode implements SuccessCode{
	CREATED_WATCHLIST(HttpStatus.OK, "관심종목 목록이 추가되었습니다"),
	CREATED_WATCHLIST_STOCK(HttpStatus.OK, "관심종목 목록에 종목이 추가되었습니다"),
	DELETED_WATCHLIST_STOCK(HttpStatus.OK, "관심목록 종목이 삭제되었습니다"),
	DELETED_WATCHLIST(HttpStatus.OK, "관심종목 목록이 삭제가 완료되었습니다");

	private final HttpStatus httpStatus;
	private final String message;

}
