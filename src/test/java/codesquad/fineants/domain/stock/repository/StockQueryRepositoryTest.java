package codesquad.fineants.domain.stock.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.stock.domain.dto.response.StockDataResponse;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.service.StockCsvReader;
import lombok.extern.slf4j.Slf4j;

@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
@Slf4j
class StockQueryRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private StockQueryRepository repository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockCsvReader stockCsvReader;

	private String lastTickerSymbol;

	@BeforeAll
	public void setup() {
		Set<StockDataResponse.StockInfo> stockInfoSet = stockCsvReader.readStockCsv();
		List<Stock> stocks = stockInfoSet.stream()
			.map(StockDataResponse.StockInfo::toEntity)
			.toList();
		stockRepository.saveAll(stocks);
	}

	@AfterAll
	public void tearDown() {
		stockRepository.deleteAllInBatch();
	}

	@DisplayName("사용자는 키워드 없이 종목을 검색한다")
	@Test
	void getSliceOfStock() {
		// given
		String tickerSymbol = null;
		int size = 10;
		String keyword = null;
		// when
		List<Stock> stocks = repository.getSliceOfStock(tickerSymbol, size, keyword);
		// then
		Assertions.assertThat(stocks.size()).isEqualTo(10);
	}

	@DisplayName("종목 검색 시나리오")
	@TestFactory
	Collection<DynamicTest> createGetSliceOfStockTest() {
		return List.of(
			DynamicTest.dynamicTest("사용자는 키워드로 삼성을 입력하고 종목 검색을 한다", () -> {
				// given
				String tickerSymbol = null;
				int size = 10;
				String keyword = "삼성";
				// when
				List<Stock> stocks = repository.getSliceOfStock(tickerSymbol, size, keyword);
				// then
				stocks.forEach(s -> log.debug("stock : {}", s));
				Assertions.assertThat(stocks.size()).isEqualTo(10);

				if (!stocks.isEmpty()) {
					lastTickerSymbol = stocks.get(stocks.size() - 1).getTickerSymbol();
				}
			}),
			DynamicTest.dynamicTest("사용자는 스크롤을 하여 추가적인 종목 검색을 한다", () -> {
				// given
				int size = 10;
				String keyword = "삼성";
				// when
				List<Stock> stocks = repository.getSliceOfStock(lastTickerSymbol, size, keyword);
				// then
				stocks.forEach(s -> log.debug("stock : {}", s));
				Assertions.assertThat(stocks.size()).isEqualTo(10);

				if (!stocks.isEmpty()) {
					lastTickerSymbol = stocks.get(stocks.size() - 1).getTickerSymbol();
				}
			}),
			DynamicTest.dynamicTest("사용자는 스크롤을 하여 남은 종목 전부를 검색한다", () -> {
				// given
				int size = 10;
				String keyword = "삼성";
				// when
				List<Stock> stocks = repository.getSliceOfStock(lastTickerSymbol, size, keyword);
				// then
				stocks.forEach(s -> log.debug("stock : {}", s));
				Assertions.assertThat(stocks.size()).isEqualTo(5);

				if (!stocks.isEmpty()) {
					lastTickerSymbol = stocks.get(stocks.size() - 1).getTickerSymbol();
				}
			})
		);
	}
}
