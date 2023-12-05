package codesquad.fineants.domain.stock_dividend;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.stock.Stock;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString(exclude = {"stock"})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "stock_dividend", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"ticker_symbol", "recordDate"})
})
@Entity
public class StockDividend extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Long dividend;

	@Column(nullable = false)
	private LocalDate exDividendDate;
	@Column(nullable = false)
	private LocalDate recordDate;
	@Column(nullable = true)
	private LocalDate paymentDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ticker_symbol", referencedColumnName = "tickerSymbol")
	private Stock stock;

	@Builder
	public StockDividend(Long id, LocalDate exDividendDate, LocalDate recordDate,
		LocalDate paymentDate, Long dividend, Stock stock) {
		this.id = id;
		this.exDividendDate = exDividendDate;
		this.recordDate = recordDate;
		this.paymentDate = paymentDate;
		this.dividend = dividend;
		this.stock = stock;
	}

	// 주식 개수에 따른 배당금 합계 계산
	// 배당금 합계 = 주당 배당금 * 주식 개수
	public long calculateDividendSum(Long numShares) {
		return dividend * numShares;
	}

	// 배당금을 받을 수 있는지 검사
	public boolean isSatisfied(LocalDate purchaseDate) {
		return purchaseDate.isBefore(exDividendDate);
	}

	// 현금 배당 지급일의 월을 반환
	public int getMonthValueByPaymentDate() {
		return paymentDate.getMonthValue();
	}
}
