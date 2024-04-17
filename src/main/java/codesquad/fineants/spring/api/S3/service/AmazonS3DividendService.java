package codesquad.fineants.spring.api.S3.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;

import codesquad.fineants.spring.api.S3.dto.Dividend;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmazonS3DividendService {
	private final AmazonS3 amazonS3;
	@Value("${aws.s3.dividend-bucket}")
	private String bucketName;

	public List<Dividend> fetchDividend() {
		String path = "dividend/dividends.csv";
		S3Object s3Object = amazonS3.getObject(new GetObjectRequest(bucketName, path));

		try (BufferedReader br = new BufferedReader(
			new InputStreamReader(s3Object.getObjectContent(), StandardCharsets.UTF_8))) {
			return br.lines()
				.skip(1) // 표 제목 스킵
				.map(line -> line.split(","))
				.map(Dividend::parse)
				.distinct()
				.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * S3 dividends.csv 파일에 현재 배당일정을 작성한다
	 * @param dividends 배당일정
	 */
	public void writeDividend(List<Dividend> dividends) {
		String path = "dividend/dividends.csv";

		String title = String.join(",", "배정기준일", "현금배당지급일", "종목코드", "종목명", "주당배당금");
		String data = dividends.stream()
			.map(Dividend::toString)
			.collect(Collectors.joining("\n"));
		String csvData = String.join("\n", title, data);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType("text/csv");
		metadata.setContentLength(csvData.length());

		PutObjectResult putObjectResult = amazonS3.putObject(
			new PutObjectRequest(bucketName, path, inputStream, metadata));
		log.debug("putObjectResult : {}", putObjectResult);
	}
}
