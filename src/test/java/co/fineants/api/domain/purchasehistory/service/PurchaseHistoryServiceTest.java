package co.fineants.api.domain.purchasehistory.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.fcm.repository.FcmRepository;
import co.fineants.api.domain.fcm.service.FirebaseMessagingService;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.domain.notification.repository.NotificationSentRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.domain.dto.request.PurchaseHistoryCreateRequest;
import co.fineants.api.domain.purchasehistory.domain.dto.request.PurchaseHistoryUpdateRequest;
import co.fineants.api.domain.purchasehistory.domain.dto.response.PurchaseHistoryCreateResponse;
import co.fineants.api.domain.purchasehistory.domain.dto.response.PurchaseHistoryDeleteResponse;
import co.fineants.api.domain.purchasehistory.domain.dto.response.PurchaseHistoryUpdateResponse;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.errors.errorcode.MemberErrorCode;
import co.fineants.api.global.errors.errorcode.PortfolioErrorCode;
import co.fineants.api.global.errors.errorcode.PurchaseHistoryErrorCode;
import co.fineants.api.global.errors.exception.FineAntsException;
import reactor.core.publisher.Mono;

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

	@MockBean
	private KisClient kisClient;

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
		currentPriceRedisRepository.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 50000L));

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

	@DisplayName("포트폴리오의 잔고와 추가하는 매입 이력의 금액이 동일해도 매입 이력이 추가된다")
	@Test
	void addPurchaseHistory_givenPortfolio_whenBalanceEqualPurchaseAmount_thenAddPurchaseHistory() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding holding = portFolioHoldingRepository.save(createPortfolioHolding(portfolio, stock, 50_000L));
		PurchaseHistory history = purchaseHistoryRepository.save(
			createPurchaseHistory(null, LocalDateTime.now(), Count.from(3), Money.won(50_000),
				"메모", holding));
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		setAuthentication(member);
		// when
		PurchaseHistoryCreateRequest request = PurchaseHistoryCreateRequest.create(LocalDateTime.now(), Count.from(17),
			Money.won(50_000), "메모");
		PurchaseHistoryCreateResponse response = service.createPurchaseHistory(request, portfolio.getId(),
			holding.getId(), member.getId());
		// then
		Assertions.assertThat(response).isNotNull();
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

		currentPriceRedisRepository.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 50000L));

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
		given(kisClient.fetchCurrentPrice(anyString()))
			.willReturn(Mono.just(KisCurrentPrice.create(stock.getTickerSymbol(), 50000L)));

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
