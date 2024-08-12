package codesquad.fineants.domain.kis.client;

import static codesquad.fineants.domain.kis.properties.KisHeader.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import codesquad.fineants.domain.kis.aop.CheckedKisAccessToken;
import codesquad.fineants.domain.kis.domain.dto.response.KisClosingPrice;
import codesquad.fineants.domain.kis.domain.dto.response.KisDividend;
import codesquad.fineants.domain.kis.domain.dto.response.KisDividendWrapper;
import codesquad.fineants.domain.kis.domain.dto.response.KisIpoResponse;
import codesquad.fineants.domain.kis.domain.dto.response.KisSearchStockInfo;
import codesquad.fineants.domain.kis.properties.KisAccessTokenRequest;
import codesquad.fineants.domain.kis.properties.KisHeaderBuilder;
import codesquad.fineants.domain.kis.properties.KisProperties;
import codesquad.fineants.domain.kis.properties.KisQueryParam;
import codesquad.fineants.domain.kis.properties.KisQueryParamBuilder;
import codesquad.fineants.domain.kis.properties.KisTrIdProperties;
import codesquad.fineants.domain.kis.repository.KisAccessTokenRepository;
import codesquad.fineants.global.errors.exception.KisException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@Component
public class KisClient {
	private static final String APPLICATION_JSON_UTF8 = "application/json; charset=utf-8";
	private static final String KIS_CUSTOMER_TYPE = "P";
	private final KisProperties kisProperties;
	private final KisTrIdProperties kisTrIdProperties;
	private final WebClient realWebClient;
	private final KisAccessTokenRepository manager;

	public KisClient(KisProperties properties,
		KisTrIdProperties kisTrIdProperties,
		@Qualifier(value = "realKisWebClient") WebClient realWebClient,
		KisAccessTokenRepository manager) {
		this.kisProperties = properties;
		this.kisTrIdProperties = kisTrIdProperties;
		this.realWebClient = realWebClient;
		this.manager = manager;
	}

