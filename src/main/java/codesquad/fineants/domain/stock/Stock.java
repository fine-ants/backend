package codesquad.fineants.domain.stock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.stock_dividend.StockDividend;
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
	@Enumerated(value = EnumType.STRING)
	private Market market;

	@OneToMany(mappedBy = "stock", fetch = FetchType.LAZY)
	private final List<StockDividend> stockDividends = new ArrayList<>();

	@Builder
	public Stock(String companyName, String companyNameEng, String stockCode, String tickerSymbol,
		Market market) {
		this.companyName = companyName;
		this.companyNameEng = companyNameEng;
		this.stockCode = stockCode;
		this.tickerSymbol = tickerSymbol;
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
}
