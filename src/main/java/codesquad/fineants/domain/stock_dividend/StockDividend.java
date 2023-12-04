package codesquad.fineants.domain.stock_dividend;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.*;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.stock.Stock;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
	@Column(nullable = false)
	private LocalDate exDividendDate;
	@Column(nullable = false)
	private LocalDate recordDate;
	@Column(nullable = true)
	private LocalDate paymentDate;
	private Long dividend;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ticker_symbol", referencedColumnName = "tickerSymbol")
	private Stock stock;

	@Builder
	public StockDividend(Long id, LocalDate exDividendDate, LocalDate recordDate, LocalDate paymentDate, Long dividend, Stock stock) {
		this.id = id;
		this.exDividendDate = exDividendDate;
		this.recordDate = recordDate;
		this.paymentDate = paymentDate;
		this.dividend = dividend;
		this.stock = stock;
	}
}
