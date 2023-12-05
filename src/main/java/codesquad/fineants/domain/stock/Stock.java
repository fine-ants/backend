package codesquad.fineants.domain.stock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
}
