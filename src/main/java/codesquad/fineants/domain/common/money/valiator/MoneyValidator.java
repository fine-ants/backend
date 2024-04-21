package codesquad.fineants.domain.common.money.valiator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import codesquad.fineants.domain.common.money.Money;

public class MoneyValidator implements ConstraintValidator<MoneyNumber, Money> {
	@Override
	public boolean isValid(Money value, ConstraintValidatorContext context) {
		if (value == null) {
			return false;
		}
		return value.compareTo(Money.zero()) > 0;
	}
}
