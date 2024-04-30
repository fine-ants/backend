package codesquad.fineants.domain.common.money;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = Percentage.PercentageSerializer.class)
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
		return from(new BigDecimal(value));
	}

	public static Percentage from(BigDecimal value) {
		return new Percentage(value);
	}

	public double toPercentage() {
		return amount.multiply(HUNDRED)
			.setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP)
			.doubleValue();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object == null || getClass() != object.getClass())
			return false;
		Percentage that = (Percentage)object;
		return compareTo(that) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(amount);
	}

	@Override
	public int compareTo(@NotNull Percentage o) {
		BigDecimal a = amount.setScale(4, RoundingMode.HALF_UP);
		BigDecimal b = o.amount.setScale(4, RoundingMode.HALF_UP);
		return a.compareTo(b);
	}

	static class PercentageSerializer extends JsonSerializer<Percentage> {
		@Override
		public void serialize(Percentage value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			gen.writeNumber(value.toPercentage());
		}
	}
}
