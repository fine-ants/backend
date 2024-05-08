package codesquad.fineants.global.config.aws.s3;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Profile(value = {"local"})
@Configuration(proxyBeanMethods = false)
public class LocalStackS3Config {
	@Bean(initMethod = "start", destroyMethod = "stop")
	public LocalStackContainer localStackContainer() {
		try (LocalStackContainer container = new LocalStackContainer(DockerImageName.parse("localstack/localstack"))
			.withServices(LocalStackContainer.Service.S3)
			.withReuse(true)) {
			return container;
		}
	}

	@Bean
	public AmazonS3 amazonS3(LocalStackContainer localStackContainer) {
		return AmazonS3ClientBuilder.standard()
			.withEndpointConfiguration(
				new AwsClientBuilder.EndpointConfiguration(
					localStackContainer.getEndpoint().toString(),
					localStackContainer.getRegion()
				)
			)
			.withCredentials(
				new AWSStaticCredentialsProvider(
					new BasicAWSCredentials(
						localStackContainer.getAccessKey(),
						localStackContainer.getSecretKey()
					)
				)
			)
			.build();
	}
}
