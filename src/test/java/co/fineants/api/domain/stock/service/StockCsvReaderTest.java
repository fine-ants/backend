package co.fineants.api.domain.stock.service;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.api.domain.dividend.domain.calculator.FileExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.reader.HolidayFileReader;
import co.fineants.api.domain.kis.repository.FileHolidayRepository;
import co.fineants.api.domain.stock.domain.entity.Market;
import co.fineants.api.domain.stock.domain.entity.Stock;

class StockCsvReaderTest {

	@DisplayName("csv 파일을 참조하여 종목을 조회한다")
	@Test
	void readStockCsv() {
		// given
		StockCsvReader reader = new StockCsvReader(
			new FileExDividendDateCalculator(new FileHolidayRepository(new HolidayFileReader())));
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
			.containsExactly("KR7446540007", "446540", "메가터치", "Megatouch Co., Ltd", Market.KOSDAQ, "기타");
	}

}
