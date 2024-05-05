package codesquad.fineants.spring.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Money;

@Configuration
public class JacksonConfig {

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		// Jackson Visibility 설정
		objectMapper.setVisibility(
			objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
				.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
				.withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withGetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withSetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
		);

		// jackson-datatype-jsr310 등록
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

		// Money 직렬화/역직렬화 설정
		objectMapper.registerModule(new SimpleModule().addSerializer(Money.class, new JsonSerializer<>() {
			@Override
			public void serialize(Money value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
				gen.writeNumber(value.toInteger());
			}
		}));
		objectMapper.registerModule(new SimpleModule().addDeserializer(Money.class, new JsonDeserializer<>() {
			@Override
			public Money deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
				return Money.won(p.getValueAsString());
			}
		}));

		// Count 직렬화/역직렬화 설정
		objectMapper.registerModule(new SimpleModule().addSerializer(Count.class, new JsonSerializer<Count>() {
			@Override
			public void serialize(Count value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
				gen.writeNumber(value.getValue());
			}
		}));
		objectMapper.registerModule(new SimpleModule().addDeserializer(Count.class, new JsonDeserializer<Count>() {
			@Override
			public Count deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
				return Count.from(p.getValueAsString());
			}
		}));

		return objectMapper;
	}
}
