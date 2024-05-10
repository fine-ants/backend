package codesquad.fineants.domain.common.money;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class PercentageConverter implements AttributeConverter<Percentage, Double> {

	@Override
	public Double convertToDatabaseColumn(Percentage attribute) {
		return attribute.toDoubleValue();
	}

	@Override
	public Percentage convertToEntityAttribute(Double dbData) {
		return Percentage.from(dbData);
	}
}
