package codesquad.fineants.infra.s3.service;

import static java.nio.charset.StandardCharsets.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;

import codesquad.fineants.domain.stock.domain.entity.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AmazonS3StockService {

	public static final String CSV_SEPARATOR = ",";
	// 정규식 패턴: 큰따옴표로 묶인 텍스트 또는 쉼표로 구분된 텍스트를 매칭
	private static final Pattern CSV_SEPARATOR_PATTERN = Pattern.compile(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

	private final AmazonS3 amazonS3;
	@Value("${aws.s3.bucket}")
	private String bucketName;
	@Value("${aws.s3.stock-path}")
	private String stockPath;

	public List<Stock> fetchStocks() {
		return getS3Object()
			.map(this::parseStocks)
			.orElseGet(Collections::emptyList);
	}

	private List<Stock> parseStocks(S3Object s3Object) {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(s3Object.getObjectContent(), UTF_8))) {
			return br.lines()
				.skip(1) // skip title
				.map(CSV_SEPARATOR_PATTERN::split)
				.map(Stock::parse)
				.distinct()
				.toList();
		} catch (Exception e) {
			log.error(e.getMessage());
			return Collections.emptyList();
		}
	}

	private Optional<S3Object> getS3Object() {
		try {
			return Optional.ofNullable(amazonS3.getObject(new GetObjectRequest(bucketName, stockPath)));
		} catch (AmazonServiceException e) {
			log.error(e.getMessage());
			return Optional.empty();
		}
	}

	public void writeStocks(List<Stock> stocks) {
		String title = csvTitle();
		String lines = csvLines(stocks);
		String data = String.join(Strings.LINE_SEPARATOR, title, lines);
		PutObjectResult result = putStockData(data);
		log.info("writeStocks result : {}", result);
	}

	@NotNull
	private String csvTitle() {
		return String.join(CSV_SEPARATOR, "stockCode", "tickerSymbol", "companyName", "companyNameEng",
			"market", "sector");
	}

	@NotNull
	private String csvLines(List<Stock> stocks) {
		return stocks.stream()
			.map(Stock::toCsvLineString)
			.collect(Collectors.joining(Strings.LINE_SEPARATOR));
	}

	private PutObjectResult putStockData(String data) {
		InputStream inputStream = new ByteArrayInputStream(data.getBytes(UTF_8));
		PutObjectRequest request = new PutObjectRequest(bucketName, stockPath, inputStream, createObjectMetadata());
		return amazonS3.putObject(request);
	}

	@NotNull
	private ObjectMetadata createObjectMetadata() {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType("text/csv");
		return metadata;
	}

}
