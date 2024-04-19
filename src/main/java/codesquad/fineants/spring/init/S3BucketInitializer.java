package codesquad.fineants.spring.init;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.RequiredArgsConstructor;

@Profile({"local", "test"})
@Component
@RequiredArgsConstructor
public class S3BucketInitializer {
	private final AmazonS3 amazonS3;

	@PostConstruct
	public void init() {
		String bucketName = "fineants-secret";
		amazonS3.createBucket(bucketName);
		// dividends.csv 파일 저장
		try {
			amazonS3.putObject(new PutObjectRequest(
					bucketName,
					"dividend/dividends.csv",
					new ClassPathResource("dividends.csv").getFile()
				)
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
