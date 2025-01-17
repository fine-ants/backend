package co.fineants.api.global.aspect;

import java.util.Objects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.errors.exception.FineAntsException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Component
@Aspect
@Slf4j
public class HttpLoggingAspect {
	private static final String HTTP_REQUEST_LOG_FORMAT = "HTTP Request: HTTPMethod={} Path={} from IP={}";
	private static final String HTTP_RESPONSE_LOG_FORMAT =
		"HTTP Response: Path={} ResponseCode={} ResponseMessage=\"{}\" ResponseData=\"{}\"";
	private static final String HTTP_EXECUTION_LOG_FORMAT = "HTTP Execution: Path={} ExecutionTime={}ms";
	private long startTime;

	// Controller의 모든 메서드에 대해 적용

	@Pointcut("execution(* co.fineants..controller.*.*(..))")
	public void pointCut() {

	}

	@Before("pointCut()")
	public void logHttpRequest(JoinPoint ignoredJoinPoint) {
		startTime = System.currentTimeMillis();
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes())
			.getRequest();
		log.info(HTTP_REQUEST_LOG_FORMAT, request.getMethod(), request.getRequestURL(), request.getRemoteAddr());
	}

	// 메서드 호출 후 정상적으로 반환된 경우 로그 남기기
	@AfterReturning(pointcut = "pointCut()", returning = "response")
	public void logAfterReturning(JoinPoint ignoredJoinPoint, ApiResponse<?> response) {
		HttpServletRequest request =
			((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		log.info(HTTP_RESPONSE_LOG_FORMAT, request.getRequestURI(),
			response.getCode(), response.getMessage(), response.getData());
	}

	// 예외 발생시 로그 남기기
	@AfterThrowing(pointcut = "pointCut()", throwing = "ex")
	public void logAfterThrowing(JoinPoint ignoredJoinPoint, Throwable ex) {
		HttpServletRequest request =
			((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		// 특정 비즈니스 예외 처리
		if (ex instanceof FineAntsException fineAntsException) {
			log.warn(HTTP_RESPONSE_LOG_FORMAT,
				request.getRequestURI(), fineAntsException.getHttpStatusCode(), fineAntsException.getMessage(), null,
				ex);
		} else if (ex instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
			String errorMessage = Objects.requireNonNull(methodArgumentNotValidException.getBindingResult()
				.getFieldError()).getDefaultMessage();
			log.warn(HTTP_RESPONSE_LOG_FORMAT,
				request.getRequestURI(), HttpStatus.BAD_REQUEST.value(), errorMessage, null, ex);
		} else if (ex instanceof MissingServletRequestPartException missingServletRequestPartException) {
			String errorMessage = missingServletRequestPartException.getMessage();
			log.warn(HTTP_RESPONSE_LOG_FORMAT,
				request.getRequestURI(), HttpStatus.BAD_REQUEST.value(), errorMessage, null, ex);
		} else {
			log.error(HTTP_RESPONSE_LOG_FORMAT,
				request.getRequestURI(), HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), null, ex);
		}
	}

	// 완전히 종료된후 메서드 실행시간 측정하기
	@After("pointCut()")
	public void logAfter(JoinPoint ignoredJoinPoint) {
		HttpServletRequest request =
			((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		long executionTime = System.currentTimeMillis() - startTime;
		log.info(HTTP_EXECUTION_LOG_FORMAT, request.getRequestURI(), executionTime);
	}
}
