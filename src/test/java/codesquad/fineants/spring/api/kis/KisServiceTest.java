package codesquad.fineants.spring.api.kis;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.portfolio_holding.PortfolioHoldingRepository;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.spring.api.errors.exception.KisException;
import codesquad.fineants.spring.api.kis.client.KisClient;
import codesquad.fineants.spring.api.kis.manager.KisAccessTokenManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.kis.response.CurrentPriceResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
class KisServiceTest {

	@Autowired
	private KisService kisService;

	@Autowired
	private KisRedisService kisRedisService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private PortfolioHoldingRepository portfolioHoldingRepository;

	@Autowired
	private StockRepository stockRepository;

	@MockBean
	private KisClient client;

	@MockBean
	private KisAccessTokenManager kisAccessTokenManager;

	@MockBean
	private LastDayClosingPriceManager lastDayClosingPriceManager;

	@AfterEach
	void tearDown() {
		portfolioHoldingRepository.deleteAllInBatch();
		portfolioRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
		stockRepository.deleteAllInBatch();
	}

	@DisplayName("주식 현재가 시세를 가져온다")
	@Test
	void readRealTimeCurrentPrice() {
		// given
		String tickerSymbol = "005930";
		given(kisAccessTokenManager.createAuthorization()).willReturn(createAuthorization());
		given(client.readRealTimeCurrentPrice(anyString(), anyString())).willReturn(60000L);
		// when
		CurrentPriceResponse response = kisService.readRealTimeCurrentPrice(tickerSymbol);
		// then
		assertThat(response)
			.extracting("tickerSymbol", "currentPrice")
			.containsExactlyInAnyOrder("005930", 60000L);
	}

	@DisplayName("AccessTokenAspect이 수행하여 새로운 엑세스 토큰을 갱신한다")
	@Test
	void readRealTimeCurrentPriceWithAspect() {
		// given
		String tickerSymbol = "005930";
		// when
		CurrentPriceResponse response = kisService.readRealTimeCurrentPrice(tickerSymbol);
		// then
		assertThat(response).extracting("tickerSymbol").isEqualTo(tickerSymbol);
		assertThat(response).extracting("currentPrice").isNotNull();
	}

	@DisplayName("별도의 쓰레드로 실행시 AccessTokenAspect이 수행하여 새로운 엑세스 토큰을 갱신한다")
	@Test
	void readRealTimeCurrentPriceWithAspectAndRunnable() throws
		ExecutionException,
		InterruptedException,
		TimeoutException {
		// given
		String tickerSymbol = "005930";
		// when
		CompletableFuture<CurrentPriceResponse> future = CompletableFuture.supplyAsync(
			() -> kisService.readRealTimeCurrentPrice(tickerSymbol));

		CurrentPriceResponse response = future.get(2L, TimeUnit.SECONDS);
		// then
		assertThat(response).extracting("tickerSymbol").isEqualTo(tickerSymbol);
		assertThat(response).extracting("currentPrice").isNotNull();
	}

	@DisplayName("현재가 및 종가 갱신 전에 액세스 토큰을 새로 발급받아 갱신한다")
	@Test
	void refreshStockPrice() {
		// given
		kisRedisService.deleteAccessTokenMap();
		given(kisAccessTokenManager.isAccessTokenExpired(any(LocalDateTime.class)))
			.willReturn(true);
		given(client.accessToken()).willReturn(createAccessTokenMap(LocalDateTime.now()));
		// when
		kisService.refreshStockPrice();
		// then
		assertThat(kisRedisService.getAccessTokenMap()).isNotNull();
	}

	@DisplayName("초당 거래건수가 초과되어 현재가 및 종가를 갱신할 수 없다")
	@Test
	void refreshStockPriceWhenExceedingTransactionPerSecond() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		List<String> tickerSymbols = List.of(
			"000270", "000660", "000880", "001360", "001500",
			"003530", "003550", "003800", "005930", "034220",
			"035420", "035720", "086520", "115390", "194480",
			"323410");
		List<Stock> stocks = stockRepository.saveAll(tickerSymbols.stream()
			.map(this::createStock)
			.collect(Collectors.toList()));
		stocks.forEach(stock -> portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock)));

		given(kisAccessTokenManager.createAuthorization()).willReturn(createAuthorization());
		given(lastDayClosingPriceManager.hasPrice(anyString())).willReturn(false);
		given(client.readRealTimeCurrentPrice(anyString(), anyString())).willThrow(
			new KisException("초당 거래건수를 초과하였습니다."));
		given(client.readLastDayClosingPrice(anyString(), anyString())).willThrow(
			new KisException("초당 거래건수를 초과하였습니다."));
		// when
		kisService.refreshStockPrice();
		// then
		verify(client, times(16)).readRealTimeCurrentPrice(anyString(), anyString());
		verify(client, times(16)).readLastDayClosingPrice(anyString(), anyString());
	}

	private static String createAuthorization() {
		return "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0b2tlbiIsImF1ZCI6Ijg5MjBlNjM2LTNkYmItNGU5MS04ZGJmLWJmZDU5ZmI2YjAwYiIsImlzcyI6InVub2d3IiwiZXhwIjoxNzAzNTcwOTA0LCJpYXQiOjE3MDM0ODQ1MDQsImp0aSI6IlBTRGc4WlVJd041eVl5ZkR6bnA0TDM2Z2xhRUpic2RJNGd6biJ9.z8dh9rlOyPq_ukm9KeCz0tkKI2QaHEe07LhXTcKQBrcP1-uiW3dwAwdknpAojJZ7aUWLUaQQn0HmjTCttjSJaA";
	}

	private Map<String, Object> createAccessTokenMap(LocalDateTime now) {
		Map<String, Object> map = new HashMap<>();
		map.put("access_token",
			"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0b2tlbiIsImF1ZCI6IjBiYWFlYzg1LWU0YjctNDFlOS05ODk1LTUyNDE2ODRjNDhkOSIsImlzcyI6InVub2d3IiwiZXhwIjoxNzAzNDcxMzg2LCJpYXQiOjE3MDMzODQ5ODYsImp0aSI6IlBTRGc4WlVJd041eVl5ZkR6bnA0TDM2Z2xhRUpic2RJNGd6biJ9.mrJht_O2aRrhSPN1DSmHKarwAfgDpr4GECvF30Is2EI0W6ypbe7DXwXmluhQXT0h1g7OHhGhyBhDNtya4LcctQ");
		map.put("access_token_token_expired",
			now.plusDays(1L).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		map.put("token_type", "Bearer");
		map.put("expires_in", 86400);
		return map;
	}

	private Member createMember() {
		return Member.builder()
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.password("kim1234@")
			.provider("local")
			.build();
	}

	private Portfolio createPortfolio(Member member) {
		return Portfolio.builder()
			.name("내꿈은 워렌버핏")
			.securitiesFirm("토스")
			.budget(1000000L)
			.targetGain(1500000L)
			.maximumLoss(900000L)
			.member(member)
			.targetGainIsActive(false)
			.maximumIsActive(false)
			.build();
	}

	private Stock createStock(String tickerSymbol) {
		return Stock.builder()
			.companyName("임시 종목")
			.tickerSymbol(tickerSymbol)
			.companyNameEng("temp stock")
			.stockCode("1234")
			.sector("임시섹터")
			.market(Market.KOSPI)
			.build();
	}

	private PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock) {
		return PortfolioHolding.builder()
			.portfolio(portfolio)
			.stock(stock)
			.build();
	}

}
