package co.fineants.api.global.api;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.api.global.errors.errorcode.ErrorCode;
import co.fineants.api.global.success.SuccessCode;

public class ApiResponse<T> {

	@JsonProperty
	private final int code;
	@JsonProperty
	private final String status;
	@JsonProperty
	private final String message;
	@JsonProperty
	private final T data;

	private ApiResponse(HttpStatus httpStatus, String message, T data) {
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
		return error(errorCode, null);
	}

	public static <T> ApiResponse<T> error(ErrorCode errorCode, T data) {
		return new ApiResponse<>(errorCode.getHttpStatus(), errorCode.getMessage(), data);
	}

	@Override
	public String toString() {
		return String.format("%s, %s(code=%d, status=%s, message=%s, data=%s)", "API 공통 응답",
			this.getClass().getSimpleName(),
			code,
			status,
			message,
			data);
	}
}
