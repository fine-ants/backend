package codesquad.fineants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import codesquad.fineants.domain.fcm.domain.entity.FcmToken;
import codesquad.fineants.domain.holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.kis.client.KisAccessToken;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.domain.entity.MemberRole;
import codesquad.fineants.domain.member.domain.entity.Role;
import codesquad.fineants.domain.member.repository.RoleRepository;
import codesquad.fineants.domain.notificationpreference.domain.entity.NotificationPreference;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.purchasehistory.domain.entity.PurchaseHistory;
import codesquad.fineants.domain.stock.domain.entity.Market;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock_target_price.domain.entity.StockTargetPrice;
import codesquad.fineants.domain.stock_target_price.domain.entity.TargetPriceNotification;
import codesquad.fineants.domain.watchlist.domain.entity.WatchList;
import codesquad.fineants.domain.watchlist.domain.entity.WatchStock;
import codesquad.fineants.global.errors.errorcode.RoleErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;
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

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private DatabaseCleaner databaseCleaner;

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

	@BeforeEach
	public void abstractSetup() {
		createRoleIfNotFound("ROLE_ADMIN", "관리자");
		createRoleIfNotFound("ROLE_MANAGER", "매니저");
		createRoleIfNotFound("ROLE_USER", "회원");
	}

	@AfterEach
	public void cleanDatabase() {
		databaseCleaner.clear();
	}

	public KisAccessToken createKisAccessToken() {
		return KisAccessToken.bearerType("accessToken", LocalDateTime.now().plusSeconds(86400), 86400);
	}

	protected void createRoleIfNotFound(String roleName, String roleDesc) {
		Role role = roleRepository.findRoleByRoleName(roleName)
			.orElseGet(() -> Role.create(roleName, roleDesc));
		roleRepository.save(role);
	}

	protected Member createMember() {
		return createMember("nemo1234");
	}

	protected Member createMember(String nickname) {
		return createMember(nickname, "dragonbead95@naver.com");
	}

	protected Member createMember(String nickname, String email) {
		Role userRole = roleRepository.findRoleByRoleName("ROLE_USER")
			.orElseThrow(() -> new FineAntsException(RoleErrorCode.NOT_EXIST_ROLE));
		// 회원 생성
		Member member = Member.localMember(
			email,
			nickname,
			passwordEncoder.encode("nemo1234@"),
			"profileUrl"
		);
		// 역할 설정
		member.addMemberRole(MemberRole.create(member, userRole));

		// 계정 알림 설정
		member.setNotificationPreference(NotificationPreference.allActive(member));
		return member;
	}

	protected NotificationPreference createNotificationPreference(boolean browserNotify, boolean targetGainNotify,
		boolean maxLossNotify, boolean targetPriceNotify, Member member) {
		return NotificationPreference.create(
			browserNotify,
			targetGainNotify,
			maxLossNotify,
			targetPriceNotify,
			member
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
			"서비스업"
		);
	}

	protected Stock createStock(String tickerSymbol, String companyName, String companyNameEng, String stockCode,
		String sector) {
		return Stock.of(tickerSymbol, companyName, companyNameEng, stockCode, sector, Market.KOSPI);
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

	protected FcmToken createFcmToken(String token, Member member) {
		return FcmToken.create(member, token);
	}

	protected WatchList createWatchList(Member member) {
		return createWatchList("관심 종목1", member);
	}

	protected WatchList createWatchList(String name, Member member) {
		return WatchList.newWatchList(name, member);
	}

	protected WatchStock createWatchStock(WatchList watchList, Stock stock) {
		return WatchStock.newWatchStock(watchList, stock);
	}

	protected StockTargetPrice createStockTargetPrice(Member member, Stock stock) {
		return StockTargetPrice.newStockTargetPriceWithActive(member, stock);
	}

	protected TargetPriceNotification createTargetPriceNotification(StockTargetPrice stockTargetPrice) {
		return TargetPriceNotification.newTargetPriceNotification(Money.won(60000L), stockTargetPrice);
	}

	protected List<TargetPriceNotification> createTargetPriceNotification(StockTargetPrice stockTargetPrice,
		List<Long> targetPrices) {
		return targetPrices.stream()
			.map(targetPrice -> TargetPriceNotification.newTargetPriceNotification(Money.won(targetPrice),
				stockTargetPrice))
			.collect(Collectors.toList());
	}

	protected List<StockDividend> createStockDividendWith(Stock stock) {
		return List.of(
			createStockDividend(
				LocalDate.of(2022, 12, 31), LocalDate.of(2022, 12, 30),
				LocalDate.of(2023, 4, 14),
				stock),
			createStockDividend(
				LocalDate.of(2023, 3, 31), LocalDate.of(2023, 3, 30),
				LocalDate.of(2023, 5, 17),
				stock),
			createStockDividend(
				LocalDate.of(2023, 6, 30), LocalDate.of(2023, 6, 29),
				LocalDate.of(2023, 8, 16),
				stock),
			createStockDividend(
				LocalDate.of(2023, 9, 30), LocalDate.of(2023, 9, 27),
				LocalDate.of(2023, 11, 20),
				stock),
			createStockDividend(
				LocalDate.of(2024, 3, 31), LocalDate.of(2024, 3, 29),
				LocalDate.of(2024, 5, 17),
				stock),
			createStockDividend(
				LocalDate.of(2024, 6, 30), LocalDate.of(2024, 6, 28),
				LocalDate.of(2024, 8, 16),
				stock),
			createStockDividend(
				LocalDate.of(2024, 9, 30), LocalDate.of(2024, 9, 27),
				LocalDate.of(2024, 11, 20),
				stock)
		);
	}
}
