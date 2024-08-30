package co.fineants.api.domain.common.money;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

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
