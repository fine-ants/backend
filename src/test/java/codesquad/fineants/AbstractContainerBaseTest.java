package codesquad.fineants;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.dividend.domain.entity.StockDividend;
import codesquad.fineants.domain.holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.kis.client.KisAccessToken;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.purchasehistory.domain.entity.PurchaseHistory;
import codesquad.fineants.domain.stock.domain.entity.Market;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.global.init.S3BucketInitializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(value = {AmazonS3Config.class, S3BucketInitializer.class})
@AutoConfigureWebTestClient
@Testcontainers
@WithMockUser(username = "dragonbead95@naver.com", roles = {"USER"})
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

	@Autowired
	private PasswordEncoder passwordEncoder;

	@DynamicPropertySource
	public static void overrideProps(DynamicPropertyRegistry registry) {
		// redis property config
		registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
		registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(REDIS_PORT).toString());

		// mysql property config
		registry.add("spring.datasource.driver-class-name", () -> "org.testcontainers.jdbc.ContainerDatabaseDriver");
		registry.add("spring.datasource.url", () -> "jdbc:tc:mysql:8.0.33://localhost/fineAnts");
		registry.add("spring.datasource.username", () -> "admin");
		registry.add("spring.datasource.password", () -> "password1234!");
	}

	public KisAccessToken createKisAccessToken() {
		return KisAccessToken.bearerType("accessToken", LocalDateTime.now().plusSeconds(86400), 86400);
	}

	protected Member createMember() {
		return createMember("nemo1234");
	}

	protected Member createMember(String nickname) {
		return createMember(nickname, "dragonbead95@naver.com");
	}

	protected Member createMember(String nickname, String email) {
		return Member.localMember(
			1L,
			email,
			nickname,
			passwordEncoder.encode("nemo1234@"),
			"profileUrl"
		);
	}

	protected Portfolio createPortfolio(Member member) {
		return createPortfolio(
			member,
			Money.won(1000000)
		);
	}

	protected Portfolio createPortfolio(Member member, String name) {
		return createPortfolio(
			member,
			name,
			Money.won(1000000L),
			Money.won(1500000L),
			Money.won(900000L)
		);
	}

	protected Portfolio createPortfolio(Member member, Money budget) {
		return createPortfolio(
			member,
			"내꿈은 워렌버핏",
			budget,
			Money.won(1500000L),
			Money.won(900000L)
		);
	}

	protected Portfolio createPortfolio(Member member, String name, Money budget, Money targetGain, Money maximumLoss) {
		return Portfolio.active(
			name,
			"토스증권",
			budget,
			targetGain,
			maximumLoss,
			member
		);
	}

	protected Stock createSamsungStock() {
		return Stock.of("005930", "삼성전자보통주", "SamsungElectronics", "KR7005930003", "전기전자", Market.KOSPI);
	}

	protected Stock createDongwhaPharmStock() {
		return Stock.of("000020", "동화약품보통주", "DongwhaPharm", "KR7000020008", "의약품", Market.KOSPI);
	}

	protected Stock createCcsStack() {
		return Stock.of("066790", "씨씨에스충북방송", "KOREA CABLE T.V CHUNG-BUK SYSTEM CO.,LTD.", "KR7066790007", "방송서비스",
			Market.KOSDAQ);
	}

	protected Stock createKakaoStock() {
		return createStock(
			"035720",
			"카카오보통주",
			"Kakao",
			"KR7035720002",
			"서비스업",
			Market.KOSPI
		);
	}

	protected Stock createStock(String tickerSymbol, String companyName, String companyNameEng, String stockCode,
		String sector, Market market) {
		return Stock.of(tickerSymbol, companyName, companyNameEng, stockCode, sector, market);
	}

	protected PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock) {
		return PortfolioHolding.of(portfolio, stock);
	}

	protected PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock, Long currentPrice) {
		return PortfolioHolding.of(portfolio, stock, Money.won(currentPrice));
	}

	protected StockDividend createStockDividend(LocalDate recordDate, LocalDate exDividendDate, LocalDate paymentDate,
		Stock stock) {
		return StockDividend.create(Money.won(361), recordDate, exDividendDate, paymentDate, stock);
	}

	protected StockDividend createStockDividend(Money dividend, LocalDate recordDate, LocalDate exDividendDate,
		LocalDate paymentDate, Stock stock) {
		return StockDividend.create(dividend, recordDate, exDividendDate, paymentDate, stock);
	}

	protected PurchaseHistory createPurchaseHistory(Long id, LocalDateTime purchaseDate, Count numShares,
		Money purchasePricePerShare, String memo, PortfolioHolding portfolioHolding) {
		return PurchaseHistory.create(id, purchaseDate, numShares, purchasePricePerShare, memo, portfolioHolding);
	}
}
