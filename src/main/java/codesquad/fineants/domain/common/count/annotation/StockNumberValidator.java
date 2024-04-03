package codesquad.fineants.domain.common.count.annotation;

import java.math.BigInteger;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import codesquad.fineants.domain.common.count.Count;

public class StockNumberValidator implements ConstraintValidator<StockCount, Count> {
	@Override
	public boolean isValid(Count count, ConstraintValidatorContext context) {
		if (count == null) {
			return false;
		}

		return count.getValue().compareTo(BigInteger.ZERO) >= 0;
	}
}
