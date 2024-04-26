package codesquad.fineants.domain.common.money;

import static codesquad.fineants.domain.common.money.Currency.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Objects;

import codesquad.fineants.domain.common.count.Count;

public class Money implements Comparable<Money>, Expression {
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###");
	protected final BigDecimal amount;

	protected final Currency currency;

	public Money(int amount, Currency currency) {
		this(new BigDecimal(amount), currency);
	}

	public Money(BigDecimal amount, Currency currency) {
		this.amount = amount;
		this.currency = currency;
	}

	public static Money dollar(int amount) {
		return dollar(new BigDecimal(amount));
	}

	public static Money dollar(BigDecimal amount) {
		return new Money(amount, USD);
	}

	public static Money franc(int amount) {
		return franc(new BigDecimal(amount));
	}

	public static Money franc(BigDecimal amount) {
		return new Money(amount, CHF);
	}

	public static Money won(String amount) {
		return won(new BigDecimal(amount));
	}

	public static Money won(int amount) {
		return won(new BigDecimal(amount));
	}

	public static Money won(BigDecimal amount) {
		return new Money(amount, KRW);
	}

	public static Money won(long amount) {
		return won(new BigDecimal(amount));
	}

	public static Money won(double amount) {
		return won(new BigDecimal(amount));
	}

	public static Money zero() {
		return won(BigDecimal.ZERO);
	}

	public static Expression wonZero() {
		return won(BigDecimal.ZERO);
	}

	@Override
	public Money reduce(Bank bank, Currency to) {
		int rate = bank.rate(currency, to);
		return new Money(amount.divide(new BigDecimal(rate)), to);
	}

	@Override
	public Expression times(int multiplier) {
		return new Money(amount.multiply(new BigDecimal(multiplier)), currency);
	}

	@Override
	public Expression plus(Expression addend) {
		return new Sum(this, addend);
	}

	public Currency currency() {
		return currency;
	}

	public Money add(Money money) {
		return Money.won(amount.add(money.amount));
	}

	public Money subtract(Money money) {
		return Money.won(amount.subtract(money.amount));
	}

	public Money multiply(Count count) {
		return count.multiply(this);
	}

	public Money multiply(BigInteger value) {
		return Money.won(amount.multiply(new BigDecimal(value)).setScale(4, RoundingMode.HALF_UP));
	}

	public Money divide(Money money) {
		if (money.isZero()) {
			return Money.zero();
		}
		return Money.won(amount.divide(money.amount, 4, RoundingMode.HALF_UP));
	}

	public Money divide(Count divisor) {
		if (divisor.isZero()) {
			return Money.zero();
		}
		return divisor.division(this);
	}

	public Money divide(BigInteger divisor) {
		return Money.won(amount.divide(new BigDecimal(divisor), 4, RoundingMode.HALF_UP));
	}

	public double toPercentage() {
		return amount.multiply(BigDecimal.valueOf(100)).setScale(4, RoundingMode.HALF_UP).doubleValue();
	}

	public boolean isZero() {
		return amount.compareTo(BigDecimal.ZERO) == 0;
	}

	public String toDecimalFormat() {
		return DECIMAL_FORMAT.format(amount);
	}

	public BigDecimal toInteger() {
		return amount.setScale(0, RoundingMode.HALF_UP);
	}

	public BigDecimal toDouble() {
		return amount.setScale(2, RoundingMode.HALF_UP);
	}

	@Override
	public boolean equals(Object object) {
		Money money = (Money)object;
		return compareTo(money) == 0 && currency().equals(money.currency);
	}

	@Override
	public int hashCode() {
		return Objects.hash(amount);
	}

	@Override
	public int compareTo(Money m) {
		return this.amount.compareTo(m.amount);
	}

	@Override
	public String toString() {
		return amount.toString();
	}
}
