package codesquad.fineants.spring.api.stock.response;

import java.util.List;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
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
	private Double dailyChangeRate;
	private String sector;
	private Money annualDividend;
	private Double annualDividendYield;
	private List<Integer> dividendMonths;

	public static StockResponse create(String stockCode, String tickerSymbol, String companyName,
		String companyNameEng, Market market, Money currentPrice, Money dailyChange, Double dailyChangeRate,
		String sector, Money annualDividend, Double annualDividendYield, List<Integer> dividendMonths) {
		return StockResponse.builder()
			.stockCode(stockCode)
			.tickerSymbol(tickerSymbol)
			.companyName(companyName)
			.companyNameEng(companyNameEng)
			.market(market)
			.currentPrice(currentPrice)
			.dailyChange(dailyChange)
			.dailyChangeRate(dailyChangeRate)
			.sector(sector)
			.annualDividend(annualDividend)
			.annualDividendYield(annualDividendYield)
			.dividendMonths(dividendMonths)
			.build();
	}

	public static StockResponse of(Stock stock, CurrentPriceManager currentPriceManager,
		LastDayClosingPriceManager lastDayClosingPriceManager) {
		return StockResponse.builder()
			.stockCode(stock.getStockCode())
			.tickerSymbol(stock.getTickerSymbol())
			.companyName(stock.getCompanyName())
			.companyNameEng(stock.getCompanyNameEng())
			.market(stock.getMarket())
			.currentPrice(stock.getCurrentPrice(currentPriceManager))
			.dailyChange(stock.getDailyChange(currentPriceManager, lastDayClosingPriceManager))
			.dailyChangeRate(stock.getDailyChangeRate(currentPriceManager, lastDayClosingPriceManager))
			.sector(stock.getSector())
			.annualDividend(stock.getAnnualDividend())
			.annualDividendYield(stock.getAnnualDividendYield(currentPriceManager))
			.dividendMonths(stock.getDividendMonths())
			.build();
	}
}
