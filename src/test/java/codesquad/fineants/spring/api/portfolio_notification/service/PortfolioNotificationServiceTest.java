package codesquad.fineants.spring.api.portfolio_notification.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistoryRepository;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.portfolio_holding.PortfolioHoldingRepository;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.purchase_history.PurchaseHistoryRepository;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.spring.AbstractContainerBaseTest;
import codesquad.fineants.spring.api.common.errors.errorcode.PortfolioErrorCode;
import codesquad.fineants.spring.api.common.errors.exception.FineAntsException;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.mail.service.MailService;
import codesquad.fineants.spring.api.portfolio_notification.manager.MailRedisManager;
import codesquad.fineants.spring.api.portfolio_notification.request.PortfolioNotificationUpdateRequest;
import codesquad.fineants.spring.api.portfolio_notification.response.PortfolioNotificationUpdateResponse;

class PortfolioNotificationServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private PortfolioNotificationService service;

	@Autowired
	private PortfolioHoldingRepository portFolioHoldingRepository;

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

	@MockBean
	private MailRedisManager manager;

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
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding portfolioHolding = portFolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding));

		Map<String, Boolean> requestBodyMap = new HashMap<>();
		requestBodyMap.put("isActive", true);
		PortfolioNotificationUpdateRequest request = objectMapper.readValue(
			objectMapper.writeValueAsString(requestBodyMap), PortfolioNotificationUpdateRequest.class);

		// when
		PortfolioNotificationUpdateResponse response = service.updateNotificationTargetGain(request,
			portfolio.getId());

		// then
		assertAll(
			() -> assertThat(response).extracting("portfolioId", "isActive")
				.containsExactlyInAnyOrder(portfolio.getId(), true),
			() -> assertThat(
				portfolioRepository.findById(portfolio.getId()).orElseThrow().getTargetGainIsActive()).isTrue()
		);
	}

	@DisplayName("사용자는 포트폴리오의 목표수익금액이 0원인 경우 알림을 수정할 수 없다")
	@Test
	void updateNotificationTargetGain_whenTargetGainIsZero_thenNotUpdate() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member, 1000000L, 0L, 0L));

		PortfolioNotificationUpdateRequest request = PortfolioNotificationUpdateRequest.active();
		// when
		Throwable throwable = catchThrowable(() -> service.updateNotificationTargetGain(request, portfolio.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage(PortfolioErrorCode.TARGET_GAIN_IS_ZERO_WITH_NOTIFY_UPDATE.getMessage());
	}

	@DisplayName("사용자는 포트폴리오 최대손실금액 알림을 활성화한다")
	@Test
	void modifyPortfolioMaximumLossNotification() throws JsonProcessingException {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding portfolioHolding = portFolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding));

		long portfolioId = portfolio.getId();
		Map<String, Boolean> requestBodyMap = new HashMap<>();
		requestBodyMap.put("isActive", true);
		PortfolioNotificationUpdateRequest request = objectMapper.readValue(
			objectMapper.writeValueAsString(requestBodyMap), PortfolioNotificationUpdateRequest.class);

		// when
		PortfolioNotificationUpdateResponse response = service.updateNotificationMaximumLoss(
			request, portfolioId);

		// then
		assertAll(
			() -> assertThat(response).extracting("portfolioId", "isActive")
				.containsExactlyInAnyOrder(portfolioId, true),
			() -> assertThat(portfolioRepository.findById(portfolioId).orElseThrow().getMaximumLossIsActive()).isTrue()
		);
	}

	@DisplayName("사용자는 포트폴리오의 최대손실금액이 0원이어서 알림을 수정할 수 없다")
	@Test
	void updateNotificationMaximumLoss() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member, 1000000L, 1500000L, 0L));

		PortfolioNotificationUpdateRequest request = PortfolioNotificationUpdateRequest.active();
		// when
		Throwable throwable = catchThrowable(() -> service.updateNotificationMaximumLoss(request, portfolio.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage(PortfolioErrorCode.MAX_LOSS_IS_ZERO_WITH_NOTIFY_UPDATE.getMessage());
	}

	private Member createMember() {
		return Member.builder()
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.password("kim1234@")
			.provider("local")
			.build();
	}

	private Portfolio createPortfolio(Member member) {
		return createPortfolio(member, 1000000L, 1500000L, 900000L);
	}

	private Portfolio createPortfolio(Member member, Long budget, Long targetGain, Long maximumLoss) {
		return new Portfolio(
			null,
			"내꿈은 워렌버핏",
			"토스증권",
			Money.won(budget),
			Money.won(targetGain),
			Money.won(maximumLoss),
			true,
			true,
			member
		);
	}

	private Stock createStock() {
		return Stock.builder()
			.companyName("삼성전자보통주")
			.tickerSymbol("005930")
			.companyNameEng("SamsungElectronics")
			.stockCode("KR7005930003")
			.market(Market.KOSPI)
			.build();
	}

	private PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock) {
		return PortfolioHolding.empty(portfolio, stock);
	}

	private PurchaseHistory createPurchaseHistory(PortfolioHolding portfolioHolding) {
		return PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(3L))
			.purchasePricePerShare(Money.won(50000.0))
			.memo("첫구매")
			.portfolioHolding(portfolioHolding)
			.build();
	}
}
