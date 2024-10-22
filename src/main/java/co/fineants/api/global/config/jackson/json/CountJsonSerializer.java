package co.fineants.api.global.config.jackson.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import co.fineants.api.domain.common.count.Count;

public class CountJsonSerializer extends JsonSerializer<Count> {
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
	public void serialize(Count value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeNumber(value.getValue());
	}
}
