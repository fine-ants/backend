package co.fineants.api.domain.common.money;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RateDivisionTest {

	@DisplayName("Expression을 0으로 나누면 0원을 반환한다")
	@Test
	void reduce_givenMoney_whenDivisorIsZero_thenThrowException() {
		// given
		Money division = Money.won(10000);
		Money divisor = Money.zero();
		Expression expression = RateDivision.of(division, divisor);
		// when
		Expression actual = expression.reduce(Bank.getInstance(), Currency.KRW);
		// then
		Assertions.assertThat(actual).isEqualByComparingTo(Money.ZERO);
	}

}
