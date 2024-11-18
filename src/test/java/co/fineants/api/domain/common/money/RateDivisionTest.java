package co.fineants.api.domain.common.money;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RateDivisionTest {

	@DisplayName("10000원을 0원으로 나누면 0원이 된다")
	@Test
	void reduce_givenMoney_whenDivisorIsZero_thenReturnZeroWon() {
		// given
		Money division = Money.won(10000);
		Money divisor = Money.zero();
		RateDivision expression = division.divide(divisor);
		// when
		Expression actual = expression.reduce(Bank.getInstance(), Currency.KRW);
		// then
		Assertions.assertThat(actual).isEqualByComparingTo(Money.zero());
	}
}
