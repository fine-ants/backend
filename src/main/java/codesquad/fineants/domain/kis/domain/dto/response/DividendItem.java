package codesquad.fineants.domain.kis.domain.dto.response;

import java.time.LocalDate;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.dividend.domain.entity.StockDividend;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = {"id"})
public class DividendItem {
	private Long id;
	private String tickerSymbol;
	private Money dividend;
	private LocalDate recordDate;
	private LocalDate exDividendDate;
	private LocalDate paymentDate;

	public static DividendItem create(Long id, String tickerSymbol, Money dividend, LocalDate recordDate,
		LocalDate exDividendDate, LocalDate paymentDate) {
		return new DividendItem(id, tickerSymbol, dividend, recordDate, exDividendDate, paymentDate);
	}

	public static DividendItem from(StockDividend stockDividend) {
		return new DividendItem(
			stockDividend.getId(),
			stockDividend.getStock().getTickerSymbol(),
			stockDividend.getDividend(),
			stockDividend.getRecordDate(),
			stockDividend.getExDividendDate(),
			stockDividend.getPaymentDate()
		);
	}
}
