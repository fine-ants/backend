package codesquad.fineants.domain.common.money;

import java.math.BigDecimal;

public class Subtraction implements Expression {
	private final Expression augend;
	private final Expression addend;

	public Subtraction(Expression augend, Expression addend) {
		this.augend = augend;
		this.addend = addend;
	}

	@Override
	public Money reduce(Bank bank, Currency to) {
		BigDecimal amount = bank.reduce(augend, to).amount.subtract(bank.reduce(addend, to).amount);
		return new Money(amount, to);
	}

	@Override
	public Expression plus(Expression addend) {
		return new Subtraction(this, addend);
	}

	@Override
	public Expression times(int multiplier) {
		return new Subtraction(augend.times(multiplier), addend.times(multiplier));
	}
}
