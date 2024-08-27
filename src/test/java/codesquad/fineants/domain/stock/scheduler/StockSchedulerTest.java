package codesquad.fineants.domain.stock.scheduler;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.dividend.repository.StockDividendRepository;
import codesquad.fineants.domain.kis.client.KisClient;
import codesquad.fineants.domain.kis.domain.dto.response.KisDividend;
import codesquad.fineants.domain.kis.domain.dto.response.KisSearchStockInfo;
import codesquad.fineants.domain.kis.service.KisService;
import codesquad.fineants.domain.stock.domain.dto.response.StockDataResponse;
import codesquad.fineants.domain.stock.domain.entity.Market;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockRepository;
import codesquad.fineants.domain.stock.service.StockCsvReader;
import codesquad.fineants.global.common.delay.DelayManager;
import codesquad.fineants.infra.s3.service.AmazonS3DividendService;
import codesquad.fineants.infra.s3.service.AmazonS3StockService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class StockSchedulerTest extends AbstractContainerBaseTest {

	@Autowired
	private StockScheduler stockScheduler;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockDividendRepository stockDividendRepository;

	@Autowired
	private StockCsvReader stockCsvReader;

	@Autowired
	private AmazonS3StockService amazonS3StockService;

	@Autowired
	private AmazonS3DividendService amazonS3DividendService;

	@MockBean
	private KisClient kisClient;

	@MockBean
	private KisService kisService;

	@SpyBean
	private DelayManager delayManager;

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
			.willReturn(Flux.just(stock));
		given(kisService.fetchSearchStockInfo(stock.getTickerSymbol()))
			.willReturn(Mono.just(
				KisSearchStockInfo.listedStock(stock.getStockCode(), stock.getTickerSymbol(), stock.getCompanyName(),
					stock.getCompanyNameEng(), "STK", "전기,전자")));
		stocks.forEach(s -> given(kisService.fetchSearchStockInfo(s.getTickerSymbol()))
			.willReturn(Mono.just(KisSearchStockInfo.listedStock(
					s.getStockCode(),
					s.getTickerSymbol(),
					s.getCompanyName(),
					s.getCompanyNameEng(),
					"STK",
					s.getSector()
				))
			));
		stocks.forEach(s -> given(kisService.fetchDividend(anyString()))
			.willReturn(Flux.empty()));
		stocks.forEach(s -> given(kisService.fetchDividend(s.getTickerSymbol()))
			.willReturn(Flux.just(KisDividend.create(s.getTickerSymbol(), Money.won(300), LocalDate.of(2024, 3, 1),
					LocalDate.of(2024, 5, 1)),
				KisDividend.create(s.getTickerSymbol(), Money.won(300), LocalDate.of(2024, 5, 1),
					LocalDate.of(2024, 7, 1)))));
		given(delayManager.delay()).willReturn(Duration.ZERO);
		// when
		stockScheduler.scheduledReloadStocks();
		// then
		assertThat(stockRepository.findByTickerSymbol("000660")).isPresent();
		assertThat(amazonS3StockService.fetchStocks())
			.as("Verify that the stock information in the stocks.csv file stored "
				+ "in s3 matches the items in the database")
			.containsExactlyInAnyOrderElementsOf(stockRepository.findAll());
		assertThat(amazonS3DividendService.fetchDividends())
			.as("Verify that the dividend information in the dividends.csv file stored "
				+ "in s3 matches the items in the database")
			.containsExactlyInAnyOrderElementsOf(stockDividendRepository.findAllStockDividends());
	}

	private List<Stock> saveStocks() {
		List<Stock> stocks = stockCsvReader.readStockCsv().stream()
			.limit(100)
			.toList();
		return stockRepository.saveAll(stocks);
	}
}
