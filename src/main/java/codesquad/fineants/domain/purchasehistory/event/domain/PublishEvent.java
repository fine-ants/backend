package codesquad.fineants.domain.purchasehistory.event.domain;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PublishEvent {

	/**
	 * return이 비어있는 경우 new eventType()
	 * params가 비어있는 경우 new eventType(returnValue)
	 * params가 문자열인 경우 new event
	 */
	Class<? extends EventHoldingValue> eventType();

	// 빈값, 문자열, SpEL('#{표현식}')을 사용할 수 있음
	String params() default "";
}
