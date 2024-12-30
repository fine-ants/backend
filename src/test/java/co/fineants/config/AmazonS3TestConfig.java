package co.fineants.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.PutObjectRequest;

import co.fineants.AbstractContainerBaseTest;
import lombok.extern.slf4j.Slf4j;

@TestConfiguration(proxyBeanMethods = false)
@Slf4j
public class AmazonS3TestConfig {

	@Value("${aws.s3.bucket}")
	private String bucketName;
	@Value("${aws.s3.stock-path}")
	private String stockPath;
	@Value("${aws.s3.dividend-csv-path}")
	private String dividendPath;

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

		// stocks.csv 파일 저장
		try {
			amazonS3.putObject(new PutObjectRequest(
				bucketName,
				stockPath,
				new ClassPathResource("stocks.csv").getFile()
			));
		} catch (IOException e) {
			throw new IllegalStateException("not put stocks.csv", e);
		}

		// dividends.csv 파일 저장
		try {
			amazonS3.putObject(new PutObjectRequest(
					bucketName,
					dividendPath,
					new ClassPathResource("dividends.csv").getFile()
				)
			);
		} catch (IOException e) {
			throw new IllegalStateException("not put dividends.csv", e);
		}
	}
}
