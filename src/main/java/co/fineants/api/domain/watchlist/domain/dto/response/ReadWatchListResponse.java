package co.fineants.api.domain.watchlist.domain.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.watchlist.domain.entity.WatchStock;
import co.fineants.api.global.common.time.LocalDateTimeService;
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
		CurrentPriceRedisRepository currentPriceRedisRepository, ClosingPriceRepository closingPriceRepository,
		LocalDateTimeService localDateTimeService) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		Stock stock = watchStock.getStock();
		return ReadWatchListResponse.WatchStockResponse.builder()
			.id(watchStock.getId())
			.companyName(stock.getCompanyName())
			.tickerSymbol(stock.getTickerSymbol())
			.currentPrice(stock.getCurrentPrice(currentPriceRedisRepository).reduce(bank, to))
			.dailyChange(stock
				.getDailyChange(currentPriceRedisRepository, closingPriceRepository)
				.reduce(bank, to))
			.dailyChangeRate(stock
				.getDailyChangeRate(currentPriceRedisRepository, closingPriceRepository)
				.toPercentage(bank, to))
			.annualDividendYield(stock
				.getAnnualDividendYield(currentPriceRedisRepository, localDateTimeService)
				.toPercentage(bank, to))
			.sector(stock.getSector())
			.dateAdded(watchStock.getCreateAt())
			.build();
	}
}
