package codesquad.fineants.spring.config;

import java.io.IOException;
import java.text.DecimalFormat;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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

		// 소수점 2자리 무조건 표시
		SimpleModule decimalFormatModule = new SimpleModule();
		decimalFormatModule.addSerializer(Double.class, new DecimalFormatSerializer("0.00"));
		objectMapper.registerModule(decimalFormatModule);
		return objectMapper;
	}

	private static class DecimalFormatSerializer extends JsonSerializer<Double> {
		private final DecimalFormat decimalFormat;

		public DecimalFormatSerializer(String pattern) {
			this.decimalFormat = new DecimalFormat(pattern);
		}

		@Override
		public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			gen.writeNumber(decimalFormat.format(value));
		}
	}

}
