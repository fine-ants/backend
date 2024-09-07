package co.fineants.api.domain.holding.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.web.reactive.server.WebTestClient;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.dto.response.KisClosingPrice;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.member.domain.dto.request.LoginRequest;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.price.domain.stock_price.repository.StockPriceRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class PortfolioHoldingRestControllerIntegrationTest extends AbstractContainerBaseTest {

	@LocalServerPort
	private int port;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private PortfolioHoldingRepository holdingRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private PurchaseHistoryRepository purchaseHistoryRepository;

	@Autowired
	private CurrentPriceRedisRepository currentPriceRedisRepository;

	@Autowired
	private ClosingPriceRepository closingPriceRepository;

	@MockBean
	private LocalDateTimeService localDateTimeService;

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private StockPriceRepository stockPriceRepository;

	@BeforeEach
	void setUp() {
		stockPriceRepository.clear();
	}

	@AfterEach
	void tearDown() {
		stockPriceRepository.clear();
	}

	@DisplayName("사용자가 포트폴리오 종목 조회(SSE)를 요청하면 포트폴리오 종목들의 정보를 price 모듈의 큐에 푸시한다")
	@Test
	void observePortfolioHoldings_whenPassingPortfolioId_thenSaveStockPriceRepository() {
		// given
		Stock samsung = stockRepository.save(createSamsungStock());
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		PortfolioHolding holding = holdingRepository.save(createPortfolioHolding(portfolio, samsung));
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, LocalDateTime.now(), Count.from(3), Money.won(50000), "첫구매", holding));

		BDDMockito.given(localDateTimeService.getLocalDateTimeWithNow())
			.willReturn(LocalDateTime.of(2024, 9, 6, 10, 0, 0));
		currentPriceRedisRepository.savePrice(KisCurrentPrice.create(samsung.getTickerSymbol(), 50000L));
		closingPriceRepository.addPrice(KisClosingPrice.create(samsung.getTickerSymbol(), 49000L));

		Map<String, String> loginCookies = login(member);
		Long portfolioId = portfolio.getId();
		String uri = String.format("/api/portfolio/%d/holdings/realtime", portfolioId);
		// when
		Flux<ServerSentEvent> responseBody = webTestClient
			.mutate()
			.responseTimeout(Duration.ofSeconds(35))
			.build()
			.get()
			.uri(uri)
			.cookies(cookies -> {
				cookies.add("accessToken", loginCookies.get("accessToken"));
				cookies.add("refreshToken", loginCookies.get("refreshToken"));
			})
			.accept(MediaType.TEXT_EVENT_STREAM)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
			.returnResult(ServerSentEvent.class)
			.getResponseBody();
		// then
		StepVerifier.create(responseBody)
			.expectSubscription()
			.expectNextCount(1)
			.thenCancel()
			.verify();

		Assertions.assertThat(stockPriceRepository.size()).isEqualTo(1);
	}

	private Map<String, String> login(Member member) {
		return RestAssured
			.with()
			.contentType(ContentType.JSON)
			.port(port)
			.body(LoginRequest.create(member.getEmail(), "nemo1234@"))
			.post("/api/auth/login")
			.cookies();
	}
}
