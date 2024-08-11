package codesquad.fineants.domain.stock.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.dividend.domain.entity.StockDividend;
import codesquad.fineants.domain.dividend.repository.StockDividendRepository;
import codesquad.fineants.domain.kis.client.KisAccessToken;
import codesquad.fineants.domain.kis.client.KisClient;
import codesquad.fineants.domain.kis.client.KisCurrentPrice;
import codesquad.fineants.domain.kis.domain.dto.response.KisClosingPrice;
import codesquad.fineants.domain.kis.domain.dto.response.KisDividend;
import codesquad.fineants.domain.kis.domain.dto.response.KisSearchStockInfo;
import codesquad.fineants.domain.kis.repository.ClosingPriceRepository;
import codesquad.fineants.domain.kis.repository.CurrentPriceRepository;
import codesquad.fineants.domain.kis.repository.KisAccessTokenRepository;
import codesquad.fineants.domain.kis.service.KisService;
import codesquad.fineants.domain.stock.domain.dto.response.StockDataResponse;
import codesquad.fineants.domain.stock.domain.dto.response.StockReloadResponse;
import codesquad.fineants.domain.stock.domain.dto.response.StockResponse;
import codesquad.fineants.domain.stock.domain.entity.Market;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockRepository;
import codesquad.fineants.global.common.delay.DelayManager;
import codesquad.fineants.infra.s3.service.AmazonS3DividendService;
import codesquad.fineants.infra.s3.service.AmazonS3StockService;
import reactor.core.publisher.Mono;

class StockServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private StockService stockService;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockDividendRepository stockDividendRepository;

	@Autowired
	private StockCsvReader stockCsvReader;

	@Autowired
	private KisAccessTokenRepository kisAccessTokenRepository;

	@Autowired
	private CurrentPriceRepository currentPriceRepository;

	@Autowired
	private ClosingPriceRepository closingPriceRepository;

	@Autowired
	private AmazonS3StockService amazonS3StockService;

	@Autowired
	private AmazonS3DividendService amazonS3DividendService;

	@MockBean
	private KisClient kisClient;

	@MockBean
	private KisService kisService;

	@MockBean
	private DelayManager delayManager;

	@AfterEach
	void tearDown() {
		stockDividendRepository.deleteAllInBatch();
		stockRepository.deleteAllInBatch();
	}

	@DisplayName("사용자는 종목 정보를 상세 조회합니다")
	@Test
	void getDetailedStock() {
		// given
		Stock stock = stockRepository.save(createSamsungStock());
		stockDividendRepository.saveAll(createStockDividendWith(stock));

		currentPriceRepository.addCurrentPrice(KisCurrentPrice.create("005930", 50000L));
		closingPriceRepository.addPrice(KisClosingPrice.create("005930", 49000L));
		given(kisClient.fetchAccessToken())
			.willReturn(
				Mono.just(new KisAccessToken("accessToken", "Bearer", LocalDateTime.now().plusSeconds(86400), 86400)));

		String tickerSymbol = "005930";
		// when
		StockResponse response = stockService.getDetailedStock(tickerSymbol);
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
				.usingComparatorForType(Percentage::compareTo, Percentage.class)
				.containsExactlyInAnyOrder(
					stock.getStockCode(),
					stock.getTickerSymbol(),
					stock.getCompanyName(),
					stock.getCompanyNameEng(),
					stock.getMarket(),
					Money.won(50000),
					Money.won(1000),
					Percentage.from(0.0204),
					stock.getSector(),
					Money.won(1083),
					Percentage.from(0.0217)
				)
		);
	}

	@DisplayName("사용자가 종목 상세 정보 조회시 종목의 현재가 및 종가가 없는 경우 서버로부터 조회하여 가져온다")
	@Test
	void getDetailedStock_whenPriceIsNotExist_thenFetchCurrentPrice() {
		// given
		Stock stock = stockRepository.save(createSamsungStock());
		stockDividendRepository.saveAll(createStockDividendWith(stock));

		given(kisClient.fetchAccessToken())
			.willReturn(
				Mono.just(new KisAccessToken("accessToken", "Bearer", LocalDateTime.now().plusDays(1), 3600 * 24)));
		given(kisClient.fetchCurrentPrice(anyString(), anyString()))
			.willReturn(Mono.just(KisCurrentPrice.create(stock.getTickerSymbol(), 50000L)));
		given(kisClient.fetchClosingPrice(anyString(), anyString()))
			.willReturn(Mono.just(KisClosingPrice.create(stock.getTickerSymbol(), 49000L)));

		String tickerSymbol = "005930";
		// when
		StockResponse response = stockService.getDetailedStock(tickerSymbol);
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
				.usingComparatorForType(Percentage::compareTo, Percentage.class)
				.containsExactlyInAnyOrder(
					stock.getStockCode(),
					stock.getTickerSymbol(),
					stock.getCompanyName(),
					stock.getCompanyNameEng(),
					stock.getMarket(),
					Money.won(50000),
					Money.won(1000),
					Percentage.from(0.0204),
					stock.getSector(),
					Money.won(1083),
					Percentage.from(0.0217)
				)
		);
	}

	@DisplayName("상장된 종목과 폐지된 종목을 조회하여 최신화한다")
	@Test
	void reloadStocks() {
		// given
		Stock nokwon = stockRepository.save(createNokwonCI());

		kisAccessTokenRepository.refreshAccessToken(createKisAccessToken());
		StockDataResponse.StockIntegrationInfo hynix = StockDataResponse.StockIntegrationInfo.create(
			"000660",
			"에스케이하이닉스보통주",
			"SK hynix",
			"KR7000660001",
			"전기,전자",
			Market.KOSPI);
		given(kisService.fetchStockInfoInRangedIpo())
			.willReturn(Set.of(hynix));
		given(kisService.fetchSearchStockInfo(hynix.getTickerSymbol()))
			.willReturn(Mono.just(KisSearchStockInfo.listedStock(
					"KR7000660001",
					"000660",
					"에스케이하이닉스보통주",
					"SK hynix",
					"STK",
					"전기,전자")
				)
			);
		given(kisService.fetchSearchStockInfo(nokwon.getTickerSymbol()))
			.willReturn(Mono.just(
				KisSearchStockInfo.delistedStock("KR7065560005", "065560", "녹원씨엔아이",
					"Nokwon Commercials & Industries, Inc.",
					"KSQ", "소프트웨어", LocalDate.of(2024, 7, 29))));
		DateTimeFormatter dtf = DateTimeFormatter.BASIC_ISO_DATE;
		given(kisService.fetchDividend(hynix.getTickerSymbol()))
			.willReturn(Mono.just(List.of(
				KisDividend.create(hynix.getTickerSymbol(),
					Money.won(300),
					LocalDate.parse("20240331", dtf),
					LocalDate.parse("20240514", dtf)),
				KisDividend.create(hynix.getTickerSymbol(),
					Money.won(300),
					LocalDate.parse("20240630", dtf),
					LocalDate.parse("20240814", dtf))
			)));
		given(delayManager.getDelay()).willReturn(Duration.ZERO);
		// when
		StockReloadResponse response = stockService.reloadStocks();
		// then
		assertThat(response).isNotNull();
		assertThat(response.getAddedStocks()).hasSize(1);
		assertThat(response.getDeletedStocks()).hasSize(1);

		Stock deletedStock = stockRepository.findByTickerSymbol(nokwon.getTickerSymbol()).orElseThrow();
		assertThat(deletedStock.isDeleted()).isTrue();

		List<StockDividend> hynixDividends = stockDividendRepository.findStockDividendsByTickerSymbol(
			hynix.getTickerSymbol());
		assertThat(hynixDividends)
			.hasSize(2)
			.extracting(StockDividend::getDividend, StockDividend::getRecordDate, StockDividend::getExDividendDate,
				StockDividend::getPaymentDate)
			.usingComparatorForType(Money::compareTo, Money.class)
			.containsExactly(
				Tuple.tuple(
					Money.won(300),
					LocalDate.parse("20240331", dtf),
					LocalDate.parse("20240329", dtf),
					LocalDate.parse("20240514", dtf)
				),
				Tuple.tuple(
					Money.won(300),
					LocalDate.parse("20240630", dtf),
					LocalDate.parse("20240628", dtf),
					LocalDate.parse("20240814", dtf)
				)
			);
	}

	@DisplayName("서버는 종목들을 최신화한다")
	@Test
	void scheduledRefreshStocks() {
		// given
		List<Stock> stocks = saveStocks();
		StockDataResponse.StockIntegrationInfo stock = StockDataResponse.StockIntegrationInfo.create(
			"000660",
			"에스케이하이닉스보통주",
			"SK hynix",
			"KR7000660001",
			"전기,전자",
			Market.KOSPI);
		given(kisService.fetchStockInfoInRangedIpo())
			.willReturn(Set.of(stock));
		given(kisService.fetchSearchStockInfo(stock.getTickerSymbol()))
			.willReturn(Mono.just(
				KisSearchStockInfo.listedStock(stock.getStockCode(), stock.getTickerSymbol(), stock.getCompanyName(),
					stock.getCompanyNameEng(), "STK", "전기,전자")));
		stocks.forEach(s -> given(kisService.fetchSearchStockInfo(s.getTickerSymbol()))
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
			));
		stocks.forEach(s -> given(kisService.fetchDividend(anyString()))
			.willReturn(Mono.just(Collections.emptyList())));
		stocks.forEach(s -> given(kisService.fetchDividend(s.getTickerSymbol()))
			.willReturn(Mono.just(List.of(
				KisDividend.create(s.getTickerSymbol(), Money.won(300), LocalDate.of(2024, 3, 1),
					LocalDate.of(2024, 5, 1)),
				KisDividend.create(s.getTickerSymbol(), Money.won(300), LocalDate.of(2024, 5, 1),
					LocalDate.of(2024, 7, 1)))))
		);
		given(delayManager.getDelay()).willReturn(Duration.ZERO);
		// when
		stockService.scheduledReloadStocks();
		// then
		assertThat(stockRepository.findByTickerSymbol("000660")).isPresent();
		assertThat(amazonS3StockService.fetchStocks())
			.as("Verify that the stock information in the stocks.txt file stored in s3 matches the items in the database")
			.containsExactlyInAnyOrderElementsOf(stockRepository.findAll());
		assertThat(amazonS3DividendService.fetchDividends())
			.as("Verify that the dividend information in the dividends.csv file stored in s3 matches the items in the database")
			.containsExactlyInAnyOrderElementsOf(stockDividendRepository.findAllStockDividends());
	}

	private List<Stock> saveStocks() {
		Set<StockDataResponse.StockInfo> stockInfoSet = stockCsvReader.readStockCsv();
		List<Stock> stocks = stockInfoSet.stream()
			.map(StockDataResponse.StockInfo::toEntity)
			.limit(100)
			.toList();
		return stockRepository.saveAll(stocks);
	}
}
