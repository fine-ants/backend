package codesquad.fineants.domain.stock_dividend;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
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
import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.MoneyConverter;
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
	@Convert(converter = MoneyConverter.class)
	@Column(precision = 19, nullable = false)
	private Money dividend;

	@Column(nullable = false)
	private LocalDate exDividendDate;
	@Column(nullable = false)
	private LocalDate recordDate;
	@Column
	private LocalDate paymentDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ticker_symbol", referencedColumnName = "tickerSymbol")
	private Stock stock;

	@Builder
	public StockDividend(Long id, LocalDate exDividendDate, LocalDate recordDate,
		LocalDate paymentDate, Money dividend, Stock stock) {
		this.id = id;
		this.exDividendDate = exDividendDate;
		this.recordDate = recordDate;
		this.paymentDate = paymentDate;
		this.dividend = dividend;
		this.stock = stock;
	}

	// 주식 개수에 따른 배당금 합계 계산
	// 배당금 합계 = 주당 배당금 * 주식 개수
	public Money calculateDividendSum(Count numShares) {
		return dividend.multiply(numShares);
	}

	// 배당금을 받을 수 있는지 검사
	public boolean isSatisfied(LocalDate purchaseDate) {
		return purchaseDate.isBefore(exDividendDate);
	}

	// 현금 배당 지급일의 월을 반환
	public Integer getMonthValueByPaymentDate() {
		return paymentDate.getMonthValue();
	}

	// 배정기준일이 현재 년도인지 검사
	public boolean isCurrentYearRecordDate(LocalDate localDate) {
		return localDate.getYear() == recordDate.getYear();
	}

	// 현금지급일자가 작년도인지 검사
	public boolean isLastYearPaymentDate(LocalDate lastYearLocalDate) {
		return lastYearLocalDate.getYear() == paymentDate.getYear();
	}

	// 입력으로 받은 배당금 데이터들중 배정기준일이 같은 분기에 해당하는 데이터가 존재하는지 검사
	public boolean isDuplicatedRecordDate(List<StockDividend> currentYearStockDividends) {
		return currentYearStockDividends.stream()
			.anyMatch(stockDividend -> stockDividend.getQuarter().equals(getQuarter()));
	}

	private Integer getQuarter() {
		return recordDate.getMonthValue() / 4 + 1;
	}

	// 현금지급일자를 기준으로 현재년도인지 검사
	public boolean isCurrentYearPaymentDate(LocalDate today) {
		return paymentDate != null && paymentDate.getYear() == today.getYear();
	}
}
