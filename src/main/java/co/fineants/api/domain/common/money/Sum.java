package co.fineants.api.domain.common.money;

import java.math.BigDecimal;

import org.jetbrains.annotations.NotNull;

import co.fineants.api.domain.common.count.Count;

public class Sum implements Expression {
	private final Expression augend;
	private final Expression addend;

	public Sum(Expression augend, Expression addend) {
		this.augend = augend;
		this.addend = addend;
	}

	@Override
	public Money reduce(Bank bank, Currency to) {
		BigDecimal amount = bank.reduce(augend, to).amount.add(bank.reduce(addend, to).amount);
		return new Money(amount, to);
	}

	@Override
	public Expression plus(Expression addend) {
		return new Sum(this, addend);
	}

	@Override
	public Expression minus(Expression subtrahend) {
		return new Subtraction(this, subtrahend);
	}

	@Override
	public Expression times(int multiplier) {
		return new Sum(augend.times(multiplier), addend.times(multiplier));
	}

	@Override
	public Expression divide(Count divisor) {
		return new AverageDivision(this, divisor);
	}

	@Override
	public RateDivision divide(Expression divisor) {
		return new RateDivision(this, divisor);
	}

	@Override
	public Percentage toPercentage(Bank bank, Currency to) {
		return Percentage.from(reduce(bank, to).amount);
	}

	@Override
	public int compareTo(@NotNull Expression o) {
		Money won1 = Bank.getInstance().toWon(this);
		Money won2 = Bank.getInstance().toWon(o);
		return won1.compareTo(won2);
	}

	@Override
	public String toString() {
		return String.format("Sum(augend=%s, addend=%s)", augend, addend);
	}
}
