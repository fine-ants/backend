package codesquad.fineants.domain.stock.repository;

import java.util.List;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.stock.domain.dto.response.StockDataResponse;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.service.StockCsvReader;

@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
class StockQueryRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private StockQueryRepository repository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockCsvReader stockCsvReader;

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
		List<Stock> stocks = repository.getSliceOfStock(tickerSymbol, 10, keyword);
		// then
		Assertions.assertThat(stocks.size()).isEqualTo(10);
	}
}
