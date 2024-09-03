package co.fineants.api.global.errors.errorcode;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileErrorCode implements ErrorCode {
	FAIL_FILE_MAKING(HttpStatus.INTERNAL_SERVER_ERROR, "파일 생성을 실패하였습니다."),
	FAIL_FILE_READ(HttpStatus.INTERNAL_SERVER_ERROR, "파일 읽기에 실패하였습니다.");
	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String toString() {
		return String.format("%s, %s(httpStatus=%s, message=%s)", "ObjectMapper 에러 코드",
			this.getClass().getSimpleName(),
			httpStatus,
			message);
	}
}
