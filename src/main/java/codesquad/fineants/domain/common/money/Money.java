package codesquad.fineants.domain.common.money;

import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class Money implements Comparable<Money> {
	private final BigDecimal amount;

	private Money() {
		this.amount = BigDecimal.ZERO;
	}

	public static Money from(BigDecimal amount) {
		return new Money(amount);
	}

	public static Money from(String amount) {
		return new Money(new BigDecimal(amount));
	}

	public static Money from(long amount) {
		return new Money(BigDecimal.valueOf(amount));
	}

	public static Money from(double amount) {
		return new Money(BigDecimal.valueOf(amount));
	}

	@Override
	public int compareTo(Money m) {
		return this.amount.compareTo(m.amount);
	}
}
