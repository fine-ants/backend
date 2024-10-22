package co.fineants.api.global.config.jackson;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.global.config.jackson.json.CountJsonDeserializer;
import co.fineants.api.global.config.jackson.json.CountJsonSerializer;
import co.fineants.api.global.config.jackson.json.MoneyJsonDeserializer;
import co.fineants.api.global.config.jackson.json.MoneyJsonSerializer;

@Configuration
public class JacksonConfig {

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		VisibilityChecker<?> checker = objectMapper.getSerializationConfig().getDefaultVisibilityChecker();
		objectMapper.setVisibility(visibilityChecker(checker));

		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		objectMapper.registerModule(commonModule());
		return objectMapper;
	}

	private VisibilityChecker<?> visibilityChecker(VisibilityChecker<?> checker) {
		checker.withFieldVisibility(JsonAutoDetect.Visibility.ANY);
		checker.withGetterVisibility(JsonAutoDetect.Visibility.NONE);
		checker.withIsGetterVisibility(JsonAutoDetect.Visibility.NONE);
		checker.withSetterVisibility(JsonAutoDetect.Visibility.NONE);
		checker.withCreatorVisibility(JsonAutoDetect.Visibility.NONE);
		return checker;
	}

	@Bean
	public SimpleModule commonModule() {
		SimpleModule commonModule = new SimpleModule();
		commonModule.addSerializer(Money.class, moneyJsonSerializer());
		commonModule.addDeserializer(Money.class, moneyJsonDeserializer());
		commonModule.addSerializer(Count.class, countJsonSerializer());
		commonModule.addDeserializer(Count.class, countJsonDeserializer());
		return commonModule;
	}

	@Bean
	public JsonSerializer<Money> moneyJsonSerializer() {
		return new MoneyJsonSerializer();
	}

	@Bean
	public JsonDeserializer<Money> moneyJsonDeserializer() {
		return new MoneyJsonDeserializer();
	}

	@Bean
	public JsonSerializer<Count> countJsonSerializer() {
		return new CountJsonSerializer();
	}

	@Bean
	public JsonDeserializer<Count> countJsonDeserializer() {
		return new CountJsonDeserializer();
	}
}
