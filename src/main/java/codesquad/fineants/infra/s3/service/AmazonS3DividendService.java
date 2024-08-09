package codesquad.fineants.infra.s3.service;

import static java.nio.charset.StandardCharsets.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;

import codesquad.fineants.domain.dividend.domain.entity.StockDividend;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmazonS3DividendService {
	public static final String CSV_SEPARATOR = ",";
	private final AmazonS3 amazonS3;
	@Value("${aws.s3.bucket}")
	private String bucketName;
	@Value("${aws.s3.dividend-csv-path}")
	private String dividendPath;
	private final StockRepository stockRepository;

	@Transactional(readOnly = true)
	public List<StockDividend> fetchDividends() {
		return getS3Object()
			.map(this::parseStockDividends)
			.orElseGet(Collections::emptyList);
	}

	private Optional<S3Object> getS3Object() {
		try {
			return Optional.ofNullable(amazonS3.getObject(new GetObjectRequest(bucketName, dividendPath)));
		} catch (AmazonServiceException e) {
			log.error(e.getMessage());
			return Optional.empty();
		}
	}

	private List<StockDividend> parseStockDividends(S3Object s3Object) {
		try (BufferedReader br = new BufferedReader(
			new InputStreamReader(s3Object.getObjectContent(), UTF_8))) {
			Map<String, Stock> stockMap = stockRepository.findAll().stream()
				.collect(Collectors.toMap(Stock::getStockCode, stock -> stock));
			return br.lines()
				.skip(1) // skip title
				.map(line -> line.split(CSV_SEPARATOR))
				.map(columns -> StockDividend.parseCsvLine(columns, stockMap))
				.distinct()
				.toList();
		} catch (Exception e) {
			log.error(e.getMessage());
			return Collections.emptyList();
		}
	}

	public void writeDividends(List<StockDividend> dividends) {
		String title = csvTitle();
		String lines = csvLines(dividends);
		String data = String.join(Strings.LINE_SEPARATOR, title, lines);
		PutObjectResult result = putDividendData(data);
		log.debug("writeDividend result : {}", result);
	}

	@NotNull
	private String csvTitle() {
		return String.join(CSV_SEPARATOR, "id", "dividend", "recordDate", "paymentDate", "stockCode");
	}

	private String csvLines(List<StockDividend> dividends) {
		return dividends.stream()
			.map(StockDividend::toCsvLineString)
			.collect(Collectors.joining(Strings.LINE_SEPARATOR));
	}

	private PutObjectResult putDividendData(String data) {
		InputStream inputStream = new ByteArrayInputStream(data.getBytes(UTF_8));
		PutObjectRequest request = new PutObjectRequest(bucketName, dividendPath, inputStream, createObjectMetadata());
		return amazonS3.putObject(request);
	}

	@NotNull
	private ObjectMetadata createObjectMetadata() {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType("text/csv");
		return metadata;
	}
}
