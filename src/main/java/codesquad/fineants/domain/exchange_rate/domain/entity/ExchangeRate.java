package codesquad.fineants.domain.exchange_rate.domain.entity;

import java.text.DecimalFormat;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.common.money.PercentageConverter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "code", callSuper = false)
@Getter
public class ExchangeRate extends BaseEntity {

	@Id
	private String code;

	@Column(name = "rate", nullable = false, precision = 19)
	@Convert(converter = PercentageConverter.class)
	private Percentage rate;

	private Boolean base;

	public static ExchangeRate zero(String code, Boolean base) {
		return of(code, 0.0, base);
	}

	public static ExchangeRate none(String code, Double rate) {
		return of(code, rate, false);
	}

	public static ExchangeRate base(String code) {
		return of(code, 1.0, true);
	}

	public static ExchangeRate of(String code, Double rate, Boolean base) {
		return new ExchangeRate(code, Percentage.from(rate), base);
	}

	public String parse() {
		DecimalFormat decimalFormat = new DecimalFormat("0.##########");
		return String.format("%s:%s", code, rate.toDoubleValue(decimalFormat));
	}

	public void changeRate(Double value) {
		this.rate = Percentage.from(value);
	}

	public Boolean isBase() {
		return base;
	}

	public Boolean equalCode(String code) {
		return this.code.equals(code);
	}

	public void changeBase(boolean base) {
		this.base = base;
	}
}
