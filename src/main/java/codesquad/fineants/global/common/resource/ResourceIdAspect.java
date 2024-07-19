package codesquad.fineants.global.common.resource;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class ResourceIdAspect {
	@Around("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
	public List<Long> around(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
		Annotation[][] parameterAnnotations = methodSignature.getMethod().getParameterAnnotations();
		Object[] args = joinPoint.getArgs();

		List<Long> result = Collections.emptyList();

		for (int i = 0; i < parameterAnnotations.length; i++) {
			for (Annotation annotation : parameterAnnotations[i]) {
				if (annotation instanceof ResourceId) {
					result = Collections.singletonList((Long)args[i]);
					break;
				} else if (annotation instanceof ResourceIds) {
					result = (List<Long>)args[i];
					break;
				}
			}
		}

		if (result == null) {
			throw new IllegalArgumentException("ResourceId or ResourceIds annotation not found in method parameters");
		}
		log.info("result is {}", result);
		return result;
	}
}
