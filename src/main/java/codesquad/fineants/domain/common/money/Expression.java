package codesquad.fineants.domain.common.money;

public interface Expression {
	Money reduce(Bank bank, Currency to);

	Expression plus(Expression addend);

	Expression times(int multiplier);
}
