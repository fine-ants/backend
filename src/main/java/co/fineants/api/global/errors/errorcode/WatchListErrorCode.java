package co.fineants.api.global.errors.errorcode;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WatchListErrorCode implements ErrorCode {
	NOT_FOUND_WATCH_LIST(HttpStatus.NOT_FOUND, "관심목록을 찾지 못하였습니다."),
	NOT_FOUND_WATCH_STOCK(HttpStatus.NOT_FOUND, "관심목록 종목을 찾지 못하였습니다."),
	FORBIDDEN_WATCHLIST(HttpStatus.FORBIDDEN, "권한이 없습니다."),
	ALREADY_WATCH_STOCK(HttpStatus.CONFLICT, "이미 존재하는 관심 종목입니다");

	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String toString() {
		return String.format("%s, %s(name=%s, httpStatus=%s, message=%s)", "관심목록 에러 코드",
			this.getClass().getSimpleName(),
			name(),
			httpStatus,
			message);
	}
}
