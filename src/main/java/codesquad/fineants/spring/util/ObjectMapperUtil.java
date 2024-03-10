package codesquad.fineants.spring.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import codesquad.fineants.spring.api.common.errors.errorcode.ObjectMapperErrorCode;
import codesquad.fineants.spring.api.common.errors.exception.ServerInternalException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectMapperUtil {
	private static final ObjectMapper objectMapper;

	static {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule()); // JavaTimeModule 등록
		// 여기에 다른 설정이 필요한 경우 추가할 수 있습니다.
	}

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
