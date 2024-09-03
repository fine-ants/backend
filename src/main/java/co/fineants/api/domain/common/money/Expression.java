package co.fineants.api.domain.common.money;

import co.fineants.api.domain.common.count.Count;

public interface Expression extends Comparable<Expression> {
	Money reduce(Bank bank, Currency to);

	Expression plus(Expression addend);

	Expression minus(Expression subtrahend);

	Expression times(int multiplier);

	Expression divide(Count divisor);

	RateDivision divide(Expression divisor);

	Percentage toPercentage(Bank bank, Currency to);
}
