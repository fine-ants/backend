package codesquad.fineants.spring.api.watch_list.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import codesquad.fineants.domain.common.money.Bank;
import codesquad.fineants.domain.common.money.Currency;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.watch_stock.WatchStock;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReadWatchListResponse {
	private String name;
	private List<WatchStockResponse> watchStocks;

	@Builder
	@Getter
	public static class WatchStockResponse {
		private Long id;
		private String companyName;
		private String tickerSymbol;
		private Money currentPrice;
		private Money dailyChange;
		private Percentage dailyChangeRate;
		private Percentage annualDividendYield;
		private String sector;
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private LocalDateTime dateAdded;
	}

	public static ReadWatchListResponse.WatchStockResponse from(WatchStock watchStock,
		CurrentPriceManager currentPriceManager, LastDayClosingPriceManager lastDayClosingPriceManager) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		Stock stock = watchStock.getStock();
		return ReadWatchListResponse.WatchStockResponse.builder()
			.id(watchStock.getId())
			.companyName(stock.getCompanyName())
			.tickerSymbol(stock.getTickerSymbol())
			.currentPrice(stock.getCurrentPrice(currentPriceManager).reduce(bank, to))
			.dailyChange(stock
				.getDailyChange(currentPriceManager, lastDayClosingPriceManager)
				.reduce(bank, to))
			.dailyChangeRate(stock
				.getDailyChangeRate(currentPriceManager, lastDayClosingPriceManager)
				.toPercentage(bank, to))
			.annualDividendYield(stock
				.getAnnualDividendYield(currentPriceManager)
				.toPercentage(bank, to))
			.sector(stock.getSector())
			.dateAdded(watchStock.getCreateAt())
			.build();
	}
}
