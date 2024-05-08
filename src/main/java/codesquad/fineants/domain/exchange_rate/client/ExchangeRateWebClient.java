package codesquad.fineants.domain.exchange_rate.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import codesquad.fineants.domain.exchange_rate.domain.dto.response.ExchangeRateFetchResponse;
import codesquad.fineants.domain.member.service.WebClientWrapper;

@Component
public class ExchangeRateWebClient {

	private final WebClientWrapper webClient;
	private final String key;

	public ExchangeRateWebClient(WebClientWrapper webClient, @Value("${rapid.exchange-rate.key}") String key) {
		this.webClient = webClient;
		this.key = key;
	}

	public Double fetchRateBy(String code) {
		String uri = "https://exchange-rate-api1.p.rapidapi.com/latest?base=KRW";
		MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
		header.add("X-RapidAPI-Key", key);
		header.add("X-RapidAPI-Host", "exchange-rate-api1.p.rapidapi.com");
		ExchangeRateFetchResponse fetchResponse = webClient.get(uri, header, ExchangeRateFetchResponse.class);
		return fetchResponse.getBy(code);
	}
}
