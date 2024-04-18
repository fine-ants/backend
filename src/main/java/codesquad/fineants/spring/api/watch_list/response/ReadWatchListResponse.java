package codesquad.fineants.spring.api.watch_list.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import codesquad.fineants.domain.common.money.Money;
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
		private long id;
		private String companyName;
		private String tickerSymbol;
		private Money currentPrice;
		private Money dailyChange;
		private Double dailyChangeRate;
		private Double annualDividendYield;
		private String sector;
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private LocalDateTime dateAdded;
	}

	public static ReadWatchListResponse.WatchStockResponse from(WatchStock watchStock,
		CurrentPriceManager currentPriceManager,
		LastDayClosingPriceManager lastDayClosingPriceManager) {
		return ReadWatchListResponse.WatchStockResponse.builder()
			.id(watchStock.getId())
			.companyName(watchStock.getStock().getCompanyName())
			.tickerSymbol(watchStock.getStock().getTickerSymbol())
			.currentPrice(watchStock.getStock().getCurrentPrice(currentPriceManager))
			.dailyChange(watchStock.getStock()
				.getDailyChange(currentPriceManager, lastDayClosingPriceManager))
			.dailyChangeRate(watchStock.getStock()
				.getDailyChangeRate(currentPriceManager, lastDayClosingPriceManager))
			.annualDividendYield(watchStock.getStock().getAnnualDividendYield(currentPriceManager))
			.sector(watchStock.getStock().getSector())
			.dateAdded(watchStock.getCreateAt())
			.build();
	}
}
