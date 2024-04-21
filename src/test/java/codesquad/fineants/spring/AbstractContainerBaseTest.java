package codesquad.fineants.spring;

import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import codesquad.fineants.spring.init.S3BucketInitializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(value = {S3BucketInitializer.class})
@AutoConfigureWebTestClient
@Testcontainers
public class AbstractContainerBaseTest {
	private static final String MYSQL_IMAGE = "mysql:8.0.32";
	private static final int MYSQL_PORT = 3306;
	private static final String REDIS_IMAGE = "redis:7-alpine";
	private static final int REDIS_PORT = 6379;

	private static final GenericContainer MYSQL_CONTAINER = new GenericContainer(MYSQL_IMAGE)
		.withExposedPorts(MYSQL_PORT)
		.withReuse(true);

	private static final GenericContainer REDIS_CONTAINER = new GenericContainer(REDIS_IMAGE)
		.withExposedPorts(REDIS_PORT)
		.withReuse(true);

	static {
		MYSQL_CONTAINER.start();
		REDIS_CONTAINER.start();
	}

	@DynamicPropertySource
	public static void overrideProps(DynamicPropertyRegistry registry) {
		// redis property config
		registry.add("spring.redis.host", REDIS_CONTAINER::getHost);
		registry.add("spring.redis.port", () -> REDIS_CONTAINER.getMappedPort(REDIS_PORT).toString());

		// mysql property config
		registry.add("spring.datasource.driver-class-name", () -> "org.testcontainers.jdbc.ContainerDatabaseDriver");
		registry.add("spring.datasource.url", () -> "jdbc:tc:mysql:8.0.33://localhost/fineAnts");
		registry.add("spring.datasource.username", () -> "admin");
		registry.add("spring.datasource.password", () -> "password1234!");
	}
}
