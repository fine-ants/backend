package co.fineants.api.domain.common.money;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.ToString;

@JsonSerialize(using = Percentage.PercentageSerializer.class)
@ToString
public class Percentage implements Comparable<Percentage> {
	private static final BigDecimal HUNDRED = new BigDecimal(100);
	private static final int PERCENTAGE_SCALE = 4;
	private final BigDecimal amount;

	private Percentage(BigDecimal amount) {
		this.amount = amount;
	}

	public static Percentage zero() {
		return new Percentage(BigDecimal.ZERO);
	}

	public static Percentage from(double value) {
		return from(BigDecimal.valueOf(value));
	}

	public static Percentage from(BigDecimal value) {
		return new Percentage(value);
	}

	public Double toPercentage() {
		return amount.multiply(HUNDRED)
			.setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP)
			.doubleValue();
	}

	public double toDoubleValue() {
		return amount.doubleValue();
	}

	public String toDoubleValue(DecimalFormat decimalFormat) {
		return decimalFormat.format(amount);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || getClass() != object.getClass()) {
			return false;
		}
		Percentage that = (Percentage)object;
		return compareTo(that) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(amount);
	}

	@Override
	public int compareTo(@NotNull Percentage percentage) {
		BigDecimal p1 = amount.setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);
		BigDecimal p2 = percentage.amount.setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);
		return p1.compareTo(p2);
	}

	static class PercentageSerializer extends JsonSerializer<Percentage> {

		private final DecimalFormat decimalFormat = new DecimalFormat("0.00##");

		@Override
		public void serialize(Percentage value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			Double percentage = value.toPercentage();
			gen.writeNumber(decimalFormat.format(percentage));
		}
	}

	public static class PercentageDoubleSerializer extends JsonSerializer<Percentage> {
		@Override
		public void serialize(Percentage value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			gen.writeNumber(value.amount.setScale(10, RoundingMode.HALF_UP));
		}
	}
}
