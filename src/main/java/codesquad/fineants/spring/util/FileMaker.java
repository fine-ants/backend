package codesquad.fineants.spring.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import codesquad.fineants.spring.api.errors.errorcode.FileMakerErrorCode;
import codesquad.fineants.spring.api.errors.exception.ServerInternalException;
import codesquad.fineants.spring.api.stock.response.StockDataResponse;

public class FileMaker {
	public static void convertToTsv(List<StockDataResponse.StockInfo> stockInfos) {
		try {
			FileWriter writer = new FileWriter("src/main/resources/newStock.tsv");
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
			throw new ServerInternalException(FileMakerErrorCode.FAIL_FILE_MAKING);
		}
	}
}
