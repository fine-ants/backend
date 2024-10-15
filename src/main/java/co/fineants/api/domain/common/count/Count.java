package co.fineants.api.domain.common.count;

import java.math.BigInteger;

import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "value")
public final class Count implements Comparable<Count> {
	private static final Count ZERO = new Count();
	private final BigInteger value;

	private Count() {
		this.value = BigInteger.ZERO;
	}

	// TODO: 캐싱 인스턴스 반환
	public static Count zero() {
		return ZERO;
	}

	public static Count from(BigInteger value) {
		return new Count(value);
	}

	/**
	 * 문자열의 수치값을 Count로 변환해서 반환.
	 * <p>
	 * 인수로 전달받은 인수 value값이 long 범위를 벗어날 수 있기 때문에 new BigInteger를 사용합니다.
	 * </p>
	 * @param value 문자열 타입의 값
	 * @return 개수 객체
	 */
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

	public Expression multiply(Money money) {
		return money.times(value.intValue());
	}

	public boolean isZero() {
		return ZERO.value.equals(value);
	}

	public Expression division(Money numerator) {
		return numerator.divide(value);
	}

	public int intValue() {
		return value.intValue();
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
