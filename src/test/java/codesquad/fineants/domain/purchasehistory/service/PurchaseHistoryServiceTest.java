package codesquad.fineants.domain.purchasehistory.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import com.google.firebase.messaging.Message;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.fcm.repository.FcmRepository;
import codesquad.fineants.domain.fcm.service.FirebaseMessagingService;
import codesquad.fineants.domain.holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.holding.repository.PortfolioHoldingRepository;
import codesquad.fineants.domain.kis.client.KisCurrentPrice;
import codesquad.fineants.domain.kis.repository.CurrentPriceRedisRepository;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.notification.repository.NotificationRepository;
import codesquad.fineants.domain.notification.repository.NotificationSentRepository;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.repository.PortfolioRepository;
import codesquad.fineants.domain.purchasehistory.domain.dto.request.PurchaseHistoryCreateRequest;
import codesquad.fineants.domain.purchasehistory.domain.dto.request.PurchaseHistoryUpdateRequest;
import codesquad.fineants.domain.purchasehistory.domain.dto.response.PurchaseHistoryCreateResponse;
import codesquad.fineants.domain.purchasehistory.domain.dto.response.PurchaseHistoryDeleteResponse;
import codesquad.fineants.domain.purchasehistory.domain.dto.response.PurchaseHistoryUpdateResponse;
import codesquad.fineants.domain.purchasehistory.domain.entity.PurchaseHistory;
import codesquad.fineants.domain.purchasehistory.repository.PurchaseHistoryRepository;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockRepository;
import codesquad.fineants.global.errors.errorcode.MemberErrorCode;
import codesquad.fineants.global.errors.errorcode.PortfolioErrorCode;
import codesquad.fineants.global.errors.errorcode.PurchaseHistoryErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;

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
	private PurchaseHistoryService service;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private FcmRepository fcmRepository;

	@Autowired
	private CurrentPriceRedisRepository currentPriceRedisRepository;

	@MockBean
	private FirebaseMessagingService firebaseMessagingService;

	@MockBean
	private NotificationSentRepository sentManager;

	@WithMockUser
	@DisplayName("사용자는 매입 이력을 추가한다")
	@CsvSource(value = {"3,1000000,1500000,900000",
		"1000000000000,50000000000000000,50000000000000001,40000000000000000",
		"10,9223372036854775807,9223372036854775808,9223372036854775806"})
	@ParameterizedTest
	void addPurchaseHistory(Count numShares, BigDecimal budget, BigDecimal targetGain, BigDecimal maximumLoss) {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(
			createPortfolio(member, "내꿈은 워렌버핏", Money.won(budget), Money.won(targetGain), Money.won(maximumLoss)));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding holding = portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));

		LocalDateTime now = LocalDateTime.now();
		Money money = Money.won(50000.0);
		PurchaseHistoryCreateRequest request = PurchaseHistoryCreateRequest.builder()
			.purchaseDate(now)
			.numShares(numShares)
			.purchasePricePerShare(money)
			.memo("첫구매")
			.build();
		currentPriceRedisRepository.addCurrentPrice(KisCurrentPrice.create(stock.getTickerSymbol(), 50000L));

		setAuthentication(member);
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
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		Stock stock2 = stockRepository.save(createDongwhaPharmStock());
		PortfolioHolding holding = portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));
		portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock2));
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, LocalDateTime.of(2023, 9, 26, 9, 30, 0), Count.from(3), Money.won(50000), "첫구매",
				holding));
		fcmRepository.save(createFcmToken("token", member));
		fcmRepository.save(createFcmToken("token2", member));

		PurchaseHistoryCreateRequest request = PurchaseHistoryCreateRequest.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(100L))
			.purchasePricePerShare(Money.won(100.0))
			.memo("첫구매")
			.build();

		currentPriceRedisRepository.addCurrentPrice(KisCurrentPrice.create(stock.getTickerSymbol(), 50000L));
		given(sentManager.hasTargetGainSendHistory(anyLong()))
			.willReturn(false);
		given(firebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));

		setAuthentication(member);
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

	@DisplayName("사용자는 매입 이력 추가시 최대 손실율에 달성하여 알림을 받는다")
	@Test
	void addPurchaseHistory_whenAchieveMaxLoss_thenSaveNotification() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding holding = portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));
		fcmRepository.save(createFcmToken("token", member));

		PurchaseHistoryCreateRequest request = PurchaseHistoryCreateRequest.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(10L))
			.purchasePricePerShare(Money.won(90000.0))
			.memo("첫구매")
			.build();

		currentPriceRedisRepository.addCurrentPrice(KisCurrentPrice.create(stock.getTickerSymbol(), 50000L));
		given(sentManager.hasTargetGainSendHistory(anyLong()))
			.willReturn(false);
		given(firebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));

		setAuthentication(member);
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
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding holding = portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));

		PurchaseHistoryCreateRequest request = PurchaseHistoryCreateRequest.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(3L))
			.purchasePricePerShare(Money.won(500000.0))
			.memo("첫구매")
			.build();

		setAuthentication(member);
		// when
		Throwable throwable = catchThrowable(() ->
			service.createPurchaseHistory(request, portfolio.getId(), holding.getId(), member.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage(PortfolioErrorCode.TOTAL_INVESTMENT_PRICE_EXCEEDS_BUDGET.getMessage());
	}

	@DisplayName("회원은 다른 회원의 포트폴리오에 매입 이력을 추가할 수 없다")
	@Test
	void createPurchaseHistory_whenOtherMemberCreate_thenThrowException() {
		// given
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding holding = portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));

		LocalDateTime now = LocalDateTime.now();
		Money money = Money.won(50000.0);
		PurchaseHistoryCreateRequest request = PurchaseHistoryCreateRequest.builder()
			.purchaseDate(now)
			.numShares(Count.from(3))
			.purchasePricePerShare(money)
			.memo("첫구매")
			.build();

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(
			() -> service.createPurchaseHistory(request, portfolio.getId(), holding.getId(), hacker.getId()));
		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage(MemberErrorCode.FORBIDDEN_MEMBER.getMessage());
	}

	@DisplayName("사용자는 매입 이력을 수정한다")
	@Test
	void modifyPurchaseHistory() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding holding = portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));
		PurchaseHistory history = purchaseHistoryRepository.save(
			createPurchaseHistory(null, LocalDateTime.of(2023, 9, 26, 9, 30, 0), Count.from(3), Money.won(50000), "첫구매",
				holding));

		PurchaseHistoryUpdateRequest request = PurchaseHistoryUpdateRequest.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(4L))
			.purchasePricePerShare(Money.won(50000.0))
			.memo("첫구매")
			.build();

		currentPriceRedisRepository.addCurrentPrice(KisCurrentPrice.create(stock.getTickerSymbol(), 50000L));

		setAuthentication(member);
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
		fcmRepository.save(createFcmToken("token", member));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding holding = portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));
		PurchaseHistory history = purchaseHistoryRepository.save(
			createPurchaseHistory(null, LocalDateTime.of(2023, 9, 26, 9, 30, 0), Count.from(3), Money.won(50000), "첫구매",
				holding));

		PurchaseHistoryUpdateRequest request = PurchaseHistoryUpdateRequest.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(100L))
			.purchasePricePerShare(Money.won(100.0))
			.memo("첫구매")
			.build();

		currentPriceRedisRepository.addCurrentPrice(KisCurrentPrice.create(stock.getTickerSymbol(), 50000L));
		given(sentManager.hasTargetGainSendHistory(anyLong()))
			.willReturn(false);
		given(firebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));

		setAuthentication(member);
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

	@DisplayName("회원은 다른 회원의 매입 이력을 수정할 수 없다")
	@Test
	void modifyPurchaseHistory_whenOtherMemberModify_thenThrowException() {
		// given
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding holding = portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));
		PurchaseHistory history = purchaseHistoryRepository.save(
			createPurchaseHistory(null, LocalDateTime.of(2023, 9, 26, 9, 30, 0), Count.from(3), Money.won(50000), "첫구매",
				holding));

		PurchaseHistoryUpdateRequest request = PurchaseHistoryUpdateRequest.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(4L))
			.purchasePricePerShare(Money.won(50000.0))
			.memo("첫구매")
			.build();

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(
			() -> service.updatePurchaseHistory(request, holding.getId(), history.getId(), portfolio.getId(),
				hacker.getId()));
		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage(MemberErrorCode.FORBIDDEN_MEMBER.getMessage());
	}

	@DisplayName("사용자는 매입 이력을 삭제한다")
	@Test
	void deletePurchaseHistory() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding holding = portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));
		PurchaseHistory history = purchaseHistoryRepository.save(
			createPurchaseHistory(null, LocalDateTime.of(2023, 9, 26, 9, 30, 0), Count.from(3), Money.won(50000), "첫구매",
				holding));

		setAuthentication(member);
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
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding holding = portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));
		PurchaseHistory history = purchaseHistoryRepository.save(
			createPurchaseHistory(null, LocalDateTime.of(2023, 9, 26, 9, 30, 0), Count.from(3), Money.won(50000), "첫구매",
				holding));
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, LocalDateTime.now(), Count.from(100), Money.won(100), "첫구매", holding));
		fcmRepository.save(createFcmToken("token", member));

		currentPriceRedisRepository.addCurrentPrice(KisCurrentPrice.create(stock.getTickerSymbol(), 50000L));
		given(sentManager.hasTargetGainSendHistory(anyLong()))
			.willReturn(false);
		given(firebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));

		setAuthentication(member);
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
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding holding = portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));
		purchaseHistoryRepository.save(purchaseHistoryRepository.save(
			createPurchaseHistory(null, LocalDateTime.of(2023, 9, 26, 9, 30, 0), Count.from(3), Money.won(50000), "첫구매",
				holding)));

		Long purchaseHistoryId = 9999L;

		setAuthentication(member);
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

	@DisplayName("회원은 다른 회원의 매입 이력을 삭제할 수 없다")
	@Test
	void deletePurchaseHistory_whenOtherMemberDelete_thenThrowException() {
		// given
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding holding = portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));
		PurchaseHistory history = purchaseHistoryRepository.save(
			createPurchaseHistory(null, LocalDateTime.of(2023, 9, 26, 9, 30, 0), Count.from(3), Money.won(50000), "첫구매",
				holding));

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(() -> service.deletePurchaseHistory(
			holding.getId(),
			history.getId(),
			portfolio.getId(),
			hacker.getId()
		));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage(MemberErrorCode.FORBIDDEN_MEMBER.getMessage());
	}
}
