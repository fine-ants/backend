package co.fineants.api.domain.common.money;

import static co.fineants.api.domain.common.money.Currency.*;

import org.jetbrains.annotations.NotNull;

import co.fineants.api.domain.common.count.Count;

public class AverageDivision implements Expression {

	private final Expression division;
	private final Count divisor;

	public AverageDivision(Expression division, Count divisor) {
		this.division = division;
		this.divisor = divisor;
	}

	@Override
	public Money reduce(Bank bank, Currency to) {
		Expression result = divisor.division(bank.reduce(division, to));
		return bank.reduce(result, to);
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
		return new AverageDivision(division.times(multiplier), divisor);
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
	public int compareTo(@NotNull Expression expression) {
		Bank bank = Bank.getInstance();
		Currency to = KRW;
		Money won = this.reduce(bank, to);
		Money won2 = expression.reduce(bank, to);
		return won.compareTo(won2);
	}

	@Override
	public String toString() {
		return String.format("%s", reduce(Bank.getInstance(), KRW));
	}
}
