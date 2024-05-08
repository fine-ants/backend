package codesquad.fineants.domain.exchange_rate.domain.entity;

import java.text.DecimalFormat;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;

import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.common.money.PercentageConverter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "code")
public class ExchangeRate {

	@Id
	private String code;

	@Column(name = "rate", nullable = false, precision = 19)
	@Convert(converter = PercentageConverter.class)
	private Percentage rate;

	public static ExchangeRate zero(String code) {
		return of(code, 0.0);
	}

	public static ExchangeRate of(String code, Double rate) {
		return new ExchangeRate(code, Percentage.from(rate));
	}

	public String parse() {
		DecimalFormat decimalFormat = new DecimalFormat("0.##########");
		return String.format("%s:%s", code, rate.toDoubleValue(decimalFormat));
	}
}
