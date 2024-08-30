package co.fineants.api.domain.dividend.domain.entity;

import java.time.LocalDate;
import java.util.List;

import co.fineants.api.domain.BaseEntity;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.MoneyConverter;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.stock.domain.entity.Stock;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString(exclude = {"stock"})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"dividendDates", "stock"}, callSuper = false)
@Table(name = "stock_dividend", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"ticker_symbol", "record_date"})
})
@Entity
public class StockDividend extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Convert(converter = MoneyConverter.class)
	@Column(precision = 19, nullable = false)
	private Money dividend;

	@Embedded
	private DividendDates dividendDates;

	@Column(name = "is_deleted", nullable = false)
	private boolean isDeleted;
  
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ticker_symbol")
	private Stock stock;

	private StockDividend(Long id, Money dividend, DividendDates dividendDates, Stock stock) {
		this.id = id;
		this.dividend = dividend;
		this.dividendDates = dividendDates;
		this.isDeleted = false;
		this.stock = stock;
	}

	public static StockDividend create(Money dividend, LocalDate recordDate, LocalDate paymentDate, Stock stock) {
		DividendDates dividendDates = DividendDates.withPaymentDate(recordDate, paymentDate);
		return create(null, dividend, dividendDates, stock);
	}

	public static StockDividend create(Long id, Money dividend, LocalDate recordDate, LocalDate paymentDate,
		Stock stock) {
		DividendDates dividendDates = DividendDates.withPaymentDate(recordDate, paymentDate);
		return create(id, dividend, dividendDates, stock);
	}

	public static StockDividend create(Long id, Money dividend, DividendDates dividendDates, Stock stock) {
		return new StockDividend(id, dividend, dividendDates, stock);
	}

	// 주식 개수에 따른 배당금 합계 계산
	// 배당금 합계 = 주당 배당금 * 주식 개수
	public Expression calculateDividendSum(Count numShares) {
		return numShares.multiply(dividend);
	}

	public boolean isDuplicatedRecordDate(List<StockDividend> currentYearStockDividends) {
		return currentYearStockDividends.stream()
			.anyMatch(stockDividend -> stockDividend.getQuarter().equals(getQuarter()));
	}

	public void change(StockDividend stockDividend) {
		this.dividend = stockDividend.getDividend();
		this.dividendDates = stockDividend.getDividendDates();
	}

	/**
	 * 배당 일정 정보들을 파싱하여 반환
	 * format :  tickerSymbol:dividend:recordDate:exDividendDate:paymentDate
	 *   - ex) 005930:361:2022-08-01:2022-08-01:2022-08-01, 005930:361:2022-08-01:2022-08-01:null
	 * @return 배당 일정 정보 요약
	 */
	public String parse() {
		String dividendDateString = dividendDates.parse();
		return String.format("%s:%s:%s", stock.getTickerSymbol(), dividend, dividendDateString);
	}

	public String toCsvLineString() {
		return String.join(",",
			this.id.toString(),
			this.dividend.toString(),
			dividendDates.basicIsoForRecordDate(),
			dividendDates.basicIsoForPaymentDate(),
			this.stock.getStockCode());
	}

	public boolean canReceiveDividendOn(PurchaseHistory history) {
		return history.canReceiveDividendOn(dividendDates);
	}

	public Integer getMonthValueByPaymentDate() {
		return dividendDates.getPaymentDateMonth();
	}

	public boolean isCurrentYearRecordDate(LocalDate localDate) {
		return dividendDates.isCurrentYearRecordDate(localDate);
	}

	public boolean isLastYearPaymentDate(LocalDate lastYearLocalDate) {
		return dividendDates.isLastYearPaymentDate(lastYearLocalDate);
	}

	private Integer getQuarter() {
		return dividendDates.getQuarterWithRecordDate();
	}

	public boolean isCurrentYearPaymentDate(LocalDate today) {
		return dividendDates.isCurrentYearPaymentDate(today);
	}

	public boolean equalRecordDate(LocalDate recordDate) {
		return dividendDates.equalRecordDate(recordDate);
	}

	public boolean hasPaymentDate() {
		return dividendDates.hasPaymentDate();
	}

	public boolean isPaymentInCurrentYear(LocalDate localDate) {
		return dividendDates.isPaymentInCurrentYear(localDate);
	}

	public boolean hasInRangeForRecordDate(LocalDate from, LocalDate to) {
		return dividendDates.hasInRangeForRecordDate(from, to);
	}
  
	public boolean equalPaymentDate(LocalDate paymentDate) {
		return dividendDates.equalPaymentDate(paymentDate);
	}

	public boolean isSatisfiedBy(PurchaseHistory history) {
		return dividendDates.isSatisfiedBy(history);
	}

	public boolean isPurchaseDateBeforeExDividendDate(PurchaseHistory history) {
		return dividendDates.isPurchaseDateBeforeExDividendDate(history);
	}
}
