package codesquad.fineants.spring;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
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
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureWebTestClient
public class AbstractContainerBaseTest {
	private static final String REDIS_IMAGE = "redis:7-alpine";

	private static final GenericContainer REDIS_CONTAINER;

	static {
		REDIS_CONTAINER = new GenericContainer(REDIS_IMAGE)
			.withExposedPorts(6379)
			.withReuse(true);
		REDIS_CONTAINER.start();
	}

	@DynamicPropertySource
	public static void overrideProps(DynamicPropertyRegistry registry) {
		// redis 프로퍼티 설정
		registry.add("spring.redis.host", REDIS_CONTAINER::getHost);
		registry.add("spring.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379).toString());
	}
}
