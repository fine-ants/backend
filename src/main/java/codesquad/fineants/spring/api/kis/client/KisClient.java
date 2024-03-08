package codesquad.fineants.spring.api.kis.client;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import codesquad.fineants.spring.api.errors.exception.KisException;
import codesquad.fineants.spring.api.kis.properties.OauthKisProperties;
import codesquad.fineants.spring.api.kis.response.LastDayClosingPriceResponse;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@Component
public class KisClient {

	private final WebClient webClient;
	private final OauthKisProperties oauthKisProperties;

	public KisClient(OauthKisProperties properties,
		@Qualifier(value = "kisWebClient") WebClient webClient) {
		this.webClient = webClient;
		this.oauthKisProperties = properties;
	}

	// 액세스 토큰 발급
	public Mono<KisAccessToken> accessToken(String uri) {
		Map<String, String> requestBodyMap = new HashMap<>();
		requestBodyMap.put("grant_type", "client_credentials");
		requestBodyMap.put("appkey", oauthKisProperties.getAppkey());
		requestBodyMap.put("appsecret", oauthKisProperties.getSecretkey());

		return webClient
			.post()
			.uri(uri)
			.bodyValue(requestBodyMap)
			.retrieve()
			.onStatus(HttpStatus::isError, this::handleError)
			.bodyToMono(KisAccessToken.class)
			.retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofMinutes(1)))
			.log();
	}

	// 현재가 조회
	public long fetchCurrentPrice(String tickerSymbol, String authorization) {
		MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
		headerMap.add("authorization", authorization);
		headerMap.add("appkey", oauthKisProperties.getAppkey());
		headerMap.add("appsecret", oauthKisProperties.getSecretkey());
		headerMap.add("tr_id", "FHKST01010100");

		MultiValueMap<String, String> queryParamMap = new LinkedMultiValueMap<>();
		queryParamMap.add("fid_cond_mrkt_div_code", "J");
		queryParamMap.add("fid_input_iscd", tickerSymbol);

		Map<String, Object> responseMap = getPerform(oauthKisProperties.getCurrentPriceURI(), headerMap, queryParamMap);
		if (!responseMap.containsKey("output")) {
			throw new KisException((String)responseMap.get("msg1"));
		}
		Map<String, String> output = (Map<String, String>)responseMap.get("output");
		return Long.parseLong(output.get("stck_prpr"));
	}

	// 직전 거래일의 종가 조회
	public LastDayClosingPriceResponse readLastDayClosingPrice(String tickerSymbol, String authorization) {
		MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
		headerMap.add("authorization", authorization);
		headerMap.add("appkey", oauthKisProperties.getAppkey());
		headerMap.add("appsecret", oauthKisProperties.getSecretkey());
		headerMap.add("tr_id", "FHKST03010100");

		MultiValueMap<String, String> queryParamMap = new LinkedMultiValueMap<>();
		queryParamMap.add("FID_COND_MRKT_DIV_CODE", "J");
		queryParamMap.add("FID_INPUT_ISCD", tickerSymbol);
		queryParamMap.add("FID_INPUT_DATE_1", LocalDate.now().minusDays(1L).toString());
		queryParamMap.add("FID_INPUT_DATE_2", LocalDate.now().minusDays(1L).toString());
		queryParamMap.add("FID_PERIOD_DIV_CODE", "D");
		queryParamMap.add("FID_ORG_ADJ_PRC", "0");

		Map<String, Object> responseMap = getPerform(oauthKisProperties.getLastDayClosingPriceURI(), headerMap,
			queryParamMap);
		if (!responseMap.containsKey("output1")) {
			throw new KisException((String)responseMap.get("msg1"));
		}
		Map<String, String> output = (Map<String, String>)responseMap.get("output1");
		return LastDayClosingPriceResponse.create(tickerSymbol, Long.parseLong(output.get("stck_prdy_clpr")));
	}

	private Map<String, Object> getPerform(String uri, MultiValueMap<String, String> headerMap,
		MultiValueMap<String, String> queryParamMap) {
		return webClient
			.get()
			.uri(uriBuilder -> uriBuilder
				.path(uri)
				.queryParams(queryParamMap)
				.build())
			.headers(httpHeaders -> httpHeaders.addAll(headerMap))
			.retrieve()
			.onStatus(HttpStatus::is4xxClientError, this::handleError)
			.onStatus(HttpStatus::is5xxServerError, this::handleError)
			.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
			})
			.retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5)))
			.block();
	}

	private Mono<? extends Throwable> handleError(ClientResponse clientResponse) {
		return clientResponse.bodyToMono(String.class)
			.doOnNext(log::info)
			.flatMap(body -> Mono.error(() -> new KisException(body)));
	}
}
