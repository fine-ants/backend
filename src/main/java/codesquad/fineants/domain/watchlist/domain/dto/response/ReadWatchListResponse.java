package codesquad.fineants.domain.watchlist.domain.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import codesquad.fineants.domain.common.money.Bank;
import codesquad.fineants.domain.common.money.Currency;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.kis.repository.ClosingPriceRepository;
import codesquad.fineants.domain.kis.repository.CurrentPriceRepository;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.watchlist.domain.entity.WatchStock;
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
		CurrentPriceRepository currentPriceRepository, ClosingPriceRepository closingPriceRepository) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		Stock stock = watchStock.getStock();
		return ReadWatchListResponse.WatchStockResponse.builder()
			.id(watchStock.getId())
			.companyName(stock.getCompanyName())
			.tickerSymbol(stock.getTickerSymbol())
			.currentPrice(stock.getCurrentPrice(currentPriceRepository).reduce(bank, to))
			.dailyChange(stock
				.getDailyChange(currentPriceRepository, closingPriceRepository)
				.reduce(bank, to))
			.dailyChangeRate(stock
				.getDailyChangeRate(currentPriceRepository, closingPriceRepository)
				.toPercentage(bank, to))
			.annualDividendYield(stock
				.getAnnualDividendYield(currentPriceRepository)
				.toPercentage(bank, to))
			.sector(stock.getSector())
			.dateAdded(watchStock.getCreateAt())
			.build();
	}
}
