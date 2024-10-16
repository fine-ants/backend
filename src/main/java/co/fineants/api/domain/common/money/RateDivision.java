package co.fineants.api.domain.common.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.jetbrains.annotations.NotNull;

import co.fineants.api.domain.common.count.Count;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = {"division", "divisor"})
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
		BigDecimal amount = bank.reduce(division, to).amount.divide(bank.reduce(divisor, to).amount, 4,
			RoundingMode.HALF_UP);
		return new Money(amount, to);
	}

	@Override
	public Expression plus(Expression addend) {
		if (!(addend instanceof RateDivision)) {
			throw new IllegalArgumentException("addend must be RateDivision");
		}
		return new Sum(this, addend);
	}

	@Override
	public Expression minus(Expression subtrahend) {
		if (!(subtrahend instanceof RateDivision)) {
			throw new IllegalArgumentException("subtrahend must be RateDivision");
		}
		return new Subtraction(this, subtrahend);
	}

	@Override
	public Expression times(int multiplier) {
		return new RateDivision(division.times(multiplier), divisor);
	}

	@Override
	public Expression divide(Count divisor) {
		throw new IllegalArgumentException("This operation is not supported");
	}

	@Override
	public RateDivision divide(Expression divisor) {
		if (!(divisor instanceof RateDivision)) {
			throw new IllegalArgumentException("divisor must be RateDivision");
		}
		return new RateDivision(this, divisor);
	}

	@Override
	public Percentage toPercentage(Bank bank, Currency to) {
		try {
			BigDecimal rate = division.reduce(bank, to).amount.divide(divisor.reduce(bank, to).amount, 4,
				RoundingMode.HALF_UP);
			return Percentage.from(rate);
		} catch (ArithmeticException e) {
			return Percentage.zero();
		}
	}

	@Override
	public int compareTo(@NotNull Expression expression) {
		Bank bank = Bank.getInstance();
		Money won1 = bank.toWon(this);
		Money won2 = bank.toWon(expression);
		return won1.compareTo(won2);
	}

	@Override
	public String toString() {
		Bank bank = Bank.getInstance();
		return String.format("%s", toPercentage(bank, Currency.KRW));
	}
}
