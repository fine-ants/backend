package codesquad.fineants.domain.common.count;

import java.math.BigInteger;
import java.util.Objects;

import codesquad.fineants.domain.common.money.Money;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Count implements Comparable<Count> {
	private final BigInteger value;

	public Count() {
		this.value = BigInteger.ZERO;
	}

	public static Count zero() {
		return new Count(BigInteger.ZERO);
	}

	public static Count from(BigInteger value) {
		return new Count(value);
	}

	public static Count from(String value) {
		return new Count(new BigInteger(value));
	}

	public static Count from(Long value) {
		return new Count(BigInteger.valueOf(value));
	}

	public static Count from(Integer value) {
		return new Count(BigInteger.valueOf(value));
	}

	public Count add(Count count) {
		return new Count(value.add(count.value));
	}

	public Money multiply(Money money) {
		return money.multiply(value);
	}

	public boolean isZero() {
		return value.compareTo(BigInteger.ZERO) == 0;
	}

	public Money division(Money numerator) {
		return numerator.divide(value);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object == null || getClass() != object.getClass())
			return false;
		Count count = (Count)object;
		return value.compareTo(count.value) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public int compareTo(Count count) {
		return this.value.compareTo(count.value);
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
