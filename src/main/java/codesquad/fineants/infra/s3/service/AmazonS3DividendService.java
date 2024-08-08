package codesquad.fineants.infra.s3.service;

import static java.nio.charset.StandardCharsets.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

import codesquad.fineants.domain.dividend.domain.entity.StockDividend;
import codesquad.fineants.infra.s3.dto.Dividend;
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

	public List<Dividend> fetchDividend() {
		log.debug("bucketName : {}", bucketName);
		log.debug("dividendPath : {}", dividendPath);
		return getS3Object()
			.map(this::parseDividends)
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

	private List<Dividend> parseDividends(S3Object s3Object) {
		try (BufferedReader br = new BufferedReader(
			new InputStreamReader(s3Object.getObjectContent(), UTF_8))) {
			return br.lines()
				.skip(1) // skip title
				.map(line -> line.split(CSV_SEPARATOR))
				.map(Dividend::parse)
				.distinct()
				.toList();
		} catch (Exception e) {
			log.error(e.getMessage());
			return Collections.emptyList();
		}
	}

	public List<StockDividend> fetchDividends() {
		return Collections.emptyList();
	}

	public void writeDividend(List<Dividend> dividends) {
		writeDividend(dividends, dividendPath);
	}

	/**
	 * S3 dividends.csv 파일에 현재 배당일정을 작성한다
	 * @param dividends 배당일정
	 */
	public void writeDividend(List<Dividend> dividends, String path) {
		String title = csvTitle();
		String lines = csvLines(dividends);
		String data = String.join(Strings.LINE_SEPARATOR, title, lines);
		PutObjectResult result = putDividendData(data, path);
		log.debug("writeDividend result : {}", result);
	}

	private PutObjectResult putDividendData(String data, String path) {
		InputStream inputStream = new ByteArrayInputStream(data.getBytes(UTF_8));
		PutObjectRequest request = new PutObjectRequest(bucketName, path, inputStream, createObjectMetadata());
		return amazonS3.putObject(request);
	}

	@NotNull
	private static ObjectMetadata createObjectMetadata() {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType("text/csv");
		return metadata;
	}

	@NotNull
	private static String csvLines(List<Dividend> dividends) {
		return dividends.stream()
			.map(Dividend::toCsvLineString)
			.collect(Collectors.joining(Strings.LINE_SEPARATOR));
	}

	@NotNull
	private static String csvTitle() {
		return String.join(CSV_SEPARATOR, "recordDate", "paymentDate", "stockCode", "companyName", "amount");
	}

	public void writeDividends(List<StockDividend> dividends) {

	}
}
