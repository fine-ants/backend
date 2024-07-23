package codesquad.fineants.domain.common.count;

import java.math.BigInteger;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CountConverter implements AttributeConverter<Count, BigInteger> {
	@Override
	public BigInteger convertToDatabaseColumn(Count count) {
		return count.getValue();
	}

	@Override
	public Count convertToEntityAttribute(BigInteger dbData) {
		return Count.from(dbData);
	}
}
