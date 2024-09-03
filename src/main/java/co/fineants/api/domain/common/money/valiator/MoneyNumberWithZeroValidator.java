package co.fineants.api.domain.common.money.valiator;

import co.fineants.api.domain.common.money.Money;
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
