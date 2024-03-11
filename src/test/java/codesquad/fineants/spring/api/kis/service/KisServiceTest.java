package codesquad.fineants.spring.api.kis.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.portfolio_holding.PortfolioHoldingRepository;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.spring.AbstractContainerBaseTest;
import codesquad.fineants.spring.api.common.errors.exception.KisException;
import codesquad.fineants.spring.api.kis.client.KisClient;
import codesquad.fineants.spring.api.kis.client.KisCurrentPrice;
import codesquad.fineants.spring.api.kis.manager.HolidayManager;
import codesquad.fineants.spring.api.kis.manager.KisAccessTokenManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.kis.response.KisClosingPrice;
import reactor.core.publisher.Mono;

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

	@MockBean
	private KisClient client;

	@MockBean
	private KisAccessTokenManager kisAccessTokenManager;

	@MockBean
	private LastDayClosingPriceManager lastDayClosingPriceManager;

	@MockBean
	private HolidayManager holidayManager;

	@AfterEach
	void tearDown() {
		portfolioHoldingRepository.deleteAllInBatch();
		portfolioRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
		stockRepository.deleteAllInBatch();
		Mockito.clearInvocations(client);
	}

	@DisplayName("주식 현재가 시세를 가져온다")
	@Test
	void readRealTimeCurrentPrice() {
		// given
		String tickerSymbol = "005930";
		given(kisAccessTokenManager.createAuthorization()).willReturn(createAuthorization());
		given(client.fetchCurrentPrice(anyString(), anyString()))
			.willReturn(Mono.just(KisCurrentPrice.create(tickerSymbol, 60000L)));
		// when
		KisCurrentPrice kisCurrentPrice = kisService.fetchCurrentPrice(tickerSymbol)
			.block();
		// then
		assertThat(kisCurrentPrice)
			.extracting("tickerSymbol", "price")
			.containsExactlyInAnyOrder("005930", 60000L);
	}

	@DisplayName("현재가 갱신시 요청건수 초과로 실패하였다가 다시 시도하여 성공한다")
	@Test
	void refreshStockCurrentPriceWhenExceedingTransactionPerSecond() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		List<String> tickerSymbols = List.of("000270");
		List<Stock> stocks = stockRepository.saveAll(tickerSymbols.stream()
			.map(this::createStock)
			.collect(Collectors.toList()));
		stocks.forEach(stock -> portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock)));

		given(kisAccessTokenManager.createAuthorization()).willReturn(createAuthorization());
		given(client.fetchCurrentPrice(anyString(), anyString()))
			.willThrow(new KisException("요청건수가 초과되었습니다"))
			.willReturn(Mono.just(KisCurrentPrice.create("000270", 10000L)));

		// when
		kisService.refreshStockCurrentPrice(tickerSymbols);

		// then
		verify(client, times(1)).fetchCurrentPrice(anyString(), anyString());
	}

	@DisplayName("종목 현재가 갱신시 예외가 발생하면 null을 반환한다")
	@Test
	void refreshStockCurrentPrice_whenException_thenReturnNull() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		List<String> tickerSymbols = List.of("000270");
		List<Stock> stocks = stockRepository.saveAll(tickerSymbols.stream()
			.map(this::createStock)
			.collect(Collectors.toList()));
		stocks.forEach(stock -> portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock)));

		given(kisAccessTokenManager.createAuthorization()).willReturn(createAuthorization());
		given(client.fetchCurrentPrice(anyString(), anyString()))
			.willThrow(new KisException("요청건수가 초과되었습니다"));

		// when
		List<KisCurrentPrice> prices = kisService.refreshStockCurrentPrice(tickerSymbols);

		// then
		assertThat(prices).isEmpty();
	}

	@DisplayName("종가 갱신시 요청건수 초과로 실패하였다가 다시 시도하여 성공한다")
	@Test
	void refreshLastDayClosingPriceWhenExceedingTransactionPerSecond() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		List<String> tickerSymbols = List.of("000270");
		List<Stock> stocks = stockRepository.saveAll(tickerSymbols.stream()
			.map(this::createStock)
			.collect(Collectors.toList()));
		stocks.forEach(stock -> portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock)));

		given(kisAccessTokenManager.createAuthorization()).willReturn(createAuthorization());
		given(client.readLastDayClosingPrice(anyString(), anyString()))
			.willThrow(new KisException("요청건수가 초과되었습니다"))
			.willThrow(new KisException("요청건수가 초과되었습니다"))
			.willReturn(Mono.just(KisClosingPrice.create("000270", 10000L)));

		// when
		kisService.refreshLastDayClosingPrice(tickerSymbols);

		// then
		verify(client, times(1)).readLastDayClosingPrice(anyString(), anyString());
	}

	@DisplayName("휴장일에는 종목 가격 정보를 갱신하지 않는다")
	@Test
	void refreshStockPriceWhenHoliday() {
		// given
		given(holidayManager.isHoliday(any(LocalDate.class))).willReturn(true);
		// when
		kisService.scheduleRefreshingAllStockCurrentPrice();
		// then
		verify(holidayManager, times(1)).isHoliday(any(LocalDate.class));
	}

	private String createAuthorization() {
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
			.maximumLossIsActive(false)
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
