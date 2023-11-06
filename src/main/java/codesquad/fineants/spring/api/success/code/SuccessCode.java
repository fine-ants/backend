package codesquad.fineants.spring.api.success.code;

import org.springframework.http.HttpStatus;

public interface SuccessCode {
	HttpStatus getHttpStatus();

	String getMessage();
}
