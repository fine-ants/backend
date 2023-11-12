package codesquad.fineants.spring.aop;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class SimpleLogAop {

	@Pointcut("execution(* codesquad.fineants..*.*(..))")
	private void cut() {
	}

	@Before("cut()")
	public void beforeParameterLog(JoinPoint joinPoint) {
		Method method = getMethod(joinPoint);
		log.info("======= method name = {}", method.getName());

		Object[] args = joinPoint.getArgs();
		if (args.length == 0) {
			log.info("no parameter");
		}
		Arrays.stream(args)
			.filter(Objects::nonNull)
			.forEach(arg -> {
				log.info("parameter type = {}", arg.getClass().getSimpleName());
				log.info("parameter value = {}", arg);
			});
	}

	@AfterReturning(value = "cut()", returning = "returnObj")
	public void afterReturnLog(JoinPoint joinPoint, Object returnObj) {
		if (returnObj == null) {
			return;
		}

		Method method = getMethod(joinPoint);
		log.info("======= method name = {} =======", method.getName());

		log.info("return type = {}", returnObj.getClass().getSimpleName());
		log.info("return value = {}", returnObj);
	}

	private Method getMethod(JoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature)joinPoint.getSignature();
		return signature.getMethod();
	}
}
