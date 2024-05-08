package codesquad.fineants;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Profile("test")
@Configuration(proxyBeanMethods = false)
public class AmazonS3Config {

	@Bean
	public AmazonS3 amazonS3() {
		return AmazonS3ClientBuilder.standard()
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
	}
}
