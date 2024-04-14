package codesquad.fineants.domain.common.money;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;

import codesquad.fineants.domain.common.count.Count;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Money implements Comparable<Money> {
	private final BigDecimal amount;

	private Money() {
		this.amount = BigDecimal.ZERO;
	}

	private Money(int amount) {
		this.amount = BigDecimal.valueOf(amount);
	}

	private Money(double amount) {
		this.amount = BigDecimal.valueOf(amount);
	}

	private Money(long amount) {
		this.amount = BigDecimal.valueOf(amount);
	}

	public static Money from(BigDecimal amount) {
		return new Money(amount);
	}

	public static Money from(String amount) {
		return new Money(new BigDecimal(amount));
	}

	public static Money from(int amount) {
		return new Money(amount);
	}

	public static Money from(long amount) {
		return new Money(BigDecimal.valueOf(amount));
	}

	public static Money from(double amount) {
		return new Money(BigDecimal.valueOf(amount));
	}

	public static Money zero() {
		return new Money(BigDecimal.ZERO);
	}

	public Money add(Money money) {
		return Money.from(amount.add(money.amount));
	}

	public Money subtract(Money money) {
		return Money.from(amount.subtract(money.amount));
	}

	public Money multiply(Count count) {
		return count.multiply(this);
	}

	public Money multiply(BigInteger value) {
		return Money.from(amount.multiply(new BigDecimal(value)).setScale(4, RoundingMode.HALF_UP));
	}

	public Money divide(Money money) {
		if (money.isZero()) {
			return Money.zero();
		}
		return Money.from(amount.divide(money.amount, 4, RoundingMode.HALF_UP));
	}

	public Money divide(Count divisor) {
		if (divisor.isZero()) {
			return Money.zero();
		}
		return divisor.division(this);
	}

	public Money divide(BigInteger divisor) {
		return Money.from(amount.divide(new BigDecimal(divisor), 4, RoundingMode.HALF_UP));
	}

	public double toPercentage() {
		return amount.multiply(BigDecimal.valueOf(100)).setScale(4, RoundingMode.HALF_UP).doubleValue();
	}

	public boolean isZero() {
		return amount.compareTo(BigDecimal.ZERO) == 0;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object == null || getClass() != object.getClass())
			return false;
		Money money = (Money)object;
		return amount.compareTo(money.amount) == 0;
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
