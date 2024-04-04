package codesquad.fineants.spring.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.spring.api.common.errors.errorcode.ObjectMapperErrorCode;
import codesquad.fineants.spring.api.common.errors.exception.ServerInternalException;
import codesquad.fineants.spring.config.JacksonConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectMapperUtil {
	private static final ObjectMapper objectMapper = new JacksonConfig().objectMapper();

	public static <T> String serialize(T obj) {
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			log.error("Serialization failed: {}", e.getMessage());
			throw new ServerInternalException(ObjectMapperErrorCode.FAIL_SERIALIZE);
		}
	}

	public static <T> T deserialize(String json, Class<T> returnType) {
		try {
			return objectMapper.readValue(json, returnType);
		} catch (JsonProcessingException e) {
			log.error("Deserialization failed: {}", e.getMessage());
			throw new ServerInternalException(ObjectMapperErrorCode.FAIL_DESERIALIZE);
		}
	}
}
