package codesquad.fineants.spring.api.stock.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.domain.stock_dividend.StockDividend;
import codesquad.fineants.domain.stock_dividend.StockDividendRepository;
import codesquad.fineants.spring.AbstractContainerBaseTest;
import codesquad.fineants.spring.api.kis.client.KisAccessToken;
import codesquad.fineants.spring.api.kis.client.KisClient;
import codesquad.fineants.spring.api.kis.client.KisCurrentPrice;
import codesquad.fineants.spring.api.kis.response.KisClosingPrice;
import codesquad.fineants.spring.api.krx.service.KRXService;
import codesquad.fineants.spring.api.stock.response.StockDataResponse;
import codesquad.fineants.spring.api.stock.response.StockRefreshResponse;
import codesquad.fineants.spring.api.stock.response.StockResponse;
import codesquad.fineants.spring.api.stock.response.StockSectorResponse;
import reactor.core.publisher.Mono;

class StockServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private StockService stockService;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockDividendRepository stockDividendRepository;

	@MockBean
	private KRXService krxService;

	@MockBean
	private RedisTemplate<String, String> redisTemplate;

	@MockBean
	private KisClient kisClient;

	@AfterEach
	void tearDown() {
		stockDividendRepository.deleteAllInBatch();
		stockRepository.deleteAllInBatch();
	}

	@DisplayName("종목 데이터를 최신화한다")
	@Test
	void refreshStocks() {
		// given
		stockRepository.saveAll(List.of(
			Stock.builder()
				.tickerSymbol("345678")
				.companyName("company3")
				.companyNameEng("company3")
				.stockCode("KRX70000345678")
				.sector("전기전자")
				.market(Market.KOSPI)
				.build(),
			Stock.builder()
				.tickerSymbol("456789")
				.companyName("company4")
				.companyNameEng("company4")
				.stockCode("KRX70000456789")
				.sector("의약품")
				.market(Market.KOSDAQ)
				.build()
		));

		Set<StockDataResponse.StockInfo> fetchStockInfoResult = Set.of(
			StockDataResponse.StockInfo.of("KR70000123456", "123456", "company1", "company1", "KOSPI"),
			StockDataResponse.StockInfo.of("KR70000234567", "234567", "company2", "company2", "KOSPI")
		);
		given(krxService.fetchStockInfo())
			.willReturn(fetchStockInfoResult);

		Map<String, StockSectorResponse.SectorInfo> sectorMapResult = Map.of(
			"123456", StockSectorResponse.SectorInfo.of("123456", "전기전자", "KOSPI"),
			"234567", StockSectorResponse.SectorInfo.of("234567", "의약품", "KOSDAQ")
		);
		given(krxService.fetchSectorInfo())
			.willReturn(sectorMapResult);
		// when
		StockRefreshResponse response = stockService.refreshStocks();

		// then
		List<String> addedStocks = List.of("123456", "234567");
		List<String> deletedStocks = List.of("345678", "456789");
		assertAll(
			() -> assertThat(response.getAddedStocks())
				.hasSize(2)
				.containsExactlyInAnyOrderElementsOf(addedStocks),
			() -> assertThat(response.getDeletedStocks())
				.hasSize(2)
				.containsExactlyInAnyOrderElementsOf(deletedStocks),
			() -> assertThat(stockRepository.findAllByTickerSymbols(addedStocks))
				.hasSize(2)
		);
	}

	@DisplayName("사용자는 종목 정보를 상세 조회합니다")
	@Test
	void getStock() {
		// given
		Stock stock = stockRepository.save(createStock());
		stockDividendRepository.saveAll(createStockDividendWith(stock));

		ValueOperations valueOperationMock = Mockito.mock(ValueOperations.class);
		given(redisTemplate.opsForValue())
			.willReturn(valueOperationMock);
		given(valueOperationMock.get("cp:005930"))
			.willReturn("50000");
		given(valueOperationMock.get("lastDayClosingPrice:005930"))
			.willReturn("49000");

		String tickerSymbol = "005930";
		// when
		StockResponse response = stockService.getStock(tickerSymbol);
		// then
		Assertions.assertAll(
			() -> assertThat(response)
				.extracting(
					StockResponse::getStockCode,
					StockResponse::getTickerSymbol,
					StockResponse::getCompanyName,
					StockResponse::getCompanyNameEng,
					StockResponse::getMarket,
					StockResponse::getCurrentPrice,
					StockResponse::getDailyChange,
					StockResponse::getDailyChangeRate,
					StockResponse::getSector,
					StockResponse::getAnnualDividend,
					StockResponse::getAnnualDividendYield)
				.usingComparatorForType(Money::compareTo, Money.class)
				.containsExactlyInAnyOrder(
					stock.getStockCode(),
					stock.getTickerSymbol(),
					stock.getCompanyName(),
					stock.getCompanyNameEng(),
					stock.getMarket(),
					Money.won(50000),
					Money.won(1000),
					2.04,
					stock.getSector(),
					Money.won(1083),
					2.17
				)
		);
	}

	@DisplayName("사용자가 종목 상세 정보 조회시 종목의 현재가 및 종가가 없는 경우 서버로부터 조회하여 가져온다")
	@Test
	void getStock_whenPriceIsNotExist_thenFetchCurrentPrice() {
		// given
		Stock stock = stockRepository.save(createStock());
		stockDividendRepository.saveAll(createStockDividendWith(stock));

		ValueOperations valueOperationMock = Mockito.mock(ValueOperations.class);
		given(redisTemplate.opsForValue())
			.willReturn(valueOperationMock);
		given(valueOperationMock.get("cp:005930"))
			.willReturn(null)
			.willReturn("50000");
		given(valueOperationMock.get("lastDayClosingPrice:005930"))
			.willReturn(null)
			.willReturn("49000");
		given(kisClient.fetchAccessToken())
			.willReturn(Mono.just(
				new KisAccessToken("accessToken", "Bearer", LocalDateTime.now().plusDays(1), 3600 * 24)));
		given(kisClient.fetchCurrentPrice(anyString(), anyString()))
			.willReturn(Mono.just(KisCurrentPrice.create(stock.getTickerSymbol(), 50000L)));
		given(kisClient.fetchClosingPrice(anyString(), anyString()))
			.willReturn(Mono.just(KisClosingPrice.create(stock.getTickerSymbol(), 49000L)));

		String tickerSymbol = "005930";
		// when
		StockResponse response = stockService.getStock(tickerSymbol);
		// then
		Assertions.assertAll(
			() -> assertThat(response)
				.extracting(
					StockResponse::getStockCode,
					StockResponse::getTickerSymbol,
					StockResponse::getCompanyName,
					StockResponse::getCompanyNameEng,
					StockResponse::getMarket,
					StockResponse::getCurrentPrice,
					StockResponse::getDailyChange,
					StockResponse::getDailyChangeRate,
					StockResponse::getSector,
					StockResponse::getAnnualDividend,
					StockResponse::getAnnualDividendYield)
				.usingComparatorForType(Money::compareTo, Money.class)
				.containsExactlyInAnyOrder(
					stock.getStockCode(),
					stock.getTickerSymbol(),
					stock.getCompanyName(),
					stock.getCompanyNameEng(),
					stock.getMarket(),
					Money.won(50000),
					Money.won(1000),
					2.04,
					stock.getSector(),
					Money.won(1083),
					2.17
				)
		);

	}

	private Stock createStock() {
		return Stock.builder()
			.companyName("삼성전자보통주")
			.tickerSymbol("005930")
			.companyNameEng("SamsungElectronics")
			.stockCode("KR7005930003")
			.sector("전기전자")
			.market(Market.KOSPI)
			.build();
	}

	private StockDividend createStockDividend(LocalDate exDividendDate, LocalDate recordDate, LocalDate paymentDate,
		Stock stock) {
		return StockDividend.builder()
			.dividend(Money.won(361L))
			.exDividendDate(exDividendDate)
			.recordDate(recordDate)
			.paymentDate(paymentDate)
			.stock(stock)
			.build();
	}

	private List<StockDividend> createStockDividendWith(Stock stock) {
		return List.of(
			createStockDividend(
				LocalDate.of(2022, 12, 30),
				LocalDate.of(2022, 12, 31),
				LocalDate.of(2023, 4, 14),
				stock),
			createStockDividend(
				LocalDate.of(2023, 3, 30),
				LocalDate.of(2023, 3, 31),
				LocalDate.of(2023, 5, 17),
				stock),
			createStockDividend(
				LocalDate.of(2023, 6, 29),
				LocalDate.of(2023, 6, 30),
				LocalDate.of(2023, 8, 16),
				stock),
			createStockDividend(
				LocalDate.of(2023, 9, 27),
				LocalDate.of(2023, 9, 30),
				LocalDate.of(2023, 11, 20),
				stock),
			createStockDividend(
				LocalDate.of(2024, 3, 29),
				LocalDate.of(2024, 3, 31),
				LocalDate.of(2024, 5, 17),
				stock),
			createStockDividend(
				LocalDate.of(2024, 6, 28),
				LocalDate.of(2024, 6, 30),
				LocalDate.of(2024, 8, 16),
				stock),
			createStockDividend(
				LocalDate.of(2024, 9, 27),
				LocalDate.of(2024, 9, 30),
				LocalDate.of(2024, 11, 20),
				stock)
		);
	}
}
