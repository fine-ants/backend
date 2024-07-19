package codesquad.fineants.global.common.resource;

import java.lang.annotation.Annotation;

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
	public Long around(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
		Annotation[][] parameterAnnotations = methodSignature.getMethod().getParameterAnnotations();
		Object[] args = joinPoint.getArgs();

		Long resourceId = null;

		for (int i = 0; i < parameterAnnotations.length; i++) {
			for (Annotation annotation : parameterAnnotations[i]) {
				if (annotation instanceof ResourceId) {
					resourceId = (Long)args[i];
					break;
				}
			}
		}

		if (resourceId == null) {
			throw new IllegalArgumentException("ResourceId annotation not found in method parameters");
		}
		log.info("ResourceId is {}", resourceId);
		return resourceId;
	}
}
