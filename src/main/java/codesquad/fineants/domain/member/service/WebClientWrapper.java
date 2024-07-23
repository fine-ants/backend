package codesquad.fineants.domain.member.service;

import java.util.function.Function;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import codesquad.fineants.global.errors.errorcode.OauthErrorCode;
import codesquad.fineants.global.errors.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class WebClientWrapper {
	private final WebClient webClient;

	public WebClientWrapper() {
		this.webClient = WebClient.builder()
			.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
			.build();
	}

	public <T> Mono<T> get(String uri, MultiValueMap<String, String> headerMap, Class<T> responseTYpe) {
		return webClient.get()
			.uri(uri)
			.headers(header -> header.addAll(headerMap))
			.exchangeToMono(getClientResponseMonoFunction(responseTYpe));
	}

	public <T> T get(String uri, MultiValueMap<String, String> headerMap,
		ParameterizedTypeReference<T> reference) {
		return webClient.get()
			.uri(uri)
			.headers(header -> header.addAll(headerMap))
			.exchangeToMono(getClientResponseMonoFunction(reference))
			.block();
	}

	public <T> T post(String uri, MultiValueMap<String, String> headerMap,
		MultiValueMap<String, String> bodyMap, Class<T> responseTYpe) {
		return webClient.post()
			.uri(uri)
			.headers(header -> header.addAll(headerMap))
			.bodyValue(bodyMap)
			.exchangeToMono(getClientResponseMonoFunction(responseTYpe))
			.block();
	}

	private <T> Function<ClientResponse, Mono<T>> getClientResponseMonoFunction(Class<T> responseType) {
		return clientResponse -> {
			log.info("statusCode : {}", clientResponse.statusCode());
			if (clientResponse.statusCode().is4xxClientError() || clientResponse.statusCode().is5xxServerError()) {
				return clientResponse.bodyToMono(String.class).handle((body, sink) -> {
					log.info("responseBody : {}", body);
					sink.error(new BadRequestException(OauthErrorCode.FAIL_REQUEST, body));
				});
			}
			return clientResponse.bodyToMono(responseType);
		};
	}

	private <T> Function<ClientResponse, Mono<T>> getClientResponseMonoFunction(
		ParameterizedTypeReference<T> reference) {
		return clientResponse -> {
			log.info("statusCode : {}", clientResponse.statusCode());
			if (clientResponse.statusCode().is4xxClientError() || clientResponse.statusCode().is5xxServerError()) {
				return clientResponse.bodyToMono(String.class).handle((body, sink) -> {
					log.info("responseBody : {}", body);
					sink.error(new IllegalStateException(body));
				});
			}
			return clientResponse.bodyToMono(reference);
		};
	}
}
