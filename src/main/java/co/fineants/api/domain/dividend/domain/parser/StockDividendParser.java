package co.fineants.api.domain.dividend.domain.parser;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.dividend.domain.calculator.ExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.stock.domain.entity.Stock;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StockDividendParser {

	private final ExDividendDateCalculator calculator;

	public StockDividend parseCsvLine(String[] data, Map<String, Stock> stockMap) {
		Long id = Long.parseLong(data[0]);
		Money dividend = Money.won(Long.parseLong(data[1]));
		LocalDate recordDate = basicIso(data[2]);
		LocalDate exDividendDate = calculator.calculate(recordDate);
		LocalDate paymentDate = basicIso(data[3]);
		Stock stock = stockMap.get(data[4]);
		return StockDividend.create(id, dividend, recordDate, exDividendDate, paymentDate, stock);
	}

	private LocalDate basicIso(String localDateString) {
		if (Strings.isBlank(localDateString)) {
			return null;
		}
		return LocalDate.parse(localDateString, DateTimeFormatter.BASIC_ISO_DATE);
	}
}
