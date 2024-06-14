package codesquad.fineants.global.aws.s3.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Configuration
public class S3Config {

	@Value("${aws.s3.endpoint}")
	private String awsEndpoint;
	@Value("${aws.access-key}")
	private String accessKey;
	@Value("${aws.secret-key}")
	private String accessSecret;
	@Value("${aws.region.static}")
	private String region;

	@Profile(value = {"release", "production"})
	@Bean
	public AmazonS3 amazonS3() {
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, accessSecret);
		return AmazonS3ClientBuilder.standard()
			.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(region).build();
	}

	@Profile(value = {"local"})
	@Bean
	public AmazonS3 localAmazonS3() {
		AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(
			awsEndpoint, region);
		BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, accessSecret);

		return AmazonS3ClientBuilder.standard()
			.withEndpointConfiguration(endpoint)
			.withCredentials(new AWSStaticCredentialsProvider(credentials))
			.build();
	}
}
