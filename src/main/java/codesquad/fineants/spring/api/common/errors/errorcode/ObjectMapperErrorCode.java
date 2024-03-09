package codesquad.fineants.spring.api.common.errors.errorcode;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ObjectMapperErrorCode implements ErrorCode {
	FAIL_DESERIALIZE(HttpStatus.INTERNAL_SERVER_ERROR, "역직렬화에 실패하였습니다"),
	FAIL_SERIALIZE(HttpStatus.INTERNAL_SERVER_ERROR, "직렬화에 실패하였습니다");

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
