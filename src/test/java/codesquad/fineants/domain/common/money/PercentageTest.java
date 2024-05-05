package codesquad.fineants.domain.common.money;

import java.text.DecimalFormat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PercentageTest {

	@DisplayName("실수를 대상으로 십진수 포맷한다")
	@Test
	void format() {
		// given
		DecimalFormat format = new DecimalFormat("#.####");
		// when
		String actual = format.format(Double.valueOf(12.1234));
		String actual2 = format.format(Double.valueOf(12.1200));
		String actual3 = format.format(Double.valueOf(12.12345));
		// then
		Assertions.assertThat(actual).isEqualTo("12.1234");
		Assertions.assertThat(actual2).isEqualTo("12.12");
		Assertions.assertThat(actual3).isEqualTo("12.1235");
	}

}
