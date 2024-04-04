package codesquad.fineants.domain.common.count.valiator;

import java.math.BigInteger;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import codesquad.fineants.domain.common.count.Count;

public class CountNumberValidator implements ConstraintValidator<CountNumber, Count> {
	@Override
	public boolean isValid(Count value, ConstraintValidatorContext context) {
		if (value == null) {
			return false;
		}
		return value.getValue().compareTo(BigInteger.ZERO) >= 0;
	}
}
