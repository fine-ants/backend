package codesquad.fineants.spring.util;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import codesquad.fineants.spring.api.common.errors.errorcode.FileErrorCode;
import codesquad.fineants.spring.api.common.errors.exception.ServerInternalException;
import codesquad.fineants.spring.api.stock.response.StockDataResponse;

public class StockFileUtil {
	private static final String FILENAME = "stocks.tsv";

	public static void convertToTsvFile(Set<StockDataResponse.StockInfo> stockInfos) {
		try {
			FileWriter writer = new FileWriter("src/main/resources/" + FILENAME);
			// Write the header row
			writer.write("stockCode\ttickerSymbol\tcompanyName\tcompanyNameEng\tmarket\n");

			// Write data rows
			for (StockDataResponse.StockInfo stockInfo : stockInfos) {
				writer.write(
					stockInfo.getStockCode() + "\t" +
						stockInfo.getTickerSymbol() + "\t" +
						stockInfo.getCompanyName() + "\t" +
						stockInfo.getCompanyNameEng() + "\t" +
						stockInfo.getMarket() + "\n"
				);
			}
			writer.close();
		} catch (IOException e) {
			throw new ServerInternalException(FileErrorCode.FAIL_FILE_MAKING);
		}
	}

	public static Set<StockDataResponse.StockInfo> readStockFile() {
		Resource resource = new ClassPathResource(FILENAME);

		try (InputStream inputStream = resource.getInputStream();
			 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

			return reader.lines()
				.skip(1)
				.map(line -> line.split("\t"))
				.map(parts -> StockDataResponse.StockInfo.of(parts[0], parts[1], parts[2], parts[3], parts[4]))
				.collect(Collectors.toSet());
		} catch (IOException e) {
			throw new ServerInternalException(FileErrorCode.FAIL_FILE_READ);
		}
	}
}