	// 액세스 토큰 발급
	public Mono<KisAccessToken> fetchAccessToken() {
		KisAccessTokenRequest request = KisAccessTokenRequest.create(kisProperties);
		return realWebClient
			.post()
			.uri(kisProperties.getTokenUrl())
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.retrieve()
			.onStatus(HttpStatusCode::isError, this::handleError)
			.bodyToMono(KisAccessToken.class)
			.retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5)))
			.log();
	}

	// 현재가 조회
	@CheckedKisAccessToken
	public Mono<KisCurrentPrice> fetchCurrentPrice(String tickerSymbol) {
		MultiValueMap<String, String> header = KisHeaderBuilder.builder()
			.add(AUTHORIZATION, manager.createAuthorization())
			.add(APP_KEY, kisProperties.getAppkey())
			.add(APP_SECRET, kisProperties.getSecretkey())
			.add(TR_ID, kisTrIdProperties.getCurrentPrice())
			.build();

		MultiValueMap<String, String> queryParam = KisQueryParamBuilder.builder()
			.add(KisQueryParam.CONDITION_MARKET_DIVISION_CODE, "J")
			.add(KisQueryParam.INPUT_STOCK_CODE, tickerSymbol)
			.build();
		
		return performGet(
			kisProperties.getCurrentPriceUrl(),
			header,
			queryParam,
			KisCurrentPrice.class
		);
	}

	// 직전 거래일의 종가 조회
	@CheckedKisAccessToken
	public Mono<KisClosingPrice> fetchClosingPrice(String tickerSymbol) {
		MultiValueMap<String, String> header = KisHeaderBuilder.builder()
			.add(AUTHORIZATION, manager.createAuthorization())
			.add(APP_KEY, kisProperties.getAppkey())
			.add(APP_SECRET, kisProperties.getSecretkey())
			.add(TR_ID, kisTrIdProperties.getClosingPrice())
			.build();

		MultiValueMap<String, String> queryParamMap = new LinkedMultiValueMap<>();
		queryParamMap.add("FID_COND_MRKT_DIV_CODE", "J");
		queryParamMap.add("FID_INPUT_ISCD", tickerSymbol);
		queryParamMap.add("FID_INPUT_DATE_1", LocalDate.now().minusDays(1L).toString());
		queryParamMap.add("FID_INPUT_DATE_2", LocalDate.now().minusDays(1L).toString());
		queryParamMap.add("FID_PERIOD_DIV_CODE", "D");
		queryParamMap.add("FID_ORG_ADJ_PRC", "0");

		return performGet(
			kisProperties.getClosingPriceUrl(),
			header,
			queryParamMap,
			KisClosingPrice.class
		);
	}

	/**
	 * tickerSymbol에 해당하는 종목의 배당 일정을 조회합니다.
	 * 해당 년도 범위에 대한 배당 일정을 조회합니다.
	 *
	 * @param tickerSymbol 종목의 단축코드
	 * @return 종목의 배당 일정 정보
	 */
	@CheckedKisAccessToken
	public Mono<KisDividendWrapper> fetchDividendThisYear(String tickerSymbol) {
		LocalDate today = LocalDate.now();
		// 해당 년도 첫일
		LocalDate from = today.with(TemporalAdjusters.firstDayOfYear());
		// 해당 년도 마지막일
		LocalDate to = today.with(TemporalAdjusters.lastDayOfYear());
		return fetchDividend(tickerSymbol, from, to);
	}

	private Mono<KisDividendWrapper> fetchDividend(String tickerSymbol, LocalDate from, LocalDate to) {
		MultiValueMap<String, String> header = KisHeaderBuilder.builder()
			.add(CONTENT_TYPE, APPLICATION_JSON_UTF8)
			.add(AUTHORIZATION, manager.createAuthorization())
			.add(APP_KEY, kisProperties.getAppkey())
			.add(APP_SECRET, kisProperties.getSecretkey())
			.add(TR_ID, kisTrIdProperties.getDividend())
			.add(CUSTOMER_TYPE, KIS_CUSTOMER_TYPE)
			.build();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		MultiValueMap<String, String> queryParamMap = new LinkedMultiValueMap<>();
		queryParamMap.add("HIGH_GB", Strings.EMPTY);
		queryParamMap.add("CTS", Strings.EMPTY);
		queryParamMap.add("GB1", "0");
		queryParamMap.add("F_DT", from.format(formatter));
		queryParamMap.add("T_DT", to.format(formatter));
		queryParamMap.add("SHT_CD", tickerSymbol);

		return performGet(
			kisProperties.getDividendUrl(),
			header,
			queryParamMap,
			KisDividendWrapper.class
		).retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5)));
	}

	@CheckedKisAccessToken
	public Mono<List<KisDividend>> fetchDividendAll(LocalDate from, LocalDate to) {
		MultiValueMap<String, String> header = KisHeaderBuilder.builder()
			.add(CONTENT_TYPE, APPLICATION_JSON_UTF8)
			.add(AUTHORIZATION, manager.createAuthorization())
			.add(APP_KEY, kisProperties.getAppkey())
			.add(APP_SECRET, kisProperties.getSecretkey())
			.add(TR_ID, kisTrIdProperties.getDividend())
			.add(CUSTOMER_TYPE, KIS_CUSTOMER_TYPE)
			.build();

		MultiValueMap<String, String> queryParamMap = new LinkedMultiValueMap<>();
		queryParamMap.add("HIGH_GB", Strings.EMPTY);
		queryParamMap.add("CTS", Strings.EMPTY);
		queryParamMap.add("GB1", "0");
		queryParamMap.add("F_DT", basicIso(from));
		queryParamMap.add("T_DT", basicIso(to));
		queryParamMap.add("SHT_CD", Strings.EMPTY);

		return performGet(
			kisProperties.getDividendUrl(),
			header,
			queryParamMap,
			KisDividendWrapper.class
		).map(KisDividendWrapper::getKisDividends);
	}

	@CheckedKisAccessToken
	public Mono<KisIpoResponse> fetchIpo(LocalDate from, LocalDate to) {
		MultiValueMap<String, String> header = KisHeaderBuilder.builder()
			.add(CONTENT_TYPE, APPLICATION_JSON_UTF8)
			.add(AUTHORIZATION, manager.createAuthorization())
			.add(APP_KEY, kisProperties.getAppkey())
			.add(APP_SECRET, kisProperties.getSecretkey())
			.add(TR_ID, kisTrIdProperties.getIpo())
			.add(CUSTOMER_TYPE, KIS_CUSTOMER_TYPE)
			.build();

		MultiValueMap<String, String> queryParamMap = new LinkedMultiValueMap<>();
		queryParamMap.add("SHT_CD", Strings.EMPTY);
		queryParamMap.add("T_DT", basicIso(to));
		queryParamMap.add("F_DT", basicIso(from));
		queryParamMap.add("CTS", Strings.EMPTY);

		return performGet(
			kisProperties.getIpoUrl(),
			header,
			queryParamMap,
			KisIpoResponse.class
		).retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5)));
	}

	@NotNull
	private String basicIso(LocalDate localDate) {
		return localDate.format(DateTimeFormatter.BASIC_ISO_DATE);
	}

	@CheckedKisAccessToken
	public Mono<KisSearchStockInfo> fetchSearchStockInfo(String tickerSymbol) {
		MultiValueMap<String, String> header = KisHeaderBuilder.builder()
			.add(CONTENT_TYPE, APPLICATION_JSON_UTF8)
			.add(AUTHORIZATION, manager.createAuthorization())
			.add(APP_KEY, kisProperties.getAppkey())
			.add(APP_SECRET, kisProperties.getSecretkey())
			.add(TR_ID, kisTrIdProperties.getSearchStockInfo())
			.add(CUSTOMER_TYPE, KIS_CUSTOMER_TYPE)
			.build();

		MultiValueMap<String, String> queryParamMap = new LinkedMultiValueMap<>();
		queryParamMap.add("PRDT_TYPE_CD", "300");
		queryParamMap.add("PDNO", tickerSymbol);

		return performGet(
			kisProperties.getSearchStockInfoUrl(),
			header,
			queryParamMap,
			KisSearchStockInfo.class);
	}

	private <T> Mono<T> performGet(String uri, MultiValueMap<String, String> headerMap,
		MultiValueMap<String, String> queryParamMap, Class<T> responseType) {
		return realWebClient
			.get()
			.uri(uriBuilder -> uriBuilder
				.path(uri)
				.queryParams(queryParamMap)
				.build())
			.headers(httpHeaders -> httpHeaders.addAll(headerMap))
			.retrieve()
			.onStatus(HttpStatusCode::isError, this::handleError)
			.bodyToMono(responseType);
	}

	private Mono<? extends Throwable> handleError(ClientResponse clientResponse) {
		return clientResponse.bodyToMono(String.class)
			.doOnNext(log::error)
			.flatMap(body -> Mono.error(() -> new KisException(body)));
	}
}
