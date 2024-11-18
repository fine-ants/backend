package co.fineants.api.domain.stock.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.stock.domain.entity.Market;
import co.fineants.api.domain.stock.domain.entity.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockCsvReader {

	public static final String CSV_DELIMITER = "$";

	public Set<Stock> readStockCsv() {
		Resource resource = new ClassPathResource("stocks.csv");

		Set<Stock> result = new HashSet<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
			Iterable<CSVRecord> records = CSVFormat.Builder.create()
				.setHeader("stockCode", "tickerSymbol", "companyName", "companyNameEng", "sector", "market")
				.setSkipHeaderRecord(true)
				.setDelimiter(CSV_DELIMITER)
				.build()
				.parse(reader);

			for (CSVRecord csvRecord : records) {
				Stock stock = Stock.of(
					csvRecord.get("tickerSymbol").replace(Stock.TICKER_PREFIX, Strings.EMPTY),
					csvRecord.get("companyName"),
					csvRecord.get("companyNameEng"),
					csvRecord.get("stockCode"),
					csvRecord.get("sector"),
					Market.ofMarket(csvRecord.get("market"))
				);
				result.add(stock);
			}
		} catch (IOException e) {
			return Collections.emptySet();
		}
		return result;
	}

	public List<StockDividend> readDividendCsv(List<Stock> stocks) {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(Objects.requireNonNull(classLoader.getResource("dividends.csv")).getFile());

		List<StockDividend> result = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			Iterable<CSVRecord> records = CSVFormat.Builder.create()
				.setHeader("id", "dividend", "recordDate", "paymentDate", "stockCode")
				.setSkipHeaderRecord(true)
				.build()
				.parse(reader);

			Map<String, Stock> stockMap = stocks.stream()
				.collect(Collectors.toMap(Stock::getStockCode, stock -> stock));
			for (CSVRecord csvRecord : records) {
				Stock stock = stockMap.get(csvRecord.get("stockCode"));
				if (stock == null) {
					continue;
				}
				StockDividend stockDividend = StockDividend.create(
					Long.valueOf(csvRecord.get("id")),
					Money.won(csvRecord.get("dividend")),
					basicIso(csvRecord.get("recordDate")).orElse(null),
					basicIso(csvRecord.get("paymentDate")).orElse(null),
					stock
				);
				result.add(stockDividend);
			}
		} catch (IOException e) {
			log.error(e.getMessage());
			return Collections.emptyList();
		}
		return result;
	}

	@NotNull
	private Optional<LocalDate> basicIso(String localDateString) {
		if (Strings.isBlank(localDateString)) {
			return Optional.empty();
		}
		return Optional.of(LocalDate.parse(localDateString, DateTimeFormatter.BASIC_ISO_DATE));
	}
}
