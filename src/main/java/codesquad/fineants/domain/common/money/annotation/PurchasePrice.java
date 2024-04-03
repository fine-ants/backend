package codesquad.fineants.domain.common.money.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Payload;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PurchasePrice {
	String message() default "매입가는 0포함 양수여야 합니다";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
