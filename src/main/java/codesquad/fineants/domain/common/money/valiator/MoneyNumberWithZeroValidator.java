package codesquad.fineants.domain.common.money.valiator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import codesquad.fineants.domain.common.money.Money;

public class MoneyNumberWithZeroValidator implements ConstraintValidator<MoneyNumberWithZero, Money> {
	@Override
	public boolean isValid(Money value, ConstraintValidatorContext context) {
		if (value == null) {
			return false;
		}
		return value.compareTo(Money.zero()) >= 0;
	}
}
