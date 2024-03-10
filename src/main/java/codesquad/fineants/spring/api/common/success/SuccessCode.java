package codesquad.fineants.spring.api.common.success;

import org.springframework.http.HttpStatus;

public interface SuccessCode {
	HttpStatus getHttpStatus();

	String getMessage();
}
