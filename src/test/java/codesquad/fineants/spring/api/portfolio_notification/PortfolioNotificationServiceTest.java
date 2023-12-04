package codesquad.fineants.spring.api.portfolio_notification;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistoryRepository;
import codesquad.fineants.domain.portfolio_holding.PortFolioHoldingRepository;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.purchase_history.PurchaseHistoryRepository;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.portfolio_notification.request.PortfolioNotificationModifyRequest;
import codesquad.fineants.spring.api.portfolio_notification.response.PortfolioNotificationModifyResponse;

@ActiveProfiles("test")
@SpringBootTest
class PortfolioNotificationServiceTest {

	@Autowired
	private PortfolioNotificationService service;

	@Autowired
	private PortFolioHoldingRepository portFolioHoldingRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PurchaseHistoryRepository purchaseHistoryRepository;

	@Autowired
	private PortfolioGainHistoryRepository portfolioGainHistoryRepository;

	@MockBean
	private CurrentPriceManager currentPriceManager;

	@MockBean
	private MailService mailService;

	private Member member;

	private Portfolio portfolio;

	private Stock stock;

	private PortfolioHolding portfolioHolding;

	private PurchaseHistory purchaseHistory;

	@BeforeEach
	void init() {
		Member member = Member.builder()
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.password("kim1234@")
			.provider("local")
			.build();
		this.member = memberRepository.save(member);

		Portfolio portfolio = Portfolio.builder()
			.name("내꿈은 워렌버핏")
			.securitiesFirm("토스")
			.budget(1000000L)
			.targetGain(1500000L)
			.maximumLoss(900000L)
			.member(this.member)
			.targetGainIsActive(true)
			.maximumIsActive(true)
			.build();
		this.portfolio = portfolioRepository.save(portfolio);

		Stock stock = Stock.builder()
			.companyName("삼성전자보통주")
			.tickerSymbol("005930")
			.companyNameEng("SamsungElectronics")
			.stockCode("KR7005930003")
			.market(Market.KOSPI)
			.build();
		this.stock = stockRepository.save(stock);

		PortfolioHolding portfolioHolding = PortfolioHolding.empty(portfolio, stock);
		this.portfolioHolding = portFolioHoldingRepository.save(portfolioHolding);

		this.purchaseHistory = purchaseHistoryRepository.save(PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(3L)
			.purchasePricePerShare(50000.0)
			.memo("첫구매")
			.portFolioHolding(this.portfolioHolding)
			.build());
	}

	@AfterEach
	void tearDown() {
		portfolioGainHistoryRepository.deleteAllInBatch();
		purchaseHistoryRepository.deleteAllInBatch();
		portFolioHoldingRepository.deleteAllInBatch();
		portfolioRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
		stockRepository.deleteAllInBatch();
	}

	@DisplayName("사용자는 포트폴리오 목표수익금액 알림을 활성화한다")
	@Test
	void modifyPortfolioTargetGainNotification() throws JsonProcessingException {
		// given
		long portfolioId = portfolio.getId();
		Map<String, Boolean> requestBodyMap = new HashMap<>();
		requestBodyMap.put("isActive", true);
		PortfolioNotificationModifyRequest request = objectMapper.readValue(
			objectMapper.writeValueAsString(requestBodyMap), PortfolioNotificationModifyRequest.class);

		// when
		PortfolioNotificationModifyResponse response = service.modifyPortfolioTargetGainNotification(
			request, portfolioId);

		// then
		assertAll(
			() -> assertThat(response).extracting("portfolioId", "isActive")
				.containsExactlyInAnyOrder(portfolioId, true),
			() -> assertThat(portfolioRepository.findById(portfolioId).orElseThrow().getTargetGainIsActive()).isTrue()
		);
	}

	@DisplayName("사용자는 포트폴리오 최대손실금액 알림을 활성화한다")
	@Test
	void modifyPortfolioMaximumLossNotification() throws JsonProcessingException {
		// given
		long portfolioId = portfolio.getId();
		Map<String, Boolean> requestBodyMap = new HashMap<>();
		requestBodyMap.put("isActive", true);
		PortfolioNotificationModifyRequest request = objectMapper.readValue(
			objectMapper.writeValueAsString(requestBodyMap), PortfolioNotificationModifyRequest.class);

		// when
		PortfolioNotificationModifyResponse response = service.modifyPortfolioMaximumLossNotification(
			request, portfolioId);

		// then
		assertAll(
			() -> assertThat(response).extracting("portfolioId", "isActive")
				.containsExactlyInAnyOrder(portfolioId, true),
			() -> assertThat(portfolioRepository.findById(portfolioId).orElseThrow().getMaximumIsActive()).isTrue()
		);
	}

	@DisplayName("사용자에게 최대손익금액 도달 안내 메일을 전송합니다.")
	@Test
	void notifyTargetGain() {
		// given
		purchaseHistoryRepository.save(PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(100L)
			.purchasePricePerShare(10000.0)
			.memo("추가구매")
			.portFolioHolding(portfolioHolding)
			.build());

		given(currentPriceManager.hasCurrentPrice("005930")).willReturn(true);
		given(currentPriceManager.getCurrentPrice("005930")).willReturn(100000L);
		doNothing().when(mailService).sendEmail(anyString(),
			anyString(),
			anyString());
		// when
		service.notifyTargetGain();
		// then
	}

}
