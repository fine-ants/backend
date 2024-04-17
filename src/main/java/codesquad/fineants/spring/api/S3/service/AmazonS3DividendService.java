package codesquad.fineants.spring.api.S3.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
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
				.skip(3) // 표 제목 스킵
				.map(line -> line.split(","))
				.map(columns -> new String[] {columns[0], columns[1], columns[3], columns[4], columns[9]})
				.map(Dividend::from)
				.distinct()
				.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
