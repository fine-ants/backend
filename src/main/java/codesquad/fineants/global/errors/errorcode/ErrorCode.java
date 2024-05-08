package codesquad.fineants.global.errors.errorcode;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

	HttpStatus getHttpStatus();

	String getMessage();
}
