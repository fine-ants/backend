package co.fineants.api.global.aspect;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Aspect
@Slf4j
@Profile("!test")
public class ServiceLogAspect {
	private long startTime;

	// service의 모든 메서드에 대해 적용
	@Pointcut("execution(* co.fineants..service.*.*(..))")
	public void pointCut() {

	}

	// 메서드 호출 전 로그 남기기
	@Before("pointCut()")
	public void logBefore(JoinPoint joinPoint) {
		startTime = System.currentTimeMillis();
		String methodName = ((MethodSignature)joinPoint.getSignature()).getMethod().getName();
		String args = Arrays.toString(joinPoint.getArgs());
		log.info("Entering Service: Method={} with Args={}", methodName, args);
	}

	// 메서드 호출 후 정상적으로 반환된 경우 로그 남기기
	@AfterReturning(pointcut = "pointCut()", returning = "result")
	public void logAfterReturning(JoinPoint joinPoint, Object result) {
		String methodName = ((MethodSignature)joinPoint.getSignature()).getMethod().getName();
		log.info("Exiting Service: Method={}, with Return={}", methodName, result);
	}

	// 완전히 종료된후 메서드 실행시간 측정하기
	@After("pointCut()")
	public void logAfter(JoinPoint joinPoint) {
		long executionTime = System.currentTimeMillis() - startTime;
		String methodName = ((MethodSignature)joinPoint.getSignature()).getMethod().getName();
		log.info("Method={}, ExecutionTime={}ms", methodName, executionTime);
	}
}
