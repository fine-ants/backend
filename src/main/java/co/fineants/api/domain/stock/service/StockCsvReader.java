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
import java.util.HashMap;
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
import co.fineants.api.domain.stock.domain.dto.response.StockSectorResponse;
import co.fineants.api.domain.stock.domain.entity.Market;
import co.fineants.api.domain.stock.domain.entity.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockCsvReader {

	public Set<Stock> readStockCsv() {
		Resource resource = new ClassPathResource("stocks.csv");

		Set<Stock> result = new HashSet<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
			Iterable<CSVRecord> records = CSVFormat.Builder.create()
				.setHeader("stockCode", "tickerSymbol", "companyName", "companyNameEng", "market", "sector")
				.setSkipHeaderRecord(true)
				.build()
				.parse(reader);

			for (CSVRecord csvRecord : records) {
				Stock stock = Stock.of(
					csvRecord.get("tickerSymbol"),
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

	public Map<String, StockSectorResponse.SectorInfo> readKospiCsv() {
		Resource resource = new ClassPathResource("kospi.csv");

		Map<String, StockSectorResponse.SectorInfo> result = new HashMap<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
			Iterable<CSVRecord> records = CSVFormat.Builder.create()
				.setHeader("종목코드", "종목명", "시장구분", "업종명", "종가", "대비", "등락률", "시가총액")
				.setSkipHeaderRecord(true)
				.build()
				.parse(reader);

			for (CSVRecord csvRecord : records) {
				String tickerSymbol = formatCode(csvRecord.get("종목코드"));
				StockSectorResponse.SectorInfo sectorInfo = StockSectorResponse.SectorInfo.of(
					tickerSymbol,
					csvRecord.get("업종명"),
					csvRecord.get("시장구분")
				);
				result.put(tickerSymbol, sectorInfo);
			}
		} catch (IOException e) {
			return Collections.emptyMap();
		}
		return result;
	}

	/**
	 * 종목 코드를 6자리로 맞추어 반환한다
	 * ex) 104K -> 00104K
	 * @param code stock code
	 * @return formatted stock code
	 */
	private String formatCode(String code) {
		StringBuilder sb = new StringBuilder(code);
		while (sb.length() < 6) {
			sb.insert(0, "0");
		}
		return sb.toString();
	}

	public Map<String, StockSectorResponse.SectorInfo> readKosdaqCsv() {
		Resource resource = new ClassPathResource("kosdaq.csv");

		Map<String, StockSectorResponse.SectorInfo> result = new HashMap<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
			Iterable<CSVRecord> records = CSVFormat.Builder.create()
				.setHeader("종목코드", "종목명", "시장구분", "업종명", "종가", "대비", "등락률", "시가총액")
				.setSkipHeaderRecord(true)
				.build()
				.parse(reader);

			for (CSVRecord csvRecord : records) {
				String tickerSymbol = formatCode(csvRecord.get("종목코드"));
				StockSectorResponse.SectorInfo sectorInfo = StockSectorResponse.SectorInfo.of(
					tickerSymbol,
					csvRecord.get("업종명"),
					csvRecord.get("시장구분")
				);
				result.put(tickerSymbol, sectorInfo);
			}
		} catch (IOException e) {
			return Collections.emptyMap();
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
