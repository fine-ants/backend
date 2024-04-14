package codesquad.fineants.spring.api.kis.service;

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

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.portfolio_holding.PortfolioHoldingRepository;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.domain.stock_dividend.StockDividend;
import codesquad.fineants.domain.stock_dividend.StockDividendRepository;
import codesquad.fineants.spring.AbstractContainerBaseTest;
import codesquad.fineants.spring.api.common.errors.exception.KisException;
import codesquad.fineants.spring.api.kis.client.KisClient;
import codesquad.fineants.spring.api.kis.client.KisCurrentPrice;
import codesquad.fineants.spring.api.kis.manager.HolidayManager;
import codesquad.fineants.spring.api.kis.manager.KisAccessTokenManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.kis.response.KisClosingPrice;
import codesquad.fineants.spring.api.kis.response.KisDividend;
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
		stockDividendRepository.deleteAllInBatch();
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
		List<Stock> stocks = stockRepository.saveAll(List.of(
			createSamsungStock()
		));
		stocks.forEach(stock -> portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock)));

		given(kisAccessTokenManager.createAuthorization()).willReturn(createAuthorization());
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

		given(kisAccessTokenManager.createAuthorization()).willReturn(createAuthorization());
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

		given(kisAccessTokenManager.createAuthorization()).willReturn(createAuthorization());
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
		given(holidayManager.isHoliday(any(LocalDate.class))).willReturn(true);
		// when
		kisService.scheduleRefreshingAllStockCurrentPrice();
		// then
		verify(holidayManager, times(1)).isHoliday(any(LocalDate.class));
	}

	@DisplayName("배당일정을 최신화한다")
	@Test
	void refreshDividend() {
		// given
		Stock samsung = createSamsungStock();
		Stock kakao = createKakaoStock();
		stockRepository.saveAll(List.of(samsung, kakao));
		stockDividendRepository.saveAll(createSamsungDividends(samsung));
		stockDividendRepository.saveAll(createKakaoDividends(kakao));

		// 새로운 배정 기준일이 생김
		// 기존 데이터에 현금 배당 지급일이 새로 할당됨
		String samsungTickerSymbol = "005930";
		int samsungDividend = 361;

		String kakaoTickerSymbol = "035720";
		int kakaoDividend = 61;

		given(kisAccessTokenManager.createAuthorization()).willReturn(createAuthorization());
		given(client.fetchDividend(any(LocalDate.class), any(LocalDate.class), anyString()))
			.willReturn(List.of(
				KisDividend.create(
					samsungTickerSymbol,
					Money.from(samsungDividend),
					LocalDate.of(2023, 3, 31),
					LocalDate.of(2023, 5, 17)
				),
				KisDividend.create(
					samsungTickerSymbol,
					Money.from(samsungDividend),
					LocalDate.of(2023, 6, 30),
					LocalDate.of(2023, 8, 16)
				),
				KisDividend.create(
					samsungTickerSymbol,
					Money.from(samsungDividend),
					LocalDate.of(2023, 9, 30),
					LocalDate.of(2023, 11, 20)
				),
				KisDividend.create(
					samsungTickerSymbol,
					Money.from(samsungDividend),
					LocalDate.of(2023, 12, 31),
					LocalDate.of(2024, 4, 19)
				),
				KisDividend.create(
					samsungTickerSymbol,
					Money.from(samsungDividend),
					LocalDate.of(2024, 3, 31),
					LocalDate.of(2024, 5, 17) // 기존 데이터에서 새로운 현금 배당 지급일이 할당된 경우
				),
				KisDividend.create(
					samsungTickerSymbol,
					Money.from(samsungDividend),
					LocalDate.of(2024, 6, 30), // 새로운 배당 기준일이 생긴 경우
					null
				),
				KisDividend.create(
					kakaoTickerSymbol,
					Money.from(kakaoDividend),
					LocalDate.of(2024, 2, 29),
					null
				)
			));
		// when
		kisService.refreshDividendSchedule(LocalDate.of(2024, 4, 10));

		// then
		List<StockDividend> stockDividends = stockDividendRepository.findAllStockDividends();
		log.debug("stockDividends: {}", stockDividends);
		assertThat(stockDividends)
			.hasSize(7)
			.map(StockDividend::parse)
			.containsExactlyInAnyOrder(
				"005930:361:2023-03-31:2023-03-30:2023-05-17",
				"005930:361:2023-06-30:2023-06-29:2023-08-16",
				"005930:361:2023-09-30:2023-09-27:2023-11-20",
				"005930:361:2023-12-31:2023-12-28:2024-04-19",
				"005930:361:2024-03-31:2024-03-29:2024-05-17",
				"005930:361:2024-06-30:2024-06-28:null",
				"035720:61:2024-02-29:2024-02-28:null"
			);
	}

	private String createAuthorization() {
		return "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0b2tlbiIsImF1ZCI6Ijg5MjBlNjM2LTNkYmItNGU5MS04ZGJmLWJmZDU5ZmI2YjAwYiIsImlzcyI6InVub2d3IiwiZXhwIjoxNzAzNTcwOTA0LCJpYXQiOjE3MDM0ODQ1MDQsImp0aSI6IlBTRGc4WlVJd041eVl5ZkR6bnA0TDM2Z2xhRUpic2RJNGd6biJ9.z8dh9rlOyPq_ukm9KeCz0tkKI2QaHEe07LhXTcKQBrcP1-uiW3dwAwdknpAojJZ7aUWLUaQQn0HmjTCttjSJaA";
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
			.budget(Money.from(1000000L))
			.targetGain(Money.from(1500000L))
			.maximumLoss(Money.from(900000L))
			.member(member)
			.targetGainIsActive(false)
			.maximumLossIsActive(false)
			.build();
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

	private Stock createKakaoStock() {
		return createStock(
			"카카오보통주",
			"035720",
			"Kakao",
			"KR7035720002",
			"서비스업",
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

	private List<StockDividend> createSamsungDividends(Stock stock) {
		return List.of(
			createStockDividend(
				Money.from(361L),
				LocalDate.of(2022, 3, 31),
				LocalDate.of(2022, 3, 30),
				LocalDate.of(2022, 5, 17),
				stock
			),
			createStockDividend(
				Money.from(361L),
				LocalDate.of(2022, 6, 30),
				LocalDate.of(2022, 6, 29),
				LocalDate.of(2022, 8, 16),
				stock
			),
			createStockDividend(
				Money.from(361L),
				LocalDate.of(2022, 9, 30),
				LocalDate.of(2022, 9, 29),
				LocalDate.of(2022, 11, 15),
				stock
			),
			createStockDividend(
				Money.from(361L),
				LocalDate.of(2022, 12, 31),
				LocalDate.of(2022, 12, 30),
				LocalDate.of(2023, 4, 14),
				stock),
			createStockDividend(
				Money.from(361L),
				LocalDate.of(2023, 3, 31),
				LocalDate.of(2023, 3, 30),
				LocalDate.of(2023, 5, 17),
				stock),
			createStockDividend(
				Money.from(361L),
				LocalDate.of(2023, 6, 30),
				LocalDate.of(2023, 6, 29),
				LocalDate.of(2023, 8, 16),
				stock),
			createStockDividend(
				Money.from(361L),
				LocalDate.of(2023, 9, 30),
				LocalDate.of(2023, 9, 27),
				LocalDate.of(2023, 11, 20),
				stock),
			createStockDividend(
				Money.from(361L),
				LocalDate.of(2023, 12, 31),
				LocalDate.of(2023, 12, 29),
				LocalDate.of(2024, 4, 19),
				stock),
			createStockDividend(
				Money.from(361L),
				LocalDate.of(2024, 3, 31),
				LocalDate.of(2024, 3, 29),
				null,
				stock)
		);
	}

	private List<StockDividend> createKakaoDividends(Stock stock) {
		return List.of(
			createStockDividend(
				Money.from(61L),
				LocalDate.of(2022, 12, 31),
				LocalDate.of(2022, 12, 30),
				LocalDate.of(2023, 4, 25),
				stock
			),
			createStockDividend(
				Money.from(61L),
				LocalDate.of(2024, 2, 29),
				LocalDate.of(2024, 2, 28),
				null,
				stock
			)
		);
	}

	private StockDividend createStockDividend(Money dividend, LocalDate recordDate, LocalDate exDividendDate,
		LocalDate paymentDate,
		Stock stock) {
		return StockDividend.builder()
			.dividend(dividend)
			.exDividendDate(exDividendDate)
			.recordDate(recordDate)
			.paymentDate(paymentDate)
			.stock(stock)
			.build();
	}
}
