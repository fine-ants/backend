package codesquad.fineants;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.kis.client.KisAccessToken;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.global.init.S3BucketInitializer;
import codesquad.fineants.global.security.auth.dto.MemberAuthentication;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(value = {AmazonS3Config.class, S3BucketInitializer.class})
@AutoConfigureWebTestClient
@Testcontainers
public class AbstractContainerBaseTest {
	private static final String REDIS_IMAGE = "redis:7-alpine";
	private static final int REDIS_PORT = 6379;

	private static final GenericContainer REDIS_CONTAINER = new GenericContainer(REDIS_IMAGE)
		.withExposedPorts(REDIS_PORT)
		.withReuse(true);

	static final LocalStackContainer LOCAL_STACK_CONTAINER = new LocalStackContainer(
		DockerImageName.parse("localstack/localstack"))
		.withServices(LocalStackContainer.Service.S3)
		.withReuse(true);

	static {
		REDIS_CONTAINER.start();
		LOCAL_STACK_CONTAINER.start();
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

	public KisAccessToken createKisAccessToken() {
		return KisAccessToken.bearerType("accessToken", LocalDateTime.now().plusSeconds(86400), 86400);
	}

	@BeforeEach
	void setUp() {
		MemberAuthentication memberAuthentication = MemberAuthentication.from(createMember());
		Authentication authentication = new UsernamePasswordAuthenticationToken(memberAuthentication, Strings.EMPTY,
			List.of(new SimpleGrantedAuthority("ROLE_USER")));
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	protected Member createMember() {
		return createMember("nemo1234");
	}

	protected Member createMember(String nickname) {
		return createMember(nickname, "dragonbead95@naver.com");
	}

	protected Member createMember(String nickname, String email) {
		return Member.builder()
			.id(1L)
			.email(email)
			.nickname(nickname)
			.provider("local")
			.password("nemo1234@")
			.profileUrl("profileUrl")
			.build();
	}

	protected Portfolio createPortfolio(Member member) {
		return createPortfolio(member, "내꿈은 워렌버핏");
	}

	protected Portfolio createPortfolio(Member member, String name) {
		return createPortfolio(member, name, Money.won(1000000L), Money.won(1500000L), Money.won(900000L));
	}

	protected Portfolio createPortfolio(Member member, String name, Money budget, Money targetGain, Money maximumLoss) {
		return Portfolio.builder()
			.name(name)
			.securitiesFirm("토스")
			.budget(budget)
			.targetGain(targetGain)
			.maximumLoss(maximumLoss)
			.member(member)
			.targetGainIsActive(true)
			.maximumLossIsActive(true)
			.build();
	}
}
