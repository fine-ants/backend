package codesquad.fineants.domain.stock.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import codesquad.fineants.domain.stock.domain.dto.response.StockDataResponse;
import codesquad.fineants.domain.stock.domain.dto.response.StockSectorResponse;

@Component
public class StockCsvReader {

	public Set<StockDataResponse.StockInfo> readStockCsv() {
		Resource resource = new ClassPathResource("stocks.csv");

		Set<StockDataResponse.StockInfo> result = new HashSet<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
			Iterable<CSVRecord> records = CSVFormat.DEFAULT
				.withHeader("stockCode", "tickerSymbol", "companyName", "companyNameEng", "market", "sector")
				.withSkipHeaderRecord()
				.parse(reader);

			for (CSVRecord record : records) {
				StockDataResponse.StockInfo stockInfo = StockDataResponse.StockInfo.of(
					record.get("stockCode"),
					record.get("tickerSymbol"),
					record.get("companyName"),
					record.get("companyNameEng"),
					record.get("market")
				);
				result.add(stockInfo);
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
			Iterable<CSVRecord> records = CSVFormat.DEFAULT
				.withHeader("종목코드", "종목명", "시장구분", "업종명", "종가", "대비", "등락률", "시가총액")
				.withSkipHeaderRecord()
				.parse(reader);

			for (CSVRecord record : records) {
				String tickerSymbol = formatCode(record.get("종목코드"));
				StockSectorResponse.SectorInfo sectorInfo = StockSectorResponse.SectorInfo.of(
					tickerSymbol,
					record.get("업종명"),
					record.get("시장구분")
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
			Iterable<CSVRecord> records = CSVFormat.DEFAULT
				.withHeader("종목코드", "종목명", "시장구분", "업종명", "종가", "대비", "등락률", "시가총액")
				.withSkipHeaderRecord()
				.parse(reader);

			for (CSVRecord record : records) {
				String tickerSymbol = formatCode(record.get("종목코드"));
				StockSectorResponse.SectorInfo sectorInfo = StockSectorResponse.SectorInfo.of(
					tickerSymbol,
					record.get("업종명"),
					record.get("시장구분")
				);
				result.put(tickerSymbol, sectorInfo);
			}
		} catch (IOException e) {
			return Collections.emptyMap();
		}
		return result;
	}
}
