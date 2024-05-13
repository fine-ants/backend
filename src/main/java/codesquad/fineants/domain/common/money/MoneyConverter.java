package codesquad.fineants.domain.common.money;

import java.math.BigDecimal;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class MoneyConverter implements AttributeConverter<Money, BigDecimal> {
	@Override
	public BigDecimal convertToDatabaseColumn(Money attribute) {
		return attribute.toDouble();
	}

	@Override
	public Money convertToEntityAttribute(BigDecimal dbData) {
		return Money.won(dbData);
	}
}
