package codesquad.fineants.domain.stock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.stock_dividend.StockDividend;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString(exclude = "stockDividends")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Stock extends BaseEntity {

	@Id
	private String tickerSymbol;
	private String companyName;
	private String companyNameEng;
	private String stockCode;
	private String sector;
	@Enumerated(value = EnumType.STRING)
	private Market market;

	@OneToMany(mappedBy = "stock", fetch = FetchType.LAZY)
	private final List<StockDividend> stockDividends = new ArrayList<>();

	@Builder
	public Stock(String tickerSymbol, String companyName, String companyNameEng, String stockCode, String sector,
		Market market) {
		this.tickerSymbol = tickerSymbol;
		this.companyName = companyName;
		this.companyNameEng = companyNameEng;
		this.stockCode = stockCode;
		this.sector = sector;
		this.market = market;
	}

	public void addStockDividend(StockDividend stockDividend) {
		if (!stockDividends.contains(stockDividend)) {
			stockDividends.add(stockDividend);
		}
	}

	public List<StockDividend> getCurrentMonthDividends() {
		LocalDate today = LocalDate.now();
		return stockDividends.stream()
			.filter(dividend -> dividend.getPaymentDate() != null)
			.filter(dividend -> dividend.getPaymentDate().getYear() == today.getYear() &&
				dividend.getPaymentDate().getMonth() == today.getMonth())
			.collect(Collectors.toList());
	}

	public List<StockDividend> getCurrentYearDividends() {
		LocalDate today = LocalDate.now();
		return stockDividends.stream()
			.filter(dividend -> dividend.getPaymentDate() != null)
			.filter(dividend -> dividend.getPaymentDate().getYear() == today.getYear())
			.collect(Collectors.toList());
	}

	public Map<Integer, Long> createMonthlyDividends(List<PurchaseHistory> purchaseHistories) {
		Map<Integer, Long> result = new HashMap<>();
		for (int month = 1; month <= 12; month++) {
			result.put(month, 0L);
		}

		for (StockDividend stockDividend : stockDividends) {
			for (PurchaseHistory purchaseHistory : purchaseHistories) {
				if (stockDividend.isSatisfied(purchaseHistory.getPurchaseLocalDate())) {
					int paymentMonth = stockDividend.getMonthValueByPaymentDate();
					long dividendSum = stockDividend.calculateDividendSum(purchaseHistory.getNumShares());
					result.put(paymentMonth, result.getOrDefault(paymentMonth, 0L) + dividendSum);
				}
			}
		}
		return result;
	}

	public float getAnnualDividendYield(CurrentPriceManager manager) {
		long dividends = stockDividends.stream()
			.filter(dividend -> dividend.getPaymentDate().getYear() == LocalDate.now().getYear())
			.mapToLong(StockDividend::getDividend)
			.sum();
		long currentPrice = getCurrentPrice(manager);
		if(currentPrice == 0) return 0;
		return ((float) dividends / currentPrice) * 100;
	}

	public long getDailyChange(CurrentPriceManager currentPriceManager, LastDayClosingPriceManager lastDayClosingPriceManager) {
		return currentPriceManager.getCurrentPrice(tickerSymbol) - lastDayClosingPriceManager.getPrice(tickerSymbol);
	}

	public Long getCurrentPrice(CurrentPriceManager manager) {
		return manager.getCurrentPrice(tickerSymbol);
	}
	public Long getLastDayClosingPrice(LastDayClosingPriceManager manager) {
		return manager.getPrice(tickerSymbol);
	}
}
