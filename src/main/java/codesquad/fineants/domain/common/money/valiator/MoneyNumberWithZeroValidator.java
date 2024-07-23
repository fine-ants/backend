package codesquad.fineants.domain.common.money.valiator;

import codesquad.fineants.domain.common.money.Money;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MoneyNumberWithZeroValidator implements ConstraintValidator<MoneyNumberWithZero, Money> {
	@Override
	public boolean isValid(Money value, ConstraintValidatorContext context) {
		if (value == null) {
			return false;
		}
		return value.compareTo(Money.zero()) >= 0;
	}
}
