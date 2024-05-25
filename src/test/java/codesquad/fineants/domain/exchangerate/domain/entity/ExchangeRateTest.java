package codesquad.fineants.domain.exchangerate.domain.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExchangeRateTest {

	@DisplayName("파싱 테스트")
	@Test
	void testParse() {
		// given
		ExchangeRate rate = ExchangeRate.of("USD", 0.0007322, false);
		// when
		String actual = rate.parse();
		// then
		Assertions.assertThat(actual).isEqualTo("USD:0.0007322");
	}

}
