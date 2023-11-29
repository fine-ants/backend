package codesquad.fineants.spring.converter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

public class SseEventStreamMessageConverter extends MappingJackson2HttpMessageConverter {
	public SseEventStreamMessageConverter() {
		List<MediaType> mediaTypes = new ArrayList<>();
		mediaTypes.add(MediaType.TEXT_EVENT_STREAM);
		setSupportedMediaTypes(mediaTypes);
	}
}
