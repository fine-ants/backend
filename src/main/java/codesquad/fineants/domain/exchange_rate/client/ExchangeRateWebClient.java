package codesquad.fineants.domain.exchange_rate.client;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import codesquad.fineants.domain.exchange_rate.domain.dto.response.ExchangeRateFetchResponse;
import codesquad.fineants.domain.member.service.WebClientWrapper;
import codesquad.fineants.global.errors.errorcode.ExchangeRateErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;

@Component
public class ExchangeRateWebClient {

	private static final Duration TIMEOUT = Duration.ofSeconds(5);

	private final WebClientWrapper webClient;
	private final String key;

	public ExchangeRateWebClient(WebClientWrapper webClient, @Value("${rapid.exchange-rate.key}") String key) {
		this.webClient = webClient;
		this.key = key;
	}

	public Double fetchRateBy(String code, String base) {
		String uri = "https://exchange-rate-api1.p.rapidapi.com/latest?base=" + base.toUpperCase();
		MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
		header.add("X-RapidAPI-Key", key);
		header.add("X-RapidAPI-Host", "exchange-rate-api1.p.rapidapi.com");
		return webClient.get(uri, header, ExchangeRateFetchResponse.class)
			.filter(response -> response.containsBy(code))
			.map(response -> response.getBy(code))
			.blockOptional(TIMEOUT)
			.orElseThrow(() -> new FineAntsException(ExchangeRateErrorCode.NOT_EXIST_EXCHANGE_RATE));
	}

	public Map<String, Double> fetchRates() {
		String uri = "https://exchange-rate-api1.p.rapidapi.com/latest?base=KRW";
		MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
		header.add("X-RapidAPI-Key", key);
		header.add("X-RapidAPI-Host", "exchange-rate-api1.p.rapidapi.com");
		return webClient.get(uri, header, ExchangeRateFetchResponse.class)
			.map(ExchangeRateFetchResponse::getRates)
			.blockOptional(TIMEOUT)
			.orElse(Collections.emptyMap());
	}
}
