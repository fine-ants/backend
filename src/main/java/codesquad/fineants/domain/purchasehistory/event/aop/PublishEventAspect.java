package codesquad.fineants.domain.purchasehistory.event.aop;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.regex.Pattern;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import codesquad.fineants.domain.purchasehistory.event.domain.PublishEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Aspect
public class PublishEventAspect implements ApplicationEventPublisherAware {

	private static final String SPEL_REGEX = "^#\\{(.*)\\}$";
	private final Pattern spelPattern = Pattern.compile(SPEL_REGEX);

	private ApplicationEventPublisher eventPublisher;
	private final ExpressionParser expressionParser = new SpelExpressionParser();

	@Pointcut("@annotation(publishEvent)")
	public void pointcut(PublishEvent publishEvent) {
	}

	@AfterReturning(pointcut = "pointcut(publishEvent)", returning = "retVal", argNames = "publishEvent,retVal")
	public void afterReturning(PublishEvent publishEvent, Object retVal) throws
		NoSuchMethodException,
		IllegalAccessException,
		InvocationTargetException,
		InstantiationException {

		Object event;

		if (retVal == null) {
			event = publishEvent.eventType()
				.getDeclaredConstructor()
				.newInstance();

		} else if (!StringUtils.hasText(publishEvent.params())) {
			event = publishEvent.eventType()
				.getConstructor(retVal.getClass())
				.newInstance(retVal);

		} else if (isSpel(publishEvent.params())) {
			String spel = publishEvent.params().replaceAll(SPEL_REGEX, "$1");
			Object constructArg = expressionParser.parseExpression(spel)
				.getValue(retVal); // SendableParameter(portfolioId=1, memberId=1)
			event = publishEvent.eventType()
				.getDeclaredConstructor(Objects.requireNonNull(constructArg).getClass())
				.newInstance(constructArg);

		} else {
			event = publishEvent.eventType().getConstructor(String.class).newInstance(publishEvent.params());
		}

		eventPublisher.publishEvent(event);
	}

	private boolean isSpel(String params) {
		return spelPattern.matcher(params).matches();
	}

	@Override
	public void setApplicationEventPublisher(@NotNull ApplicationEventPublisher applicationEventPublisher) {
		this.eventPublisher = applicationEventPublisher;
	}
}
