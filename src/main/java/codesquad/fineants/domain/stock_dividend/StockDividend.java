package codesquad.fineants.domain.stock_dividend;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.stock.Stock;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class StockDividend extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private LocalDateTime dividendMonth;
	private Long dividend;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ticker_symbol", referencedColumnName = "tickerSymbol")
	private Stock stock;

	@Builder
	public StockDividend(Long id, LocalDateTime dividendMonth, Long dividend, Stock stock) {
		this.id = id;
		this.dividendMonth = dividendMonth;
		this.dividend = dividend;
		this.stock = stock;
	}

	public boolean isMonthlyDividend(LocalDateTime monthDateTime) {
		return dividendMonth.getYear() == monthDateTime.getYear()
			&& dividendMonth.getMonthValue() == monthDateTime.getMonthValue();
	}
}
