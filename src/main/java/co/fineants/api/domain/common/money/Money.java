package co.fineants.api.domain.common.money;

import static co.fineants.api.domain.common.money.Currency.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import co.fineants.api.domain.common.count.Count;

public final class Money implements Expression {
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###");
	private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.KOREA);
	private static final Money ZERO = new Money(BigDecimal.ZERO, KRW);
	final BigDecimal amount;

	final Currency currency;

	Money(BigDecimal amount, Currency currency) {
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
		return franc(BigDecimal.valueOf(amount));
	}

	public static Money franc(BigDecimal amount) {
		return new Money(amount, CHF);
	}

	public static Money won(String amount) {
		return won(new BigDecimal(amount));
	}

	public static Money won(int amount) {
		return won(BigDecimal.valueOf(amount));
	}

	public static Money won(BigDecimal amount) {
		return new Money(amount, KRW);
	}

	public static Money won(long amount) {
		return won(BigDecimal.valueOf(amount));
	}

	public static Money won(double amount) {
		return won(BigDecimal.valueOf(amount));
	}

	public static Money zero() {
		return ZERO;
	}

	@Override
	public Money reduce(Bank bank, Currency to) {
		double rate = bank.rate(currency, to);
		return new Money(amount.divide(BigDecimal.valueOf(rate), 4, RoundingMode.HALF_UP), to);
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
		return new Money(amount.multiply(BigDecimal.valueOf(multiplier)), currency);
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

	@Override
	public Percentage toPercentage(Bank bank, Currency to) {
		return Percentage.from(reduce(bank, to).amount);
	}

	public Currency currency() {
		return currency;
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

	public String toRawAmount() {
		return amount.toString();
	}

	/**
	 * 두 금액간에 대소 비교한다
	 * 주의 : 화폐(currency)단위 통일은 호출하는 객체(this)의 화폐를 기준으로 한다
	 * 만약 bank 객체에 두 화폐간에 환율(rate)이 존재하지 않으면 에러가 발생한다
	 * @param expression the object to be compared.
	 * @return 대소 결과
	 */
	@Override
	public int compareTo(@NotNull Expression expression) {
		Bank bank = Bank.getInstance();
		Money m1 = bank.reduce(this, currency);
		Money m2 = bank.reduce(expression, currency);
		return m1.amount.compareTo(m2.amount);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || getClass() != object.getClass()) {
			return false;
		}
		Money money = (Money)object;
		return compareTo(money) == 0 && currency().equals(money.currency);
	}

	@Override
	public int hashCode() {
		return Objects.hash(amount);
	}

	@Override
	public String toString() {
		return String.format("%s%s", currency, NUMBER_FORMAT.format(amount));
	}
}
