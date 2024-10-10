package co.fineants.api.domain.common.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import co.fineants.api.domain.common.count.Count;

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
	public int compareTo(@NotNull Expression o) {
		Money won1 = Bank.getInstance().toWon(this);
		Money won2 = Bank.getInstance().toWon(o);
		return won1.compareTo(won2);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object == null || getClass() != object.getClass())
			return false;
		RateDivision that = (RateDivision)object;
		return Objects.equals(division, that.division) && Objects.equals(divisor, that.divisor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(division, divisor);
	}

	@Override
	public String toString() {
		Bank bank = Bank.getInstance();
		Expression divisionSum = bank.toWon(division);
		Expression divisorSum = bank.toWon(divisor);
		return String.format("RateDivision(division=%s, divisor=%s)=%s", divisionSum, divisorSum,
			toPercentage(bank, Currency.KRW));
	}
}
