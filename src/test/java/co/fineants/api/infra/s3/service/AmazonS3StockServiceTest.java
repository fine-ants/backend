package co.fineants.api.infra.s3.service;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.stock.domain.entity.Market;
import co.fineants.api.domain.stock.domain.entity.Stock;

class AmazonS3StockServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private AmazonS3StockService amazonS3StockService;

	@DisplayName("종목 정보를 csv 파일에 저장한다")
	@Test
	void writeStocks() {
		// given
		Stock stock = Stock.of("000370", "한화손해보험보통주", "\"Hanwha General Insurance Co.,Ltd.\"", "KR7000370007", "보험",
			Market.KOSPI);
		// when
		amazonS3StockService.writeStocks(List.of(stock));
		// then
		Stock findStock = amazonS3StockService.fetchStocks().stream()
			.findAny()
			.orElseThrow();
		Assertions.assertThat(findStock)
			.extracting(Stock::getTickerSymbol, Stock::getCompanyName, Stock::getCompanyNameEng, Stock::getStockCode,
				Stock::getSector, Stock::getMarket)
			.containsExactly("000370", "한화손해보험보통주", "\"Hanwha General Insurance Co.,Ltd.\"", "KR7000370007", "보험",
				Market.KOSPI);
	}
}
