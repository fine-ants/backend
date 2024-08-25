package codesquad.fineants.domain.kis.client;

import static codesquad.fineants.domain.kis.properties.KisHeader.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import codesquad.fineants.domain.kis.domain.dto.response.KisClosingPrice;
import codesquad.fineants.domain.kis.domain.dto.response.KisDividend;
import codesquad.fineants.domain.kis.domain.dto.response.KisDividendWrapper;
import codesquad.fineants.domain.kis.domain.dto.response.KisErrorResponse;
import codesquad.fineants.domain.kis.domain.dto.response.KisIpoResponse;
import codesquad.fineants.domain.kis.domain.dto.response.KisSearchStockInfo;
import codesquad.fineants.domain.kis.properties.KisAccessTokenRequest;
import codesquad.fineants.domain.kis.properties.KisHeaderBuilder;
import codesquad.fineants.domain.kis.properties.KisProperties;
import codesquad.fineants.domain.kis.properties.KisQueryParam;
import codesquad.fineants.domain.kis.properties.KisQueryParamBuilder;
import codesquad.fineants.domain.kis.properties.KisTrIdProperties;
import codesquad.fineants.domain.kis.properties.kiscodevalue.imple.CustomerType;
import codesquad.fineants.domain.kis.properties.kiscodevalue.imple.FidCondMrktDivCode;
import codesquad.fineants.domain.kis.properties.kiscodevalue.imple.FidOrgAdjPrc;
import codesquad.fineants.domain.kis.properties.kiscodevalue.imple.FidPeriodDivCode;
import codesquad.fineants.domain.kis.properties.kiscodevalue.imple.GB1;
import codesquad.fineants.domain.kis.properties.kiscodevalue.imple.PrdtTypeCd;
import codesquad.fineants.domain.kis.repository.KisAccessTokenRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@Component
public class KisClient {
	private static final String APPLICATION_JSON_UTF8 = "application/json; charset=utf-8";
	private final KisProperties kisProperties;
	private final KisTrIdProperties kisTrIdProperties;
	private final WebClient webClient;
	private final KisAccessTokenRepository manager;

	public KisClient(KisProperties properties,
		KisTrIdProperties kisTrIdProperties,
		WebClient webClient,
		KisAccessTokenRepository manager) {
		this.kisProperties = properties;
		this.kisTrIdProperties = kisTrIdProperties;
		this.webClient = webClient;
		this.manager = manager;
	}

