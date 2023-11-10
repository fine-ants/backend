package codesquad.fineants.spring.util;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.spring.api.errors.errorcode.ObjectMapperErrorCode;
import codesquad.fineants.spring.api.errors.exception.ServerInternalException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ObjectMapperUtil {
	private final ObjectMapper objectMapper;

	public <T> String serialize(T obj) {
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new ServerInternalException(ObjectMapperErrorCode.FAIL_DESERIALIZE);
		}
	}

	public <T> T deserialize(String json, Class<T> returnType) {
		try {
			return objectMapper.readValue(json, returnType);
		} catch (JsonProcessingException e) {
			throw new ServerInternalException(ObjectMapperErrorCode.FAIL_DESERIALIZE);
		}
	}
}
