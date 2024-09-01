package co.fineants.api.domain.stock.domain.dto.response;

import java.util.List;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.stock.domain.entity.Market;
import co.fineants.api.domain.stock.domain.entity.Stock;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class StockResponse {
	private String stockCode;
	private String tickerSymbol;
	private String companyName;
	private String companyNameEng;
	private Market market;
	private Money currentPrice;
	private Money dailyChange;
	private Percentage dailyChangeRate;
	private String sector;
	private Money annualDividend;
	private Percentage annualDividendYield;
	private List<Integer> dividendMonths;

	public static StockResponse create(String stockCode, String tickerSymbol, String companyName,
		String companyNameEng, Market market, Expression currentPrice, Expression dailyChange,
		Percentage dailyChangeRate, String sector, Expression annualDividend, Percentage annualDividendYield,
		List<Integer> dividendMonths) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		return StockResponse.builder()
			.stockCode(stockCode)
			.tickerSymbol(tickerSymbol)
			.companyName(companyName)
			.companyNameEng(companyNameEng)
			.market(market)
			.currentPrice(currentPrice.reduce(bank, to))
			.dailyChange(dailyChange.reduce(bank, to))
			.dailyChangeRate(dailyChangeRate)
			.sector(sector)
			.annualDividend(annualDividend.reduce(bank, to))
			.annualDividendYield(annualDividendYield)
			.dividendMonths(dividendMonths)
			.build();
	}

	public static StockResponse of(Stock stock, CurrentPriceRedisRepository currentPriceRedisRepository,
		ClosingPriceRepository closingPriceRepository) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		return StockResponse.builder()
			.stockCode(stock.getStockCode())
			.tickerSymbol(stock.getTickerSymbol())
			.companyName(stock.getCompanyName())
			.companyNameEng(stock.getCompanyNameEng())
			.market(stock.getMarket())
			.currentPrice(stock.getCurrentPrice(currentPriceRedisRepository).reduce(bank, to))
			.dailyChange(stock.getDailyChange(currentPriceRedisRepository, closingPriceRepository).reduce(bank, to))
			.dailyChangeRate(stock.getDailyChangeRate(currentPriceRedisRepository, closingPriceRepository).toPercentage(
				bank, to))
			.sector(stock.getSector())
			.annualDividend(stock.getAnnualDividend().reduce(bank, to))
			.annualDividendYield(
				stock.getAnnualDividendYield(currentPriceRedisRepository).toPercentage(bank, to))
			.dividendMonths(stock.getDividendMonths())
			.build();
	}
}
