package codesquad.fineants.global.api;

import org.springframework.http.HttpStatus;

import codesquad.fineants.global.success.SuccessCode;
import codesquad.fineants.global.errors.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class ApiResponse<T> {

	private final int code;
	private final String status;
	private final String message;
	private final T data;

	public ApiResponse(HttpStatus httpStatus, String message, T data) {
		this.code = httpStatus.value();
		this.status = httpStatus.getReasonPhrase();
		this.message = message;
		this.data = data;
	}

	public static <T> ApiResponse<T> of(HttpStatus httpStatus, String message, T data) {
		return new ApiResponse<>(httpStatus, message, data);
	}

	public static <T> ApiResponse<T> ok(String message, T data) {
		return new ApiResponse<>(HttpStatus.OK, message, data);
	}

	public static <T> ApiResponse<T> created(String message) {
		return created(message, null);
	}

	public static <T> ApiResponse<T> created(String message, T data) {
		return new ApiResponse<>(HttpStatus.CREATED, message, data);
	}

	public static <T> ApiResponse<T> success(SuccessCode code) {
		return new ApiResponse<>(code.getHttpStatus(), code.getMessage(), null);
	}

	public static <T> ApiResponse<T> success(SuccessCode code, T data) {
		return new ApiResponse<>(code.getHttpStatus(), code.getMessage(), data);
	}

	public static <T> ApiResponse<T> error(ErrorCode errorCode) {
		return new ApiResponse<>(errorCode.getHttpStatus(), errorCode.getMessage(), null);
	}

	@Override
	public String toString() {
		return String.format("%s, %s(code=%d, status=%s, message=%s)", "API 공통 응답", this.getClass().getSimpleName(),
			code,
			status,
			message);
	}
}
