package codesquad.fineants.domain.common.money;

import static codesquad.fineants.domain.common.money.Currency.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import codesquad.fineants.domain.common.count.Count;

public class Money implements Expression {
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
		double rate = bank.rate(currency, to);
		return new Money(amount.divide(new BigDecimal(rate), 4, RoundingMode.HALF_UP), to);
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
		return new Money(amount.multiply(new BigDecimal(multiplier)), currency);
	}

	@Override
	public Expression divide(Count divisor) {
		return new AverageDivision(this, divisor);
	}

	@Override
	public RateDivision divide(Expression divisor) {
		return new RateDivision(this, divisor);
	}

	public Expression divide(BigInteger divisor) {
		try {
			BigDecimal result = amount.divide(new BigDecimal(divisor), 4, RoundingMode.HALF_UP);
			return new Money(result, currency);
		} catch (ArithmeticException e) {
			return new Money(BigDecimal.ZERO, currency);
		}

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

	public String getCurrencySymbol() {
		return currency.getSymbol();
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
	public String toString() {
		return amount.toString();
	}

	@Override
	public int compareTo(@NotNull Expression o) {
		Money won = Bank.getInstance().toWon(this);
		Money won2 = Bank.getInstance().toWon(o);
		return won.compareTo(won2);
	}

	public int compareTo(Money m) {
		return this.amount.compareTo(m.amount);
	}
}
