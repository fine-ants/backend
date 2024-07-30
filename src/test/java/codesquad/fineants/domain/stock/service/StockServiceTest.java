package codesquad.fineants.domain.stock.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.dividend.repository.StockDividendRepository;
import codesquad.fineants.domain.kis.client.KisAccessToken;
import codesquad.fineants.domain.kis.client.KisClient;
import codesquad.fineants.domain.kis.client.KisCurrentPrice;
import codesquad.fineants.domain.kis.domain.dto.response.KisClosingPrice;
import codesquad.fineants.domain.kis.repository.KisAccessTokenRepository;
import codesquad.fineants.domain.kis.service.KisService;
import codesquad.fineants.domain.stock.domain.dto.response.StockDataResponse;
import codesquad.fineants.domain.stock.domain.dto.response.StockRefreshResponse;
import codesquad.fineants.domain.stock.domain.dto.response.StockResponse;
import codesquad.fineants.domain.stock.domain.entity.Market;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockRepository;
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

	@MockBean
	private RedisTemplate<String, String> redisTemplate;

	@MockBean
	private KisClient kisClient;

	@MockBean
	private KisService kisService;

	@AfterEach
	void tearDown() {
		stockDividendRepository.deleteAllInBatch();
		stockRepository.deleteAllInBatch();
	}

	@DisplayName("사용자는 종목 정보를 상세 조회합니다")
	@Test
	void getStock() {
		// given
		Stock stock = stockRepository.save(createSamsungStock());
		stockDividendRepository.saveAll(createStockDividendWith(stock));

		ValueOperations valueOperationMock = Mockito.mock(ValueOperations.class);
		given(redisTemplate.opsForValue())
			.willReturn(valueOperationMock);
		given(valueOperationMock.get("cp:005930"))
			.willReturn("50000");
		given(valueOperationMock.get("lastDayClosingPrice:005930"))
			.willReturn("49000");
		given(kisClient.fetchAccessToken())
			.willReturn(
				Mono.just(new KisAccessToken("accessToken", "Bearer", LocalDateTime.now().plusSeconds(86400), 86400)));

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
	void getStock_whenPriceIsNotExist_thenFetchCurrentPrice() {
		// given
		Stock stock = stockRepository.save(createSamsungStock());
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
		kisAccessTokenRepository.refreshAccessToken(createKisAccessToken());
		StockDataResponse.StockIntegrationInfo stock = StockDataResponse.StockIntegrationInfo.create(
			"000660",
			"에스케이하이닉스보통주",
			"SK hynix",
			"KR7000660001",
			"전기,전자",
			Market.KOSPI);
		BDDMockito.given(kisService.fetchStockInfoInRangedIpo())
			.willReturn(Set.of(stock));
		// when
		StockRefreshResponse response = stockService.reloadStocks();
		// then
		assertThat(response).isNotNull();
		assertThat(response.getAddedStocks()).hasSize(1);
		assertThat(response.getDeletedStocks()).hasSize(1);
	}

	@DisplayName("서버는 종목들을 최신화한다")
	@Test
	void scheduledRefreshStocks() {
		// given
		saveStocks();

		StockDataResponse.StockIntegrationInfo stock = StockDataResponse.StockIntegrationInfo.create(
			"000660",
			"에스케이하이닉스보통주",
			"SK hynix",
			"KR7000660001",
			"전기,전자",
			Market.KOSPI);
		BDDMockito.given(kisService.fetchStockInfoInRangedIpo())
			.willReturn(Set.of(stock));
		// when
		stockService.scheduledReloadStocks();
		// then
		assertThat(stockRepository.findByTickerSymbol("000660")).isPresent();
	}

	private void saveStocks() {
		Set<StockDataResponse.StockInfo> stockInfoSet = stockCsvReader.readStockCsv();
		List<Stock> stocks = stockInfoSet.stream()
			.map(StockDataResponse.StockInfo::toEntity)
			.toList();
		stockRepository.saveAll(stocks);
	}
}
