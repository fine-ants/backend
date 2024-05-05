package codesquad.fineants.domain.common.money;

import java.text.DecimalFormat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PercentageTest {

	@DisplayName("백분율로 변환한다")
	@CsvSource(value = {"0.121234,12.1234", "0.1212345,12.1234", "0.1212356,12.1236", "0.001212345,0.1212",
		"0.12,12.0"})
	@ParameterizedTest
	void toPercentage(String stringValue, String expected) {
		// given
		double value = Double.parseDouble(stringValue);
		Percentage percentage = Percentage.from(value);
		// when
		Double actual = percentage.toPercentage();
		// then
		Assertions.assertThat(actual.toString()).isEqualTo(expected);
	}

	@DisplayName("실수를 대상으로 십진수 포맷한다")
	@CsvSource(value = {"12.1234,12.1234", "12.1200,12.12", "12.12345,12.1235", "12.1235,12.1235", "12.10,12.10"})
	@ParameterizedTest
	void format(String value, String expected) {
		// given
		DecimalFormat format = new DecimalFormat("#.00##");
		// when
		String actual = format.format(Double.valueOf(value));
		// then
		Assertions.assertThat(actual).isEqualTo(expected);
	}

}
