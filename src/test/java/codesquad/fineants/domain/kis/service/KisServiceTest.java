package codesquad.fineants.domain.kis.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.holding.repository.PortfolioHoldingRepository;
import codesquad.fineants.domain.kis.client.KisAccessToken;
import codesquad.fineants.domain.kis.client.KisClient;
import codesquad.fineants.domain.kis.client.KisCurrentPrice;
import codesquad.fineants.domain.kis.domain.dto.response.KisClosingPrice;
import codesquad.fineants.domain.kis.domain.dto.response.KisIpo;
import codesquad.fineants.domain.kis.domain.dto.response.KisIpoResponse;
import codesquad.fineants.domain.kis.domain.dto.response.KisSearchStockInfo;
import codesquad.fineants.domain.kis.repository.HolidayRepository;
import codesquad.fineants.domain.kis.repository.KisAccessTokenRepository;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.repository.PortfolioRepository;
import codesquad.fineants.domain.stock.domain.dto.response.StockDataResponse;
import codesquad.fineants.domain.stock.domain.entity.Market;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockRepository;
import codesquad.fineants.domain.stock.service.StockCsvReader;
import codesquad.fineants.global.errors.exception.KisException;
import lombok.extern.slf4j.Slf4j;
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

	@MockBean
	private KisClient client;

	@Autowired
	private KisAccessTokenRepository kisAccessTokenRepository;

	@Autowired
	private StockCsvReader stockCsvReader;

	@MockBean
	private HolidayRepository holidayRepository;

	@AfterEach
	void tearDown() {
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
		kisService.refreshCurrentPrice();
		// then
		verify(holidayRepository, times(1)).isHoliday(any(LocalDate.class));
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
			any(LocalDate.class),
			anyString()))
			.willReturn(Mono.just(kisIpoResponse));

		KisSearchStockInfo kisSearchStockInfo = KisSearchStockInfo.listedStock(
			"KR7000660001",
			"000660",
			"에스케이하이닉스보통주",
			"SK hynix",
			"STK",
			"전기,전자"
		);
		given(client.fetchSearchStockInfo(anyString(), anyString()))
			.willReturn(Mono.just(kisSearchStockInfo));
		// when
		Set<StockDataResponse.StockIntegrationInfo> stocks = kisService.fetchStockInfoInRangedIpo();
		// then
		assertThat(stocks)
			.hasSize(1)
			.extracting(
				StockDataResponse.StockIntegrationInfo::getStockCode,
				StockDataResponse.StockIntegrationInfo::getTickerSymbol,
				StockDataResponse.StockIntegrationInfo::getCompanyName,
				StockDataResponse.StockIntegrationInfo::getCompanyNameEng,
				StockDataResponse.StockIntegrationInfo::getMarket
			).containsExactly(tuple("KR7000660001", "000660", "에스케이하이닉스보통주", "SK hynix", Market.KOSPI));
	}

	@DisplayName("사용자는 db에 저장된 종목을 각각 조회한다")
	@Test
	void fetchSearchStockInfo() {
		// given
		List<Stock> stocks = saveStocks();
		List<String> tickerSymbols = stocks.stream()
			.map(Stock::getTickerSymbol)
			.toList();

		KisAccessToken kisAccessToken = createKisAccessToken();
		kisAccessTokenRepository.refreshAccessToken(kisAccessToken);
		String accessToken = kisAccessToken.createAuthorization();
		stocks.forEach(s ->
			given(client.fetchSearchStockInfo(s.getTickerSymbol(), accessToken))
				.willReturn(Mono.just(
					KisSearchStockInfo.listedStock(
						s.getStockCode(),
						s.getTickerSymbol(),
						s.getCompanyName(),
						s.getCompanyNameEng(),
						"STK",
						s.getSector()
					)
				)));
		// when & then
		for (String tickerSymbol : tickerSymbols) {
			StepVerifier.create(kisService.fetchSearchStockInfo(tickerSymbol))
				.expectNextMatches(stockInfo -> {
					Assertions.assertThat(stockInfo).isNotNull();
					Assertions.assertThat(tickerSymbol).isEqualTo(stockInfo.getTickerSymbol());
					return true;
				})
				.verifyComplete();
		}
	}

	private List<Stock> saveStocks() {
		Set<StockDataResponse.StockInfo> stockInfoSet = stockCsvReader.readStockCsv();
		List<Stock> stocks = stockInfoSet.stream()
			.map(StockDataResponse.StockInfo::toEntity)
			.toList();
		return stockRepository.saveAll(stocks);
	}
}
