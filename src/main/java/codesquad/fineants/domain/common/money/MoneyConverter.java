package codesquad.fineants.domain.common.money;

import java.math.BigDecimal;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class MoneyConverter implements AttributeConverter<Money, BigDecimal> {
	@Override
	public BigDecimal convertToDatabaseColumn(Money attribute) {
		return attribute.getAmount();
	}

	@Override
	public Money convertToEntityAttribute(BigDecimal dbData) {
		return Money.from(dbData);
	}
}
