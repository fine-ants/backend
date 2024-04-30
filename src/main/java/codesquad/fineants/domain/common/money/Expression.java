package codesquad.fineants.domain.common.money;

import codesquad.fineants.domain.common.count.Count;

public interface Expression extends Comparable<Expression> {
	Money reduce(Bank bank, Currency to);

	Expression plus(Expression addend);

	Expression minus(Expression subtrahend);

	Expression times(int multiplier);

	Expression divide(Count divisor);

	RateDivision divide(Expression divisor);
}
