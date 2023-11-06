package codesquad.fineants.spring.api.kis.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import codesquad.fineants.spring.api.errors.exception.KisException;
import codesquad.fineants.spring.api.kis.properties.OauthKisProperties;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class KisClient {
	public static final String baseUrl = "https://openapivts.koreainvestment.com:29443";
	private static final String approvalURI = "https://openapivts.koreainvestment.com:29443/oauth2/Approval";
	private static final String tokenPURI = "https://openapivts.koreainvestment.com:29443/oauth2/tokenP";
	public static final String currentPrice = "/uapi/domestic-stock/v1/quotations/inquire-price";

	private final WebClient.Builder webClient;

	private final String appkey;
	private final String secretkey;

	public KisClient(OauthKisProperties properties) {
		this.webClient = WebClient.builder().baseUrl(baseUrl);
		this.appkey = properties.getAppkey();
		this.secretkey = properties.getSecretkey();
	}

	private Map<String, Object> postPerform(String uri, MultiValueMap<String, String> headerMap,
		Map<String, String> requestBodyMap) {
		WebClient.ResponseSpec responseSpec = webClient.build()
			.post()
			.uri(uri)
			.headers(header -> header.addAll(headerMap))
			.bodyValue(requestBodyMap)
			.retrieve();
		return handleClientResponse(responseSpec);
	}

	private Map<String, Object> handleClientResponse(WebClient.ResponseSpec responseSpec) {
		WebClient.ResponseSpec response = responseSpec.onStatus(HttpStatus::is4xxClientError,
				this::handleError)
			.onStatus(HttpStatus::is5xxServerError,
				this::handleError);
		return response.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
		}).block();
	}

	private Mono<? extends Throwable> handleError(ClientResponse clientResponse) {
		return clientResponse.bodyToMono(String.class)
			.doOnNext(body -> log.info("responseBody : {}", body))
			.flatMap(body -> Mono.error(() -> new KisException(body)));
	}

	public Map<String, Object> accessToken() {
		Map<String, String> requestBodyMap = new HashMap<>();
		requestBodyMap.put("grant_type", "client_credentials");
		requestBodyMap.put("appkey", appkey);
		requestBodyMap.put("appsecret", secretkey);

		return postPerform(
			tokenPURI,
			new LinkedMultiValueMap<>(),
			requestBodyMap
		);
	}

	public long readRealTimeCurrentPrice(String tickerSymbol, String authorization) {
		MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
		log.info("authorization : {}", authorization);
		headerMap.add("authorization", authorization);
		headerMap.add("appkey", appkey);
		headerMap.add("appsecret", secretkey);
		headerMap.add("tr_id", "FHKST01010100");

		MultiValueMap<String, String> queryParamMap = new LinkedMultiValueMap<>();
		queryParamMap.add("fid_cond_mrkt_div_code", "J");
		queryParamMap.add("fid_input_iscd", tickerSymbol);

		Map<String, Object> responseMap = getPerform(currentPrice, headerMap, queryParamMap);
		Map<String, String> output = (Map<String, String>)responseMap.get("output");
		return Long.parseLong(output.get("stck_prpr"));
	}

	private Map<String, Object> getPerform(String uri, MultiValueMap<String, String> headerMap,
		MultiValueMap<String, String> queryParamMap) {
		WebClient.ResponseSpec responseSpec = webClient.build()
			.get()
			.uri(uriBuilder -> uriBuilder
				.path(uri)
				.queryParams(queryParamMap)
				.build())
			.headers(httpHeaders -> httpHeaders.addAll(headerMap))
			.retrieve();

		return handleClientResponse(responseSpec);
	}

}
