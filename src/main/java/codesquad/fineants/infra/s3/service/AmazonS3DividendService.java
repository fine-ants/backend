package codesquad.fineants.infra.s3.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
		S3Object s3Object = amazonS3.getObject(new GetObjectRequest(bucketName, dividendPath));

		try (BufferedReader br = new BufferedReader(
			new InputStreamReader(s3Object.getObjectContent(), StandardCharsets.UTF_8))) {
			return br.lines()
				.skip(1) // skip title
				.map(line -> line.split(CSV_SEPARATOR))
				.map(Dividend::parse)
				.distinct()
				.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
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
		String title = String.join(CSV_SEPARATOR, "배정기준일", "현금배당지급일", "종목코드", "종목명", "주당배당금");
		String data = dividends.stream()
			.map(Dividend::toCsv)
			.collect(Collectors.joining(Strings.LINE_SEPARATOR));
		String csvData = String.join(Strings.LINE_SEPARATOR, title, data);
		InputStream inputStream = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType("text/csv");
		PutObjectResult putObjectResult = amazonS3.putObject(
			new PutObjectRequest(bucketName, path, inputStream, metadata));
		log.debug("putObjectResult : {}", putObjectResult);
	}
}
