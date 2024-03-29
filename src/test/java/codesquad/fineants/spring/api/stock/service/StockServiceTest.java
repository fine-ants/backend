package codesquad.fineants.spring.api.stock.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.spring.AbstractContainerBaseTest;
import codesquad.fineants.spring.api.krx.service.KRXService;
import codesquad.fineants.spring.api.stock.response.StockDataResponse;
import codesquad.fineants.spring.api.stock.response.StockRefreshResponse;
import codesquad.fineants.spring.api.stock.response.StockSectorResponse;

class StockServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private StockService stockService;

	@Autowired
	private StockRepository stockRepository;

	@MockBean
	private KRXService krxService;

	@AfterEach
	void tearDown() {
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

}
