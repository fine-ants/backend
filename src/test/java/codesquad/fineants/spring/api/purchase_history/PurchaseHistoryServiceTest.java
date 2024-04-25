package codesquad.fineants.spring.api.purchase_history;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.google.firebase.messaging.Message;

import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.fcm_token.FcmRepository;
import codesquad.fineants.domain.fcm_token.FcmToken;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.notification.NotificationRepository;
import codesquad.fineants.domain.notification_preference.NotificationPreference;
import codesquad.fineants.domain.notification_preference.NotificationPreferenceRepository;
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
import codesquad.fineants.domain.stock_dividend.StockDividend;
import codesquad.fineants.domain.stock_dividend.StockDividendRepository;
import codesquad.fineants.spring.AbstractContainerBaseTest;
import codesquad.fineants.spring.api.common.errors.errorcode.PortfolioErrorCode;
import codesquad.fineants.spring.api.common.errors.errorcode.PurchaseHistoryErrorCode;
import codesquad.fineants.spring.api.common.errors.exception.FineAntsException;
import codesquad.fineants.spring.api.firebase.service.FirebaseMessagingService;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.notification.manager.NotificationSentManager;
import codesquad.fineants.spring.api.purchase_history.request.PurchaseHistoryCreateRequest;
import codesquad.fineants.spring.api.purchase_history.request.PurchaseHistoryUpdateRequest;
import codesquad.fineants.spring.api.purchase_history.response.PurchaseHistoryCreateResponse;
import codesquad.fineants.spring.api.purchase_history.response.PurchaseHistoryDeleteResponse;
import codesquad.fineants.spring.api.purchase_history.response.PurchaseHistoryUpdateResponse;
import codesquad.fineants.spring.api.purchase_history.service.PurchaseHistoryService;

class PurchaseHistoryServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private PortfolioHoldingRepository portFolioHoldingRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private PurchaseHistoryRepository purchaseHistoryRepository;

	@Autowired
	private PortfolioGainHistoryRepository portfolioGainHistoryRepository;

	@Autowired
	private StockDividendRepository stockDividendRepository;

	@Autowired
	private PurchaseHistoryService service;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private NotificationPreferenceRepository notificationPreferenceRepository;

	@Autowired
	private FcmRepository fcmRepository;

	@MockBean
	private CurrentPriceManager currentPriceManager;

	@MockBean
	private FirebaseMessagingService firebaseMessagingService;

	@MockBean
	private NotificationSentManager sentManager;

	@AfterEach
	void tearDown() {
		fcmRepository.deleteAllInBatch();
		notificationPreferenceRepository.deleteAllInBatch();
		notificationRepository.deleteAllInBatch();
		purchaseHistoryRepository.deleteAllInBatch();
		portFolioHoldingRepository.deleteAllInBatch();
		portfolioGainHistoryRepository.deleteAllInBatch();
		portfolioRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
		stockDividendRepository.deleteAllInBatch();
		stockRepository.deleteAllInBatch();
	}

	@DisplayName("사용자는 매입 이력을 추가한다")
	@CsvSource(value = {"3,1000000", "1000000000000,50000000000000000", "10,9223372036854775807"})
	@ParameterizedTest
	void addPurchaseHistory(Count numShares, BigDecimal budget) {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(createNotificationPreference(member));
		Portfolio portfolio = portfolioRepository.save(
			createPortfolio(member, budget));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding holding = portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));

		LocalDateTime now = LocalDateTime.now();
		Money money = Money.won(50000.0);
		PurchaseHistoryCreateRequest request = PurchaseHistoryCreateRequest.builder()
			.purchaseDate(now)
			.numShares(numShares)
			.purchasePricePerShare(money)
			.memo("첫구매")
			.build();
		given(currentPriceManager.getCurrentPrice(anyString()))
			.willReturn(Optional.of(Money.won(50000L)));
		// when
		PurchaseHistoryCreateResponse response = service.createPurchaseHistory(
			request,
			portfolio.getId(),
			holding.getId(),
			member.getId()
		);

		// then
		PurchaseHistory findPurchaseHistory = purchaseHistoryRepository.findById(response.getId()).orElseThrow();
		assertAll(
			() -> assertThat(response).extracting("id").isNotNull(),
			() -> assertThat(findPurchaseHistory)
				.extracting(PurchaseHistory::getId, PurchaseHistory::getPurchaseLocalDate,
					PurchaseHistory::getPurchasePricePerShare, PurchaseHistory::getNumShares, PurchaseHistory::getMemo)
				.usingComparatorForType(Money::compareTo, Money.class)
				.usingComparatorForType(Count::compareTo, Count.class)
				.containsExactlyInAnyOrder(response.getId(), now.toLocalDate(), Money.won(50000.0), numShares, "첫구매")
		);
	}

	@DisplayName("사용자는 매입 이력 추가시 목표 수익률을 달성하여 알림을 받는다")
	@Test
	void addPurchaseHistory_whenAchieveTargetGain_thenSaveNotification() {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(createNotificationPreference(member));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		Stock stock2 = stockRepository.save(createStock2());
		PortfolioHolding holding = portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));
		portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock2));
		purchaseHistoryRepository.save(createPurchaseHistory(holding));
		fcmRepository.save(createFcmToken("token", member));
		fcmRepository.save(createFcmToken("token2", member));

		PurchaseHistoryCreateRequest request = PurchaseHistoryCreateRequest.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(100L))
			.purchasePricePerShare(Money.won(100.0))
			.memo("첫구매")
			.build();

		given(currentPriceManager.getCurrentPrice(anyString()))
			.willReturn(Optional.of(Money.won(50000L)));
		given(sentManager.hasTargetGainSendHistory(anyLong()))
			.willReturn(false);
		given(firebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));

		// when
		PurchaseHistoryCreateResponse response = service.createPurchaseHistory(
			request,
			portfolio.getId(),
			holding.getId(),
			member.getId()
		);

		// then
		assertAll(
			() -> assertThat(response.getId()).isNotNull(),
			() -> assertThat(notificationRepository.findAllByMemberId(member.getId())).hasSize(1)
		);
	}

	private static FcmToken createFcmToken(String token, Member member) {
		return FcmToken.builder()
			.token(token)
			.latestActivationTime(LocalDateTime.now())
			.member(member)
			.build();
	}

	@DisplayName("사용자는 매입 이력 추가시 최대 손실율에 달성하여 알림을 받는다")
	@Test
	void addPurchaseHistory_whenAchieveMaxLoss_thenSaveNotification() {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(createNotificationPreference(member));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding holding = portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));
		fcmRepository.save(createFcmToken("token", member));

		PurchaseHistoryCreateRequest request = PurchaseHistoryCreateRequest.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(10L))
			.purchasePricePerShare(Money.won(90000.0))
			.memo("첫구매")
			.build();

		given(currentPriceManager.getCurrentPrice(anyString()))
			.willReturn(Optional.of(Money.won(50000L)));
		given(sentManager.hasTargetGainSendHistory(anyLong()))
			.willReturn(false);
		given(firebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));

		// when
		PurchaseHistoryCreateResponse response = service.createPurchaseHistory(
			request,
			portfolio.getId(),
			holding.getId(),
			member.getId()
		);

		// then
		assertAll(
			() -> assertThat(response.getId()).isNotNull(),
			() -> assertThat(notificationRepository.findAllByMemberId(member.getId())).hasSize(1)
		);
	}

	@DisplayName("사용자가 매입 이력을 추가할 때 예산이 부족해 실패한다.")
	@Test
	void addPurchaseHistoryFailsWhenTotalInvestmentExceedsBudget() {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(createNotificationPreference(member));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding holding = portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));

		PurchaseHistoryCreateRequest request = PurchaseHistoryCreateRequest.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(3L))
			.purchasePricePerShare(Money.won(500000.0))
			.memo("첫구매")
			.build();

		// when
		Throwable throwable = catchThrowable(() ->
			service.createPurchaseHistory(request, portfolio.getId(), holding.getId(), member.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage(PortfolioErrorCode.TOTAL_INVESTMENT_PRICE_EXCEEDS_BUDGET.getMessage());
	}

	@DisplayName("사용자는 매입 이력을 수정한다")
	@Test
	void modifyPurchaseHistory() {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(createNotificationPreference(member));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding holding = portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));
		PurchaseHistory history = purchaseHistoryRepository.save(createPurchaseHistory(holding));

		PurchaseHistoryUpdateRequest request = PurchaseHistoryUpdateRequest.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(4L))
			.purchasePricePerShare(Money.won(50000.0))
			.memo("첫구매")
			.build();

		given(currentPriceManager.getCurrentPrice(anyString()))
			.willReturn(Optional.of(Money.won(50000L)));
		// when
		PurchaseHistoryUpdateResponse response = service.updatePurchaseHistory(
			request,
			holding.getId(),
			history.getId(),
			portfolio.getId(),
			member.getId()
		);

		// then
		PurchaseHistory changePurchaseHistory = purchaseHistoryRepository.findById(history.getId()).orElseThrow();
		assertAll(
			() -> assertThat(response).extracting("id").isNotNull(),
			() -> assertThat(response.getNumShares()).isEqualByComparingTo(Count.from(4)),
			() -> assertThat(changePurchaseHistory.getNumShares()).isEqualByComparingTo(Count.from(4L))
		);
	}

	@DisplayName("사용자는 매입 이력을 수정시 목표 수익율을 달성하여 알림을 받는다")
	@Test
	void modifyPurchaseHistory_whenTargetGain_thenSaveNotification() {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(createNotificationPreference(member));
		fcmRepository.save(createFcmToken("token", member));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding holding = portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));
		PurchaseHistory history = purchaseHistoryRepository.save(createPurchaseHistory(holding));

		PurchaseHistoryUpdateRequest request = PurchaseHistoryUpdateRequest.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(100L))
			.purchasePricePerShare(Money.won(100.0))
			.memo("첫구매")
			.build();

		given(currentPriceManager.getCurrentPrice(anyString()))
			.willReturn(Optional.of(Money.won(50000L)));
		given(sentManager.hasTargetGainSendHistory(anyLong()))
			.willReturn(false);
		given(firebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));
		// when
		PurchaseHistoryUpdateResponse response = service.updatePurchaseHistory(
			request,
			holding.getId(),
			history.getId(),
			portfolio.getId(),
			member.getId()
		);

		// then
		PurchaseHistory changePurchaseHistory = purchaseHistoryRepository.findById(history.getId()).orElseThrow();
		assertAll(
			() -> assertThat(response).extracting("id").isNotNull(),
			() -> assertThat(response.getNumShares()).isEqualByComparingTo(Count.from(100)),
			() -> assertThat(changePurchaseHistory.getNumShares()).isEqualByComparingTo(Count.from(100L)),
			() -> assertThat(notificationRepository.findAllByMemberId(member.getId())).hasSize(1)
		);
	}

	@DisplayName("사용자는 매입 이력을 삭제한다")
	@Test
	void deletePurchaseHistory() {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(createNotificationPreference(member));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding holding = portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));
		PurchaseHistory history = purchaseHistoryRepository.save(createPurchaseHistory(holding));

		// when
		PurchaseHistoryDeleteResponse response = service.deletePurchaseHistory(
			holding.getId(),
			history.getId(),
			portfolio.getId(),
			member.getId()
		);

		// then
		assertAll(
			() -> assertThat(response).extracting("id").isNotNull(),
			() -> assertThat(purchaseHistoryRepository.findById(history.getId())).isEmpty()
		);
	}

	@DisplayName("사용자는 매입 이력 삭제시 목표 수익율을 달성하여 알림을 받는다")
	@Test
	void deletePurchaseHistory_whenTargetGain_thenSaveNotification() {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(createNotificationPreference(member));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding holding = portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));
		PurchaseHistory history = purchaseHistoryRepository.save(createPurchaseHistory(holding));
		purchaseHistoryRepository.save(createPurchaseHistory(holding, 100L, 100.0));
		fcmRepository.save(createFcmToken("token", member));

		given(currentPriceManager.getCurrentPrice(anyString()))
			.willReturn(Optional.of(Money.won(50000L)));
		given(sentManager.hasTargetGainSendHistory(anyLong()))
			.willReturn(false);
		given(firebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));

		// when
		PurchaseHistoryDeleteResponse response = service.deletePurchaseHistory(
			holding.getId(),
			history.getId(),
			portfolio.getId(),
			member.getId()
		);

		// then
		assertAll(
			() -> assertThat(response).extracting("id").isNotNull(),
			() -> assertThat(purchaseHistoryRepository.findById(history.getId())).isEmpty(),
			() -> assertThat(notificationRepository.findAllByMemberId(member.getId())).hasSize(1)
		);
	}

	@DisplayName("사용자는 존재하지 않은 매입 이력 등록번호를 가지고 매입 이력을 삭제할 수 없다")
	@Test
	void deletePurchaseHistoryWithNotExistPurchaseHistoryId() {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(createNotificationPreference(member));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding holding = portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));
		PurchaseHistory history = purchaseHistoryRepository.save(createPurchaseHistory(holding));

		Long purchaseHistoryId = 9999L;

		// when
		Throwable throwable = catchThrowable(
			() -> service.deletePurchaseHistory(
				holding.getId(),
				purchaseHistoryId,
				portfolio.getId(),
				member.getId())
		);

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage(PurchaseHistoryErrorCode.NOT_FOUND_PURCHASE_HISTORY.getMessage());
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
		return Portfolio.builder()
			.name("내꿈은 워렌버핏")
			.securitiesFirm("토스")
			.budget(Money.won(1000000L))
			.targetGain(Money.won(1500000L))
			.maximumLoss(Money.won(900000L))
			.member(member)
			.targetGainIsActive(true)
			.maximumLossIsActive(true)
			.build();
	}

	private Portfolio createPortfolio(Member member, BigDecimal budget) {
		return Portfolio.builder()
			.name("내꿈은 워렌버핏")
			.securitiesFirm("토스")
			.budget(Money.won(budget))
			.targetGain(Money.won(budget).add(Money.won(100000L)))
			.maximumLoss(Money.won(900000L))
			.member(member)
			.targetGainIsActive(true)
			.maximumLossIsActive(true)
			.build();
	}

	private Stock createStock() {
		return Stock.builder()
			.companyName("삼성전자보통주")
			.tickerSymbol("005930")
			.companyNameEng("SamsungElectronics")
			.stockCode("KR7005930003")
			.sector("전기전자")
			.market(Market.KOSPI)
			.build();
	}

	private Stock createStock2() {
		return Stock.builder()
			.companyName("동화약품보통주")
			.tickerSymbol("000020")
			.companyNameEng("DongwhaPharm")
			.stockCode("KR7000020008")
			.sector("의약품")
			.market(Market.KOSPI)
			.build();
	}

	private Stock createStock(String companyName, String tickerSymbol, String companyNameEng, String stockCode,
		String sector, Market market) {
		return Stock.builder()
			.companyName(companyName)
			.tickerSymbol(tickerSymbol)
			.companyNameEng(companyNameEng)
			.stockCode(stockCode)
			.sector(sector)
			.market(market)
			.build();
	}

	private StockDividend createStockDividend(LocalDate exDividendDate, LocalDate recordDate, LocalDate paymentDate,
		Stock stock) {
		return StockDividend.builder()
			.dividend(Money.won(361L))
			.exDividendDate(exDividendDate)
			.recordDate(recordDate)
			.paymentDate(paymentDate)
			.stock(stock)
			.build();
	}

	private PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock) {
		return PortfolioHolding.builder()
			.portfolio(portfolio)
			.stock(stock)
			.build();
	}

	private PurchaseHistory createPurchaseHistory(PortfolioHolding portfolioHolding) {
		return PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.of(2023, 9, 26, 9, 30, 0))
			.numShares(Count.from(3L))
			.purchasePricePerShare(Money.won(50000.0))
			.memo("첫구매")
			.portfolioHolding(portfolioHolding)
			.build();
	}

	private PurchaseHistory createPurchaseHistory(PortfolioHolding portfolioHolding, Long numShares,
		Double purchasePricePerShare) {
		return PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.of(2023, 9, 26, 9, 30, 0))
			.numShares(Count.from(numShares))
			.purchasePricePerShare(Money.won(purchasePricePerShare))
			.memo("첫구매")
			.portfolioHolding(portfolioHolding)
			.build();
	}

	private List<StockDividend> createStockDividendWith(Stock stock) {
		return List.of(
			createStockDividend(
				LocalDate.of(2022, 12, 30),
				LocalDate.of(2022, 12, 31),
				LocalDate.of(2023, 4, 14),
				stock),
			createStockDividend(
				LocalDate.of(2023, 3, 30),
				LocalDate.of(2023, 3, 31),
				LocalDate.of(2023, 5, 17),
				stock),
			createStockDividend(
				LocalDate.of(2023, 6, 29),
				LocalDate.of(2023, 6, 30),
				LocalDate.of(2023, 8, 16),
				stock),
			createStockDividend(
				LocalDate.of(2023, 9, 27),
				LocalDate.of(2023, 9, 30),
				LocalDate.of(2023, 11, 20),
				stock),
			createStockDividend(
				LocalDate.of(2024, 3, 29),
				LocalDate.of(2024, 3, 31),
				LocalDate.of(2024, 5, 17),
				stock),
			createStockDividend(
				LocalDate.of(2024, 6, 28),
				LocalDate.of(2024, 6, 30),
				LocalDate.of(2024, 8, 16),
				stock),
			createStockDividend(
				LocalDate.of(2024, 9, 27),
				LocalDate.of(2024, 9, 30),
				LocalDate.of(2024, 11, 20),
				stock)
		);
	}

	private NotificationPreference createNotificationPreference(Member member) {
		return NotificationPreference.builder()
			.browserNotify(true)
			.targetGainNotify(true)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.member(member)
			.build();
	}
}
