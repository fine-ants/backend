package co.fineants.api.global.config.jackson.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import co.fineants.api.domain.common.money.Money;

public class MoneyJsonSerializer extends JsonSerializer<Money> {

	/**
	 * Method that can be called to ask implementation to serialize
	 * values of type this serializer handles.
	 *
	 * @param value       Value to serialize; can <b>not</b> be null.
	 * @param gen         Generator used to output resulting Json content
	 * @param serializers Provider that can be used to get serializers for
	 *                    serializing Objects value contains, if any.
	 */
	@Override
	public void serialize(Money value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeNumber(value.toInteger());
	}
}
