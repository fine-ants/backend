package codesquad.fineants.domain.dividend.domain.parser;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.dividend.domain.entity.StockDividend;
import codesquad.fineants.domain.stock.domain.entity.Stock;

@Component
public class StockDividendParser {

	public StockDividend parseCsvLine(String[] data, Map<String, Stock> stockMap) {
		Long id = Long.parseLong(data[0]);
		Money dividend = Money.won(Long.parseLong(data[1]));
		LocalDate recordDate = basicIso(data[2]);
		LocalDate paymentDate = basicIso(data[3]);
		Stock stock = stockMap.get(data[4]);
		return StockDividend.create(id, dividend, recordDate, paymentDate, stock);
	}

	private LocalDate basicIso(String localDateString) {
		if (Strings.isBlank(localDateString)) {
			return null;
		}
		return LocalDate.parse(localDateString, DateTimeFormatter.BASIC_ISO_DATE);
	}
}
