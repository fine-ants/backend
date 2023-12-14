package codesquad.fineants.spring.api.errors.errorcode;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JwkErrorCode implements ErrorCode {

	NOT_FOUND_SIGNING_KEY(HttpStatus.NOT_FOUND, "유효하지 않은 토큰입니다"),
	INVALID_PUBLIC_KEY(HttpStatus.BAD_REQUEST, "key 타입이 RSA 또는 EC 타입을 지원하지 않습니다"),
	INVALID_ID_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 ID Token입니다"),
	INVALID_JWK_URI(HttpStatus.BAD_REQUEST, "유효하지 않은 JWK URI입니다");

	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String toString() {
		return String.format("%s, %s(name=%s, httpStatus=%s, message=%s)", "JWK 에러 코드",
			this.getClass().getSimpleName(),
			name(),
			httpStatus,
			message);
	}
}
