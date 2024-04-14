package codesquad.fineants.domain.common.money.valiator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = MoneyNumberWithZeroValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MoneyNumberWithZero {
	String message() default "금액은 0포함 양수여야 합니다";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}