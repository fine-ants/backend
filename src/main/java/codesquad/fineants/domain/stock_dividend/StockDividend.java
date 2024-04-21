package codesquad.fineants.domain.stock_dividend;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import codesquad.fineants.spring.api.S3.dto.Dividend;
import codesquad.fineants.spring.api.kis.manager.HolidayManager;
import codesquad.fineants.spring.api.stock_dividend.HolidayFileReader;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString(exclude = {"stock"})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

	@Column(name = "record_date", nullable = false)
	private LocalDate recordDate;
	@Column(name = "ex_dividend_date", nullable = false)
	private LocalDate exDividendDate;
	@Column(name = "payment_date")
	private LocalDate paymentDate;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ticker_symbol", referencedColumnName = "tickerSymbol")
	private Stock stock;

	private static final ExDividendDateCalculator EX_DIVIDEND_DATE_CALCULATOR = new ExDividendDateCalculator(
		new HolidayManager(new HolidayFileReader()));

	@Builder
	protected StockDividend(LocalDateTime createAt, LocalDateTime modifiedAt, Long id, Money dividend,
		LocalDate recordDate, LocalDate exDividendDate, LocalDate paymentDate, Stock stock) {
		super(createAt, modifiedAt);
		this.id = id;
		this.dividend = dividend;
		this.recordDate = recordDate;
		this.exDividendDate = exDividendDate;
		this.paymentDate = paymentDate;
		this.stock = stock;
	}

	public static StockDividend create(Money dividend, LocalDate recordDate,
		LocalDate paymentDate, Stock stock) {
		LocalDate exDividendDate = EX_DIVIDEND_DATE_CALCULATOR.calculate(recordDate);

		return StockDividend.builder()
			.dividend(dividend)
			.recordDate(recordDate)
			.exDividendDate(exDividendDate)
			.paymentDate(paymentDate)
			.stock(stock)
			.build();
	}

	// 주식 개수에 따른 배당금 합계 계산
	// 배당금 합계 = 주당 배당금 * 주식 개수
	public Money calculateDividendSum(Count numShares) {
		return dividend.multiply(numShares);
	}

	// 배당금을 받을 수 있는지 검사
	public boolean isSatisfied(LocalDate purchaseDate) {
		return purchaseDate.isBefore(exDividendDate) && paymentDate != null;
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
		if (paymentDate == null) {
			return false;
		}
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

	public boolean equalRecordDate(LocalDate recordDate) {
		return this.recordDate.equals(recordDate);
	}

	/**
	 * paymentDate를 가지고 있는지 확인
	 * @return 소유 여부
	 */
	public boolean hasPaymentDate() {
		return paymentDate != null;
	}

	public void change(StockDividend stockDividend) {
		this.dividend = stockDividend.getDividend();
		this.recordDate = stockDividend.getRecordDate();
		this.exDividendDate = stockDividend.getExDividendDate();
		this.paymentDate = stockDividend.getPaymentDate();
	}

	/**
	 * 배당 일정 정보들을 파싱하여 반환
	 * format :  tickerSymbol:dividend:recordDate:exDividendDate:paymentDate
	 *   - ex) 005930:361:2022-08-01:2022-08-01:2022-08-01, 005930:361:2022-08-01:2022-08-01:null
	 * @return
	 */
	public String parse() {
		return String.format("%s:%s:%s:%s:%s", stock.getTickerSymbol(), dividend, recordDate, exDividendDate,
			paymentDate);
	}

	public boolean isSatisfiedPaymentDateEqualYearBy(LocalDate now) {
		if (paymentDate == null) {
			return false;
		}
		return paymentDate.getYear() == now.getYear();
	}

	public boolean hasInRange(LocalDate from, LocalDate to) {
		return recordDate.isAfter(from) && recordDate.isBefore(to);
	}

	public Dividend toDividend() {
		return Dividend.create(recordDate, paymentDate, stock.getTickerSymbol(), stock.getCompanyName(), dividend);
	}
}
