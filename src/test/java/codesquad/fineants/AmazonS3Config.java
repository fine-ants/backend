package codesquad.fineants;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.extern.slf4j.Slf4j;

@Profile("test")
@Configuration(proxyBeanMethods = false)
@Slf4j
public class AmazonS3Config {

	@Value("${aws.s3.dividend-bucket}")
	private String bucketName;

	@Bean
	public AmazonS3 amazonS3() {
		AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard()
			.withEndpointConfiguration(
				new AwsClientBuilder.EndpointConfiguration(
					AbstractContainerBaseTest.LOCAL_STACK_CONTAINER.getEndpoint().toString(),
					AbstractContainerBaseTest.LOCAL_STACK_CONTAINER.getRegion()
				)
			)
			.withCredentials(
				new AWSStaticCredentialsProvider(
					new BasicAWSCredentials(
						AbstractContainerBaseTest.LOCAL_STACK_CONTAINER.getAccessKey(),
						AbstractContainerBaseTest.LOCAL_STACK_CONTAINER.getSecretKey()
					)
				)
			)
			.build();
		init(amazonS3);
		return amazonS3;
	}

	public void init(AmazonS3 amazonS3) {
		log.info("creating the {} bucket...", bucketName);
		Bucket bucket = amazonS3.createBucket(bucketName);
		log.info("success the bucket : {}", bucket.toString());

		// dividends.csv 파일 저장
		try {
			amazonS3.putObject(new PutObjectRequest(
					bucketName,
					"dividend/dividends.csv",
					new ClassPathResource("dividends.csv").getFile()
				)
			);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
}
