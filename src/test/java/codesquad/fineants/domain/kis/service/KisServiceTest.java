package codesquad.fineants.domain.kis.service;

import static codesquad.fineants.domain.stock.domain.dto.response.StockDataResponse.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.test.context.support.WithMockUser;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.holding.repository.PortfolioHoldingRepository;
import codesquad.fineants.domain.kis.client.KisAccessToken;
import codesquad.fineants.domain.kis.client.KisClient;
import codesquad.fineants.domain.kis.client.KisCurrentPrice;
import codesquad.fineants.domain.kis.domain.dto.response.KisClosingPrice;
import codesquad.fineants.domain.kis.domain.dto.response.KisDividend;
import codesquad.fineants.domain.kis.domain.dto.response.KisDividendWrapper;
import codesquad.fineants.domain.kis.domain.dto.response.KisIpo;
import codesquad.fineants.domain.kis.domain.dto.response.KisIpoResponse;
import codesquad.fineants.domain.kis.domain.dto.response.KisSearchStockInfo;
import codesquad.fineants.domain.kis.repository.CurrentPriceRedisRepository;
import codesquad.fineants.domain.kis.repository.KisAccessTokenRepository;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.repository.PortfolioRepository;
import codesquad.fineants.domain.stock.domain.entity.Market;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockRepository;
import codesquad.fineants.domain.stock.service.StockCsvReader;
import codesquad.fineants.global.common.delay.DelayManager;
import codesquad.fineants.global.common.time.LocalDateTimeService;
import codesquad.fineants.global.errors.exception.kis.KisException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
class KisServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private KisService kisService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private PortfolioHoldingRepository portfolioHoldingRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private KisAccessTokenRepository kisAccessTokenRepository;

	@Autowired
	private StockCsvReader stockCsvReader;

	@Autowired
	private KisAccessTokenRedisService kisAccessTokenRedisService;

	@Autowired
	private CurrentPriceRedisRepository currentPriceRedisRepository;

	@MockBean
	private KisClient client;

	@SpyBean
	private DelayManager delayManager;

	@SpyBean
	private LocalDateTimeService localDateTimeService;

	@AfterEach
	void tearDown() {
		Mockito.clearInvocations(client);
		kisAccessTokenRepository.refreshAccessToken(null);
		kisAccessTokenRedisService.deleteAccessTokenMap();
	}

	@WithMockUser(roles = {"ADMIN"})
	@DisplayName("주식 현재가 시세를 가져온다")
	@Test
	void readRealTimeCurrentPrice() {
		// given
		String tickerSymbol = "005930";
		kisAccessTokenRepository.refreshAccessToken(createKisAccessToken());
		given(client.fetchCurrentPrice(anyString()))
			.willReturn(Mono.just(KisCurrentPrice.create(tickerSymbol, 60000L)));
		// when
		KisCurrentPrice kisCurrentPrice = kisService.fetchCurrentPrice(tickerSymbol)
			.block();
		// then
		assertThat(kisCurrentPrice)
			.extracting("tickerSymbol", "price")
			.containsExactlyInAnyOrder("005930", 60000L);
	}

	@WithMockUser(roles = {"ADMIN"})
	@DisplayName("현재가를 갱신한다")
	@Test
	void refreshStockCurrentPriceWhenExceedingTransactionPerSecond() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		List<Stock> stocks = stockRepository.saveAll(List.of(createSamsungStock()));
		stocks.forEach(stock -> portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock)));

		given(client.fetchCurrentPrice("005930"))
			.willReturn(Mono.just(KisCurrentPrice.create("005930", 10000L)));
		given(delayManager.timeout()).willReturn(Duration.ofSeconds(1));
		given(delayManager.delay()).willReturn(Duration.ZERO);
		given(delayManager.fixedDelay()).willReturn(Duration.ZERO);

		List<String> tickerSymbols = stocks.stream()
			.map(Stock::getTickerSymbol)
			.toList();
		// when
		kisService.refreshStockCurrentPrice(tickerSymbols);

		// then
		assertThat(currentPriceRedisRepository.fetchPriceBy("005930").orElseThrow()).isEqualTo(Money.won(10000));
	}

	@DisplayName("다수의 종목들의 현재가를 갱신한 다음에 레디스에 저장한다")
	@Test
	void refreshStockCurrentPrice_whenMultipleStocks_thenSaveToRedis() {
		// given
		List<String> tickers = saveStocks(100).stream()
			.map(Stock::getTickerSymbol)
			.toList();
		tickers.forEach(ticker -> given(client.fetchCurrentPrice(ticker))
			.willReturn(Mono.just(KisCurrentPrice.create(ticker, 50000L)).delayElement(Duration.ofMillis(100))));
		given(delayManager.delay()).willReturn(Duration.ZERO);
		// when
		List<KisCurrentPrice> prices = kisService.refreshStockCurrentPrice(tickers);
		// then
		Assertions.assertThat(prices).hasSize(tickers.size());
	}

	@DisplayName("현재가를 갱신할때 액세스 토큰의 만료시간이 1시간 이전어서 새로운 액세스 토큰을 재발급한다")
	@Test
	void refreshStockCurrentPrice_whenAccessTokenSoonExpired_thenFetchAccessToken() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		List<Stock> stocks = stockRepository.saveAll(List.of(createSamsungStock()));
		stocks.forEach(stock -> portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock)));

		given(client.fetchCurrentPrice("005930"))
			.willReturn(Mono.just(KisCurrentPrice.create("005930", 10000L)));
		given(delayManager.delay()).willReturn(Duration.ZERO);
		given(delayManager.fixedDelay()).willReturn(Duration.ZERO);

		List<String> tickerSymbols = stocks.stream()
			.map(Stock::getTickerSymbol)
			.toList();

		KisAccessToken soonExpiredAccessToken = KisAccessToken.bearerType("accessToken",
			LocalDateTime.now().plusMinutes(10), 6000);
		kisAccessTokenRepository.refreshAccessToken(soonExpiredAccessToken);
		kisAccessTokenRedisService.setAccessTokenMap(soonExpiredAccessToken, LocalDateTime.now());

		KisAccessToken reloadAccessToken = createKisAccessToken();
		given(client.fetchAccessToken())
			.willReturn(Mono.just(reloadAccessToken));

		// when
		kisService.refreshStockCurrentPrice(tickerSymbols);

		// then
		assertThat(kisAccessTokenRepository.createAuthorization()).isEqualTo(reloadAccessToken.createAuthorization());
		assertThat(kisAccessTokenRedisService.getAccessTokenMap().orElseThrow().getAccessToken()).isEqualTo(
			reloadAccessToken.getAccessToken());
		assertThat(currentPriceRedisRepository.fetchPriceBy("005930").orElseThrow()).isEqualTo(Money.won(10000));
	}

	@DisplayName("한국투자증권에 종목 현재가 요청중에 액세스 토큰이 만료되어 실패하게 되면, 해당 요청은 조회하지 않는다")
	@Test
	void refreshStockCurrentPrice_whenAccessTokenExpired_thenCancelStockCurrentPriceRequest() {
		// given
		List<String> tickers = saveStocks(100).stream()
			.map(Stock::getTickerSymbol)
			.toList();
		tickers.forEach(ticker -> given(client.fetchCurrentPrice(ticker))
			.willReturn(Mono.error(KisException.expiredAccessToken())));
		// when
		List<KisCurrentPrice> prices = kisService.refreshStockCurrentPrice(tickers);
		// then
		Assertions.assertThat(prices).isEmpty();
	}

	@DisplayName("한국투자증권에 종목 현재가 요청중에 요청 건수 초과 에러시 재시도 또한 전부 실패하게 되면 리스트에 추가되지 않는다")
	@Test
	void refreshStockCurrentPrice_whenFailRetry_thenNotAddResultList() {
		// given
		List<String> tickers = saveStocks(100).stream()
			.map(Stock::getTickerSymbol)
			.toList();
		tickers.forEach(ticker -> given(client.fetchCurrentPrice(ticker))
			.willReturn(Mono.error(KisException.requestLimitExceeded())));
		given(delayManager.fixedDelay()).willReturn(Duration.ZERO);
		// when
		List<KisCurrentPrice> prices = kisService.refreshStockCurrentPrice(tickers);
		// then
		Assertions.assertThat(prices).isEmpty();
	}

	@DisplayName("종목 현재가 갱신시 예외가 발생하면 다시 시도하여 가격을 조회한다")
	@Test
	void refreshStockCurrentPrice_whenFailFetch_thenRetryFetch() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		List<Stock> stocks = stockRepository.saveAll(List.of(
			createSamsungStock()
		));
		stocks.forEach(stock -> portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock)));

		given(client.fetchCurrentPrice("005930"))
			.willReturn(Mono.error(KisException.requestLimitExceeded()))
			.willReturn(Mono.just(KisCurrentPrice.create("005930", 50000L)));
		given(delayManager.delay()).willReturn(Duration.ZERO);
		given(delayManager.fixedDelay()).willReturn(Duration.ZERO);

		List<String> tickerSymbols = stocks.stream()
			.map(Stock::getTickerSymbol)
			.toList();
		// when
		List<KisCurrentPrice> prices = kisService.refreshStockCurrentPrice(tickerSymbols);

		// then
		assertThat(prices).hasSize(1);
		assertThat(currentPriceRedisRepository.fetchPriceBy("005930").orElseThrow())
			.isEqualTo(Money.won(50000));
	}

	@DisplayName("종가 갱신시 요청건수 초과로 실패하였다가 다시 시도하여 성공한다")
	@Test
	void refreshLastDayClosingPriceWhenExceedingTransactionPerSecond() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		List<Stock> stocks = stockRepository.saveAll(List.of(
			createSamsungStock()
		));
		stocks.forEach(stock -> portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock)));

		kisAccessTokenRepository.refreshAccessToken(createKisAccessToken());
		given(client.fetchClosingPrice(anyString()))
			.willThrow(KisException.requestLimitExceeded())
			.willThrow(KisException.requestLimitExceeded())
			.willReturn(Mono.just(KisClosingPrice.create("005930", 10000L)));
		given(delayManager.fixedDelay()).willReturn(Duration.ZERO);
		List<String> tickerSymbols = stocks.stream()
			.map(Stock::getTickerSymbol)
			.toList();
		// when
		kisService.refreshClosingPrice(tickerSymbols);

		// then
		verify(client, times(3)).fetchClosingPrice(anyString());
	}

	@DisplayName("한국투자증권에 상장된 종목 정보를 조회한다")
	@Test
	void fetchStockInfoInRangedIpo() {
		// given
		KisAccessToken kisAccessToken = createKisAccessToken();
		kisAccessTokenRepository.refreshAccessToken(kisAccessToken);

		KisIpoResponse kisIpoResponse = KisIpoResponse.create(
			List.of(KisIpo.create("20240326", "000660", "에스케이하이닉스보통주"))
		);
		given(client.fetchIpo(
			any(LocalDate.class),
			any(LocalDate.class)
		))
			.willReturn(Mono.just(kisIpoResponse));

		KisSearchStockInfo kisSearchStockInfo = KisSearchStockInfo.listedStock(
			"KR7000660001",
			"000660",
			"에스케이하이닉스보통주",
			"SK hynix",
			"STK",
			"전기,전자"
		);
		given(client.fetchSearchStockInfo(anyString()))
			.willReturn(Mono.just(kisSearchStockInfo));
		// when
		Flux<StockIntegrationInfo> stocks = kisService.fetchStockInfoInRangedIpo();
		// then
		StepVerifier.create(stocks)
			.expectNext(StockIntegrationInfo.create("000660", "에스케이하이닉스보통주", "SK hynix", "KR7000660001",
				"전기,전자", Market.KOSPI))
			.expectComplete()
			.verify();
	}
	
	@DisplayName("상장된 종목들의 상세 종목을 조회할 때 별도의 스레드에서 blocking되면 안된다")
	@Test
	void fetchStockInfoInRangedIpo_shouldNotBlockInSeparateThread() {
		// given
		given(client.fetchIpo(
			any(LocalDate.class),
			any(LocalDate.class)
		)).willReturn(Mono.error(() -> new IllegalStateException(
			"blockOptional() is blocking, which is not supported in thread parallel-1")));
		// when
		Flux<StockIntegrationInfo> result = kisService.fetchStockInfoInRangedIpo();
		// then
		StepVerifier.create(result)
			.expectNextCount(0)
			.expectComplete()
			.verify();
	}

	@DisplayName("사용자는 db에 저장된 종목을 각각 조회한다")
	@Test
	void fetchSearchStockInfo() {
		// given
		List<Stock> stocks = saveStocks().stream()
			.limit(100)
			.toList();
		List<String> tickerSymbols = stocks.stream()
			.map(Stock::getTickerSymbol)
			.toList();

		KisAccessToken kisAccessToken = createKisAccessToken();
		kisAccessTokenRepository.refreshAccessToken(kisAccessToken);
		stocks.forEach(s ->
			given(client.fetchSearchStockInfo(s.getTickerSymbol()))
				.willReturn(Mono.just(
						KisSearchStockInfo.listedStock(
							s.getStockCode(),
							s.getTickerSymbol(),
							s.getCompanyName(),
							s.getCompanyNameEng(),
							"STK",
							s.getSector()
						)
					)
				)
		);

		// when & then
		tickerSymbols.stream()
			.map(kisService::fetchSearchStockInfo)
			.map(Mono::just)
			.forEach(mono ->
				StepVerifier.create(mono)
					.expectNextMatches(stockInfo -> {
						Assertions.assertThat(stockInfo).isNotNull();
						return true;
					})
					.verifyComplete()
			);
	}

	@DisplayName("사용자는 삼성전자의 올해 배당일정을 조회한다")
	@Test
	void fetchDividend() {
		// given
		String tickerSymbol = "005930";
		KisAccessToken kisAccessToken = createKisAccessToken();
		kisAccessTokenRepository.refreshAccessToken(kisAccessToken);
		given(client.fetchDividendThisYear(tickerSymbol))
			.willReturn(Mono.just(KisDividendWrapper.create(List.of(
				KisDividend.create(tickerSymbol, Money.won(300), LocalDate.of(2024, 3, 1),
					LocalDate.of(2024, 5, 1))))));
		// when
		Flux<KisDividend> dividends = kisService.fetchDividend(tickerSymbol);
		// then
		StepVerifier.create(dividends)
			.expectNext(
				KisDividend.create("005930", Money.won(300), LocalDate.of(2024, 3, 1), LocalDate.of(2024, 5, 1)))
			.expectComplete()
			.verify();
	}

	@DisplayName("사용자는 새로운 한국투자증권의 액세스 토큰을 발급받아서 배당 일정을 조회한다")
	@Test
	void fetchDividend_whenAccessTokenExpired_thenIssueAccessToken() {
		// given
		String tickerSymbol = "005930";
		kisAccessTokenRepository.refreshAccessToken(null);
		KisAccessToken newKisAccessToken = createKisAccessToken();
		given(client.fetchAccessToken())
			.willReturn(Mono.just(newKisAccessToken));
		given(client.fetchDividendThisYear(tickerSymbol))
			.willReturn(Mono.just(KisDividendWrapper.create(List.of(
				KisDividend.create(tickerSymbol, Money.won(300), LocalDate.of(2024, 3, 1),
					LocalDate.of(2024, 5, 1))))));
		// when
		Flux<KisDividend> dividends = kisService.fetchDividend(tickerSymbol);
		// then
		StepVerifier.create(dividends)
			.expectNext(
				KisDividend.create("005930", Money.won(300), LocalDate.of(2024, 3, 1), LocalDate.of(2024, 5, 1)))
			.expectComplete()
			.verify();
	}

	private List<Stock> saveStocks() {
		return saveStocks(0);
	}

	private List<Stock> saveStocks(int limit) {
		return stockRepository.saveAll(stockCsvReader.readStockCsv()
			.stream()
			.limit(limit)
			.toList());
	}
}
