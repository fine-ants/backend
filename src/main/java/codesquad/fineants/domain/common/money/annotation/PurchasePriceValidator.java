package codesquad.fineants.domain.common.money.annotation;

import java.math.BigDecimal;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import codesquad.fineants.domain.common.money.Money;

public class PurchasePriceValidator implements ConstraintValidator<PurchasePrice, Money> {
	@Override
	public boolean isValid(Money money, ConstraintValidatorContext context) {
		if (money == null) {
			return false;
		}
		return money.getAmount().compareTo(BigDecimal.ZERO) >= 0;
	}
}
