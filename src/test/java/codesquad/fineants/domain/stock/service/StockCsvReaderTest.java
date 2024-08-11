package codesquad.fineants.domain.stock.service;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import codesquad.fineants.domain.stock.domain.entity.Market;
import codesquad.fineants.domain.stock.domain.entity.Stock;

class StockCsvReaderTest {

	@DisplayName("csv 파일을 참조하여 종목을 조회한다")
	@Test
	void readStockCsv() {
		// given
		StockCsvReader reader = new StockCsvReader();
		// when
		Set<Stock> stocks = reader.readStockCsv();
		// then
		Stock megatouch = stocks.stream()
			.filter(stock -> "KR7446540007".equals(stock.getStockCode()))
			.findAny()
			.orElseThrow();
		Assertions.assertThat(megatouch)
			.extracting(Stock::getStockCode, Stock::getTickerSymbol, Stock::getCompanyName, Stock::getCompanyNameEng,
				Stock::getMarket, Stock::getSector)
			.containsExactly("KR7446540007", "446540", "메가터치", "Megatouch Co., Ltd", Market.KOSDAQ, "반도체");
	}

}
