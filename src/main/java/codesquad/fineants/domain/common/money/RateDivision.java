package codesquad.fineants.domain.common.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.jetbrains.annotations.NotNull;

import codesquad.fineants.domain.common.count.Count;

public class RateDivision implements Expression {

	private final Expression division;
	private final Expression divisor;

	public RateDivision(Expression division, Expression divisor) {
		this.division = division;
		this.divisor = divisor;
	}

	public static RateDivision zero() {
		return of(Money.zero(), Money.zero());
	}

	public static RateDivision of(Expression division, Expression divisor) {
		return new RateDivision(division, divisor);
	}

	@Override
	public Money reduce(Bank bank, Currency to) {
		return null;
	}

	@Override
	public Expression plus(Expression addend) {
		return null;
	}

	@Override
	public Expression minus(Expression subtrahend) {
		return null;
	}

	@Override
	public Expression times(int multiplier) {
		return null;
	}

	@Override
	public Expression divide(Count divisor) {
		return null;
	}

	@Override
	public RateDivision divide(Expression divisor) {
		return null;
	}

	@Override
	public int compareTo(@NotNull Expression o) {
		return 0;
	}

	public Percentage toPercentage(Bank bank, Currency to) {
		try {
			BigDecimal rate = division.reduce(bank, to).amount.divide(divisor.reduce(bank, to).amount, 4,
				RoundingMode.HALF_UP);
			return Percentage.from(rate);
		} catch (ArithmeticException e) {
			return Percentage.zero();
		}
	}
}