	// 액세스 토큰 발급
	public Mono<KisAccessToken> fetchAccessToken() {
		KisAccessTokenRequest request = KisAccessTokenRequest.create(kisProperties);
		return webClient
			.post()
			.uri(kisProperties.getTokenUrl())
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			.retrieve()
			.onStatus(HttpStatusCode::isError, this::handleError)
			.bodyToMono(KisAccessToken.class)
			.retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofMinutes(1)))
			.log();
	}

	// 현재가 조회
	public Mono<KisCurrentPrice> fetchCurrentPrice(String tickerSymbol) {
		MultiValueMap<String, String> header = KisHeaderBuilder.builder()
			.add(AUTHORIZATION, manager.createAuthorization())
			.add(APP_KEY, kisProperties.getAppkey())
			.add(APP_SECRET, kisProperties.getSecretkey())
			.add(TR_ID, kisTrIdProperties.getCurrentPrice())
			.build();
		MultiValueMap<String, String> queryParam = KisQueryParamBuilder.builder()
			.add(KisQueryParam.FID_COND_MRKT_DIV_CODE, FidCondMrktDivCode.STOCK)
			.add(KisQueryParam.FID_INPUT_ISCD, tickerSymbol)
			.build();

		return performGet(
			kisProperties.getCurrentPriceUrl(),
			header,
			queryParam,
			KisCurrentPrice.class
		);
	}

	// 직전 거래일의 종가 조회
	public Mono<KisClosingPrice> fetchClosingPrice(String tickerSymbol) {
		MultiValueMap<String, String> header = KisHeaderBuilder.builder()
			.add(AUTHORIZATION, manager.createAuthorization())
			.add(APP_KEY, kisProperties.getAppkey())
			.add(APP_SECRET, kisProperties.getSecretkey())
			.add(TR_ID, kisTrIdProperties.getClosingPrice())
			.build();
		MultiValueMap<String, String> queryParam = KisQueryParamBuilder.builder()
			.add(KisQueryParam.FID_COND_MRKT_DIV_CODE, FidCondMrktDivCode.STOCK)
			.add(KisQueryParam.FID_INPUT_ISCD, tickerSymbol)
			.add(KisQueryParam.FID_INPUT_DATE_1, yesterday())
			.add(KisQueryParam.FID_INPUT_DATE_2, yesterday())
			.add(KisQueryParam.FID_PERIOD_DIV_CODE, FidPeriodDivCode.DAILY_LAST_30_TRADING_DAYS)
			.add(KisQueryParam.FID_ORG_ADJ_PRC, FidOrgAdjPrc.ADJUSTED)
			.build();

		return performGet(
			kisProperties.getClosingPriceUrl(),
			header,
			queryParam,
			KisClosingPrice.class
		);
	}

	@NotNull
	private String yesterday() {
		return LocalDate.now().minusDays(1L).toString();
	}

	/**
	 * tickerSymbol에 해당하는 종목의 배당 일정을 조회합니다.
	 * 해당 년도 범위에 대한 배당 일정을 조회합니다.
	 *
	 * @param tickerSymbol 종목의 단축코드
	 * @return 종목의 배당 일정 정보
	 */
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
			.add(CUSTOMER_TYPE, CustomerType.INDIVIDUAL)
			.build();

		MultiValueMap<String, String> queryParam = KisQueryParamBuilder.builder()
			.add(KisQueryParam.HIGH_GB, Strings.EMPTY)
			.add(KisQueryParam.CTS, Strings.EMPTY)
			.add(KisQueryParam.GB1, GB1.TOTAL_DIVIDEND)
			.add(KisQueryParam.F_DT, basicIso(from))
			.add(KisQueryParam.T_DT, basicIso(to))
			.add(KisQueryParam.SHT_CD, tickerSymbol)
			.build();

		return performGet(
			kisProperties.getDividendUrl(),
			header,
			queryParam,
			KisDividendWrapper.class
		);
	}

	public Mono<List<KisDividend>> fetchDividendAll(LocalDate from, LocalDate to) {
		MultiValueMap<String, String> header = KisHeaderBuilder.builder()
			.add(CONTENT_TYPE, APPLICATION_JSON_UTF8)
			.add(AUTHORIZATION, manager.createAuthorization())
			.add(APP_KEY, kisProperties.getAppkey())
			.add(APP_SECRET, kisProperties.getSecretkey())
			.add(TR_ID, kisTrIdProperties.getDividend())
			.add(CUSTOMER_TYPE, CustomerType.INDIVIDUAL)
			.build();
		MultiValueMap<String, String> queryParam = KisQueryParamBuilder.builder()
			.add(KisQueryParam.HIGH_GB, Strings.EMPTY)
			.add(KisQueryParam.CTS, Strings.EMPTY)
			.add(KisQueryParam.GB1, GB1.TOTAL_DIVIDEND)
			.add(KisQueryParam.F_DT, basicIso(from))
			.add(KisQueryParam.T_DT, basicIso(to))
			.add(KisQueryParam.SHT_CD, Strings.EMPTY)
			.build();

		return performGet(
			kisProperties.getDividendUrl(),
			header,
			queryParam,
			KisDividendWrapper.class
		).map(KisDividendWrapper::getKisDividends);
	}

	public Mono<KisIpoResponse> fetchIpo(LocalDate from, LocalDate to) {
		MultiValueMap<String, String> header = KisHeaderBuilder.builder()
			.add(CONTENT_TYPE, APPLICATION_JSON_UTF8)
			.add(AUTHORIZATION, manager.createAuthorization())
			.add(APP_KEY, kisProperties.getAppkey())
			.add(APP_SECRET, kisProperties.getSecretkey())
			.add(TR_ID, kisTrIdProperties.getIpo())
			.add(CUSTOMER_TYPE, CustomerType.INDIVIDUAL)
			.build();
		MultiValueMap<String, String> queryParam = KisQueryParamBuilder.builder()
			.add(KisQueryParam.SHT_CD, Strings.EMPTY)
			.add(KisQueryParam.F_DT, basicIso(from))
			.add(KisQueryParam.T_DT, basicIso(to))
			.add(KisQueryParam.CTS, Strings.EMPTY)
			.build();

		return performGet(
			kisProperties.getIpoUrl(),
			header,
			queryParam,
			KisIpoResponse.class
		).retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5)));
	}

	@NotNull
	private String basicIso(LocalDate localDate) {
		return localDate.format(DateTimeFormatter.BASIC_ISO_DATE);
	}

	public Mono<KisSearchStockInfo> fetchSearchStockInfo(String tickerSymbol) {
		MultiValueMap<String, String> header = KisHeaderBuilder.builder()
			.add(CONTENT_TYPE, APPLICATION_JSON_UTF8)
			.add(AUTHORIZATION, manager.createAuthorization())
			.add(APP_KEY, kisProperties.getAppkey())
			.add(APP_SECRET, kisProperties.getSecretkey())
			.add(TR_ID, kisTrIdProperties.getSearchStockInfo())
			.add(CUSTOMER_TYPE, CustomerType.INDIVIDUAL)
			.build();
		MultiValueMap<String, String> queryParam = KisQueryParamBuilder.builder()
			.add(KisQueryParam.PRDT_TYPE_CD, PrdtTypeCd.STOCK)
			.add(KisQueryParam.PDNO, tickerSymbol)
			.build();

		return performGet(
			kisProperties.getSearchStockInfoUrl(),
			header,
			queryParam,
			KisSearchStockInfo.class);
	}

	private <T> Mono<T> performGet(String urlPath, MultiValueMap<String, String> headerMap,
		MultiValueMap<String, String> queryParam, Class<T> responseType) {
		return webClient
			.get()
			.uri(uriBuilder -> uriBuilder
				.path(urlPath)
				.queryParams(queryParam)
				.build())
			.headers(httpHeaders -> httpHeaders.addAll(headerMap))
			.retrieve()
			.onStatus(HttpStatusCode::isError, this::handleError)
			.bodyToMono(responseType);
	}

	private Mono<? extends Throwable> handleError(ClientResponse clientResponse) {
		return clientResponse.bodyToMono(KisErrorResponse.class)
			.doOnNext(kisErrorResponse -> log.error(kisErrorResponse.toString()))
			.flatMap(kisErrorResponse -> Mono.error(kisErrorResponse::toException));
	}
}
