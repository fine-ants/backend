package codesquad.fineants.domain.kis.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.kis.client.KisClient;
import codesquad.fineants.domain.kis.client.KisCurrentPrice;
import codesquad.fineants.domain.kis.domain.dto.response.KisClosingPrice;
import codesquad.fineants.domain.kis.repository.HolidayRepository;
import codesquad.fineants.domain.kis.repository.KisAccessTokenRepository;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.repository.PortfolioRepository;
import codesquad.fineants.domain.portfolio_holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.portfolio_holding.repository.PortfolioHoldingRepository;
import codesquad.fineants.domain.stock.domain.entity.Market;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockRepository;
import codesquad.fineants.domain.stock_dividend.repository.StockDividendRepository;
import codesquad.fineants.global.errors.exception.KisException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

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
	private StockDividendRepository stockDividendRepository;

	@MockBean
	private KisClient client;

	@Autowired
	private KisAccessTokenRepository kisAccessTokenRepository;

	@MockBean
	private HolidayRepository holidayRepository;

	@AfterEach
	void tearDown() {
		portfolioHoldingRepository.deleteAllInBatch();
		portfolioRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
		stockDividendRepository.deleteAllInBatch();
		stockRepository.deleteAllInBatch();
		Mockito.clearInvocations(client);
	}

	@WithMockUser(roles = {"ADMIN"})
	@DisplayName("주식 현재가 시세를 가져온다")
	@Test
	void readRealTimeCurrentPrice() {
		// given
		String tickerSymbol = "005930";
		kisAccessTokenRepository.refreshAccessToken(createKisAccessToken());
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

	@WithMockUser(roles = {"ADMIN"})
	@DisplayName("현재가 갱신시 요청건수 초과로 실패하였다가 다시 시도하여 성공한다")
	@Test
	void refreshStockCurrentPriceWhenExceedingTransactionPerSecond() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		List<Stock> stocks = stockRepository.saveAll(List.of(
			createSamsungStock()
		));
		stocks.forEach(stock -> portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock)));

		kisAccessTokenRepository.refreshAccessToken(createKisAccessToken());
		given(client.fetchCurrentPrice(anyString(), anyString()))
			.willThrow(new KisException("요청건수가 초과되었습니다"))
			.willReturn(Mono.just(KisCurrentPrice.create("005930", 10000L)));

		List<String> tickerSymbols = stocks.stream()
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toList());
		// when
		kisService.refreshStockCurrentPrice(tickerSymbols);

		// then
		verify(client, times(1)).fetchCurrentPrice(anyString(), anyString());
	}

	@WithMockUser(roles = {"ADMIN"})
	@DisplayName("종목 현재가 갱신시 예외가 발생하면 null을 반환한다")
	@Test
	void refreshStockCurrentPrice_whenException_thenReturnNull() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		List<Stock> stocks = stockRepository.saveAll(List.of(
			createSamsungStock()
		));
		stocks.forEach(stock -> portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock)));

		kisAccessTokenRepository.refreshAccessToken(createKisAccessToken());
		given(client.fetchCurrentPrice(anyString(), anyString()))
			.willThrow(new KisException("요청건수가 초과되었습니다"));

		List<String> tickerSymbols = stocks.stream()
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toList());
		// when
		List<KisCurrentPrice> prices = kisService.refreshStockCurrentPrice(tickerSymbols);

		// then
		assertThat(prices).isEmpty();
	}

	@WithMockUser(roles = {"ADMIN"})
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
		given(client.fetchClosingPrice(anyString(), anyString()))
			.willThrow(new KisException("요청건수가 초과되었습니다"))
			.willThrow(new KisException("요청건수가 초과되었습니다"))
			.willReturn(Mono.just(KisClosingPrice.create("005930", 10000L)));

		List<String> tickerSymbols = stocks.stream()
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toList());
		// when
		kisService.refreshLastDayClosingPrice(tickerSymbols);

		// then
		verify(client, times(1)).fetchClosingPrice(anyString(), anyString());
	}

	@DisplayName("휴장일에는 종목 가격 정보를 갱신하지 않는다")
	@Test
	void refreshStockPriceWhenHoliday() {
		// given
		given(holidayRepository.isHoliday(any(LocalDate.class))).willReturn(true);
		// when
		kisService.scheduleRefreshingAllStockCurrentPrice();
		// then
		verify(holidayRepository, times(1)).isHoliday(any(LocalDate.class));
	}

	private Stock createSamsungStock() {
		return createStock(
			"삼성전자보통주",
			"005930",
			"SamsungElectronics",
			"KR7005930003",
			"전기전자",
			Market.KOSPI
		);
	}

	private Stock createStock(String companyName, String tickerSymbol, String companyNameEng, String stockCode,
		String sector, Market market) {
		return Stock.builder()
			.companyName(companyName)
			.tickerSymbol(tickerSymbol)
			.companyNameEng(companyNameEng)
			.stockCode(stockCode)
			.sector(sector)
			.market(market)
			.build();
	}

	private PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock) {
		return PortfolioHolding.builder()
			.portfolio(portfolio)
			.stock(stock)
			.build();
	}
}
