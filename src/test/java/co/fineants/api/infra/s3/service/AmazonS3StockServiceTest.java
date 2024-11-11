package co.fineants.api.infra.s3.service;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

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
	void givenStockInfo_whenWriteStocks_thenSaveFile() {
		// given
		Stock stock = Stock.of("000370", "한화손해보험보통주", "\"Hanwha General Insurance Co.,Ltd.\"", "KR7000370007", "보험",
			Market.KOSPI);
		// when
		amazonS3StockService.writeStocks(List.of(stock));
		// then
		Stock findStock = amazonS3StockService.fetchStocks().stream()
			.findAny()
			.orElseThrow();
		assertThat(findStock)
			.extracting(Stock::getTickerSymbol, Stock::getCompanyName, Stock::getCompanyNameEng, Stock::getStockCode,
				Stock::getSector, Stock::getMarket)
			.containsExactly("000370", "한화손해보험보통주", "\"Hanwha General Insurance Co.,Ltd.\"", "KR7000370007", "보험",
				Market.KOSPI);
	}

	@DisplayName("종목 정보를 S3에서 가져온 다음에 csv 파일에 작성하면 정상적으로 CSV 파일에 저장된다")
	@Test
	void givenStocks_whenWriteStocks_thenSaveCsvFile() {
		// given
		List<Stock> stocks = amazonS3StockService.fetchStocks();
		// when
		amazonS3StockService.writeStocks(stocks);
		// then
		List<Stock> actual = amazonS3StockService.fetchStocks();
		assertThat(actual).hasSize(2803);

		Stock kakaopay = actual.stream()
			.filter(stock -> stock.getTickerSymbol().equals("377300"))
			.findAny()
			.orElseThrow();
		assertThat(kakaopay.getSector()).isEqualTo("기타금융");
		assertThat(kakaopay.getMarket()).isEqualTo(Market.KOSPI);

		List<String> marketNames = Arrays.stream(Market.values())
			.map(Market::name)
			.toList();
		assertThat(actual.stream()
			.map(Stock::getSector)
			.noneMatch(marketNames::contains)).isTrue();
	}

	@DisplayName("아마존 S3에 stock.csv가 주어지고 파싱하여 Stock 컬렉션으로 가져온다")
	@Test
	void givenCsvFile_whenFetchStocks_thenReturnCollectionOfStocks() {
		// given

		// when
		List<Stock> stocks = amazonS3StockService.fetchStocks();
		// then
		assertThat(stocks).hasSize(2803);
		Stock kakaoPay = stocks.stream()
			.filter(stock -> stock.getTickerSymbol().equals("377300"))
			.findAny()
			.orElseThrow();
		assertThat(kakaoPay.getSector()).isEqualTo("기타금융");
		assertThat(kakaoPay.getMarket()).isEqualTo(Market.KOSPI);
	}
}
