package co.fineants.api.domain.exchangerate.domain.entity;

import java.text.DecimalFormat;

import co.fineants.api.domain.BaseEntity;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.common.money.PercentageConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "code", callSuper = false)
@Getter
@ToString
public class ExchangeRate extends BaseEntity {

	@Id
	private String code;

	@Column(name = "rate", nullable = false, precision = 19, columnDefinition = "DOUBLE")
	@Convert(converter = PercentageConverter.class)
	private Percentage rate;

	@Column(name = "base", nullable = false)
	private Boolean base;

	public static ExchangeRate zero(String code, Boolean base) {
		return of(code, 0.0, base);
	}

	public static ExchangeRate noneBase(String code, Double rate) {
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
