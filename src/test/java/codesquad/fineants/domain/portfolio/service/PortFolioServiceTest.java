package codesquad.fineants.domain.portfolio.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Bank;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.kis.client.KisCurrentPrice;
import codesquad.fineants.domain.kis.repository.CurrentPriceRepository;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.portfolio.domain.dto.request.PortfolioCreateRequest;
import codesquad.fineants.domain.portfolio.domain.dto.request.PortfolioModifyRequest;
import codesquad.fineants.domain.portfolio.domain.dto.request.PortfoliosDeleteRequest;
import codesquad.fineants.domain.portfolio.domain.dto.response.PortFolioCreateResponse;
import codesquad.fineants.domain.portfolio.domain.dto.response.PortfoliosResponse;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.repository.PortfolioRepository;
import codesquad.fineants.domain.portfolio_gain_history.domain.entity.PortfolioGainHistory;
import codesquad.fineants.domain.portfolio_gain_history.repository.PortfolioGainHistoryRepository;
import codesquad.fineants.domain.portfolio_holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.portfolio_holding.repository.PortfolioHoldingRepository;
import codesquad.fineants.domain.purchase_history.domain.entity.PurchaseHistory;
import codesquad.fineants.domain.purchase_history.repository.PurchaseHistoryRepository;
import codesquad.fineants.domain.stock.domain.entity.Market;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockRepository;
import codesquad.fineants.domain.stock_dividend.domain.entity.StockDividend;
import codesquad.fineants.global.errors.exception.BadRequestException;
import codesquad.fineants.global.errors.exception.ConflictException;
import codesquad.fineants.global.errors.exception.FineAntsException;
import codesquad.fineants.global.errors.exception.ForBiddenException;
import codesquad.fineants.global.util.ObjectMapperUtil;

class PortFolioServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private PortFolioService service;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private PurchaseHistoryRepository purchaseHistoryRepository;

	@Autowired
	private PortfolioHoldingRepository portFolioHoldingRepository;

	@Autowired
	private PortfolioGainHistoryRepository portfolioGainHistoryRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private CurrentPriceRepository currentPriceRepository;

	@AfterEach
	void tearDown() {
		purchaseHistoryRepository.deleteAllInBatch();
		portFolioHoldingRepository.deleteAllInBatch();
		portfolioGainHistoryRepository.deleteAllInBatch();
		portfolioRepository.deleteAllInBatch();
		stockRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
	}

	@DisplayName("회원이 포트폴리오를 추가한다")
	@CsvSource(value = {
		"토스증권,1000000,1500000,900000",
		"토스증권,0,0,0",
		"토스증권,0,1500000,900000",
		"토스증권,1000000,0,0",
		"토스증권,1000000,1500000,0",
		"토스증권,1000000,0,900000"})
	@ParameterizedTest
	void addPortfolio(String securitiesFirm, Long budget, Long targetGain, Long maximumLoss) {
		// given
		Member member = memberRepository.save(createMember());
		Map<String, Object> body = createAddPortfolioRequestBodyMap(securitiesFirm, budget, targetGain, maximumLoss);

		PortfolioCreateRequest request = ObjectMapperUtil.deserialize(ObjectMapperUtil.serialize(body),
			PortfolioCreateRequest.class);

		// when
		PortFolioCreateResponse response = service.createPortfolio(request, AuthMember.from(member));

		// then
		assertAll(
			() -> assertThat(response).isNotNull(),
			() -> assertThat(portfolioRepository.existsById(Objects.requireNonNull(response).getPortfolioId())).isTrue()
		);
	}

	@DisplayName("회원은 포트폴리오를 추가할때 목표수익금액이 예산보다 같거나 작으면 안된다")
	@MethodSource("provideInvalidTargetGain")
	@ParameterizedTest
	void addPortfolioWithTargetGainIsEqualLessThanBudget(Long targetGain) {
		// given
		Member member = memberRepository.save(createMember());
		Map<String, Object> body = new HashMap<>();
		body.put("name", "내꿈은 워렌버핏");
		body.put("securitiesFirm", "토스증권");
		body.put("budget", 1000000L);
		body.put("targetGain", targetGain);
		body.put("maximumLoss", 900000L);

		PortfolioCreateRequest request = ObjectMapperUtil.deserialize(ObjectMapperUtil.serialize(body),
			PortfolioCreateRequest.class);

		// when
		Throwable throwable = catchThrowable(() -> service.createPortfolio(request, AuthMember.from(member)));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage("목표 수익금액은 예산보다 커야 합니다");
	}

	@DisplayName("회원은 포트폴리오를 추가할때 최대손실율이 예산보다 같거나 크면 안된다")
	@MethodSource("provideInvalidMaximumLoss")
	@ParameterizedTest
	void addPortfolioWithMaximumLossIsEqualGreaterThanBudget(Long maximumLoss) {
		// given
		Member member = memberRepository.save(createMember());
		Map<String, Object> body = new HashMap<>();
		body.put("name", "내꿈은 워렌버핏");
		body.put("securitiesFirm", "토스증권");
		body.put("budget", 1000000L);
		body.put("targetGain", 1500000L);
		body.put("maximumLoss", maximumLoss);

		PortfolioCreateRequest request = ObjectMapperUtil.deserialize(ObjectMapperUtil.serialize(body),
			PortfolioCreateRequest.class);

		// when
		Throwable throwable = catchThrowable(() -> service.createPortfolio(request, AuthMember.from(member)));

		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.extracting("message")
			.isEqualTo("최대 손실 금액은 예산 보다 작아야 합니다");
	}

	@DisplayName("회원은 내가 가지고 있는 포트폴리오들중에서 동일한 이름을 가지는 포트폴리오를 추가할 수는 없다")
	@Test
	void addPortfolioWithDuplicateName() {
		// given
		Member member = memberRepository.save(createMember());
		portfolioRepository.save(createPortfolio(member));

		Map<String, Object> body = createAddPortfolioRequestBodyMap();

		PortfolioCreateRequest request = ObjectMapperUtil.deserialize(ObjectMapperUtil.serialize(body),
			PortfolioCreateRequest.class);

		// when
		Throwable throwable = catchThrowable(() -> service.createPortfolio(request, AuthMember.from(member)));

		// then
		assertThat(throwable)
			.isInstanceOf(ConflictException.class)
			.extracting("message")
			.isEqualTo("포트폴리오 이름이 중복되었습니다");
	}

	@DisplayName("회원은 포트폴리오 추가시 목록에 없는 증권사를 입력하여 추가할 수 없다")
	@Test
	void addPortfolio_shouldNotAllowNonExistingSecurities() {
		Member member = memberRepository.save(createMember());
		Map<String, Object> body = createAddPortfolioRequestBodyMap("없는증권");

		PortfolioCreateRequest request = ObjectMapperUtil.deserialize(ObjectMapperUtil.serialize(body),
			PortfolioCreateRequest.class);

		// when
		Throwable throwable = catchThrowable(() -> service.createPortfolio(request, AuthMember.from(member)));

		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage("해당 증권사는 포함되어 있지 않습니다");
	}

	@DisplayName("회원이 포트폴리오를 수정한다")
	@CsvSource(value = {
		"내꿈은 워렌버핏2,미래에셋증권,1000000,1500000,900000",
		"내꿈은 워렌버핏2,미래에셋증권,0,0,0",
		"내꿈은 워렌버핏2,미래에셋증권,0,1500000,900000",
		"내꿈은 워렌버핏2,미래에셋증권,1000000,0,0",
		"내꿈은 워렌버핏2,미래에셋증권,1000000,1500000,0",
		"내꿈은 워렌버핏2,미래에셋증권,1000000,0,900000"})
	@ParameterizedTest
	void updatePortfolio(String name, String securitiesFirm, Long budget, Long targetGain, Long maximumLoss) {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio originPortfolio = portfolioRepository.save(createPortfolio(member));

		Map<String, Object> body = createModifiedPortfolioRequestBodyMap(name, securitiesFirm, budget, targetGain,
			maximumLoss);

		PortfolioModifyRequest request = ObjectMapperUtil.deserialize(ObjectMapperUtil.serialize(body),
			PortfolioModifyRequest.class);
		Long portfolioId = originPortfolio.getId();

		// when
		service.updatePortfolio(request, portfolioId, AuthMember.from(member));

		// then
		Portfolio changePortfolio = portfolioRepository.findById(portfolioId).orElseThrow();
		assertThat(changePortfolio.getName()).isEqualTo(name);
		assertThat(changePortfolio.getSecuritiesFirm()).isEqualTo(securitiesFirm);
		assertThat(changePortfolio.getBudget())
			.isEqualByComparingTo(Money.won(budget));
		assertThat(changePortfolio.getTargetGain()).isEqualByComparingTo(Money.won(targetGain));
		assertThat(changePortfolio.getMaximumLoss()).isEqualByComparingTo(Money.won(maximumLoss));
	}

	@DisplayName("회원은 포트폴리오의 정보를 수정시 이름이 그대로인 경우 그대로 수정합니다.")
	@Test
	void updatePortfolio_whenNameUnchanged_thenNoDuplicateCheckAndApplyChanges() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio originPortfolio = portfolioRepository.save(createPortfolio(member));

		Map<String, Object> body = createModifiedPortfolioRequestBodyMap(originPortfolio.getName());

		PortfolioModifyRequest request = ObjectMapperUtil.deserialize(ObjectMapperUtil.serialize(body),
			PortfolioModifyRequest.class);
		Long portfolioId = originPortfolio.getId();

		// when
		service.updatePortfolio(request, portfolioId, AuthMember.from(member));

		// then
		Portfolio changePortfolio = portfolioRepository.findById(portfolioId).orElseThrow();

		assertThat(changePortfolio)
			.extracting(Portfolio::getName, Portfolio::getSecuritiesFirm, Portfolio::getBudget,
				Portfolio::getTargetGain, Portfolio::getMaximumLoss)
			.usingComparatorForType(Money::compareTo, Money.class)
			.containsExactly("내꿈은 워렌버핏", "미래에셋증권", Money.won(1500000L), Money.won(2000000L), Money.won(900000L));
	}

	@DisplayName("회원이 포트폴리오의 이름을 수정할때 본인이 가지고 있는 다른 포트폴리오의 이름과 중복될 수 없다")
	@Test
	void updatePortfolio_whenMemberChangeName_thenNoDuplicateWithNameInOtherMyPortfolios() {
		// given
		Member member = memberRepository.save(createMember());
		String duplicatedName = "내꿈은 찰리몽거";
		portfolioRepository.save(createPortfolio(member, duplicatedName));
		Portfolio originPortfolio = portfolioRepository.save(createPortfolio(member));

		Map<String, Object> body = createModifiedPortfolioRequestBodyMap(duplicatedName);

		PortfolioModifyRequest request = ObjectMapperUtil.deserialize(ObjectMapperUtil.serialize(body),
			PortfolioModifyRequest.class);
		Long portfolioId = originPortfolio.getId();

		// when
		Throwable throwable = catchThrowable(
			() -> service.updatePortfolio(request, portfolioId, AuthMember.from(member)));

		// then
		assertThat(throwable)
			.isInstanceOf(ConflictException.class)
			.hasMessage("포트폴리오 이름이 중복되었습니다");
	}

	@DisplayName("회원은 다른사람의 포트폴리오 정보를 수정할 수 없다")
	@Test
	void updatePortfolio_whenMemberTriesToUpdateOtherPersonPortfolio_thenModificationNotAllowed() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio originPortfolio = portfolioRepository.save(createPortfolio(member));

		Map<String, Object> body = createModifiedPortfolioRequestBodyMap("내꿈은 찰리몽거");

		PortfolioModifyRequest request = ObjectMapperUtil.deserialize(ObjectMapperUtil.serialize(body),
			PortfolioModifyRequest.class);
		Long portfolioId = originPortfolio.getId();

		Member hacker = memberRepository.save(createMember("hacker1234", "hack1234@naver.com"));
		// when
		Throwable throwable = catchThrowable(
			() -> service.updatePortfolio(request, portfolioId, AuthMember.from(hacker)));

		// then
		assertThat(throwable)
			.isInstanceOf(ForBiddenException.class)
			.hasMessage("포트폴리오에 대한 권한이 없습니다");
	}

	@DisplayName("회원이 포트폴리오 정보 수정시 예산이 목표수익금액보다 같거나 작게 수정할 수 없다")
	@CsvSource(value = {"900000", "1000000"})
	@ParameterizedTest
	void updatePortfolio_whenMemberAttemptsToUpdateWithBudgetLessThanTargetGain_thenModificationNotAllowed(
		long targetGain) {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio originPortfolio = portfolioRepository.save(createPortfolio(member));

		long budget = 1000000L;
		long maximumLoss = 900000L;
		Map<String, Object> body = createModifiedPortfolioRequestBodyMap("내꿈은 찰리몽거", "미래애셋증권", budget, targetGain,
			maximumLoss);

		PortfolioModifyRequest request = ObjectMapperUtil.deserialize(ObjectMapperUtil.serialize(body),
			PortfolioModifyRequest.class);
		Long portfolioId = originPortfolio.getId();

		// when
		Throwable throwable = catchThrowable(
			() -> service.updatePortfolio(request, portfolioId, AuthMember.from(member)));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage("목표 수익금액은 예산보다 커야 합니다");
	}

	@DisplayName("회원이 포트폴리오 정보 수정시 예산이 최대손실금액보다 같거나 작게 수정할 수 없다")
	@CsvSource(value = {"1500000", "1000000"})
	@ParameterizedTest
	void updatePortfolio_whenMemberAttemptsToUpdateWithBudgetLessThanMaximumLoss_thenModificationNotAllowed(
		long maximumLoss) {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio originPortfolio = portfolioRepository.save(createPortfolio(member));

		long budget = 1000000L;
		long targetGain = 1500000L;
		Map<String, Object> body = createModifiedPortfolioRequestBodyMap("내꿈은 찰리몽거", "미래에셋증권", budget, targetGain,
			maximumLoss);

		PortfolioModifyRequest request = ObjectMapperUtil.deserialize(ObjectMapperUtil.serialize(body),
			PortfolioModifyRequest.class);
		Long portfolioId = originPortfolio.getId();

		// when
		Throwable throwable = catchThrowable(
			() -> service.updatePortfolio(request, portfolioId, AuthMember.from(member)));

		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage("최대 손실 금액은 예산 보다 작아야 합니다");
	}

	@DisplayName("회원이 포트폴리오를 삭제한다")
	@Test
	void deletePortfolio() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createStock());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		PortfolioGainHistory portfolioGainHistory = portfolioGainHistoryRepository.save(
			createPortfolioGainHistory(portfolio));
		PortfolioHolding portfolioHolding = portFolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		PurchaseHistory purchaseHistory = purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding));

		// when
		service.deletePortfolio(portfolio.getId(), AuthMember.from(member));

		// then
		assertAll(
			() -> assertThat(portfolioRepository.existsById(portfolio.getId())).isFalse(),
			() -> assertThat(portfolioGainHistoryRepository.existsById(portfolioGainHistory.getId())).isFalse(),
			() -> assertThat(portFolioHoldingRepository.existsById(portfolioHolding.getId())).isFalse(),
			() -> assertThat(purchaseHistoryRepository.existsById(purchaseHistory.getId())).isFalse()
		);
	}

	@DisplayName("사용자가 나의 포트폴리오들을 처음 조회한다")
	@Test
	void readMyAllPortfolio() {
		// given
		Member member = memberRepository.save(createMember());
		List<Portfolio> portfolios = new ArrayList<>();
		for (int i = 1; i <= 25; i++) {
			portfolios.add(createPortfolioWithRandomName(member));
		}
		portfolioRepository.saveAll(portfolios);

		// when
		PortfoliosResponse response = service.readMyAllPortfolio(AuthMember.from(member));

		// then
		assertAll(
			() -> assertThat(response).extracting("portfolios")
				.asList()
				.hasSize(25)
		);
	}

	@DisplayName("사용자가 한 포트폴리오의 당일변동 손익이 음수인 경우 당일변 손익률도 음수여야 한다")
	@Test
	void readMyAllPortfolio_whenDailyGainIsMinus_thenDailGainRateIsMinus() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding portfolioHolding = portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));
		purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding, 3L, 90000.0));
		portfolioGainHistoryRepository.save(PortfolioGainHistory.builder()
			.totalGain(Money.won(-120000L))
			.dailyGain(Money.won(-120000L))
			.cash(Money.won(730000L))
			.currentValuation(Money.won(150000L))
			.portfolio(portfolio)
			.build());

		currentPriceRepository.addCurrentPrice(KisCurrentPrice.create(stock.getTickerSymbol(), 40000L));
		// when
		PortfoliosResponse response = service.readMyAllPortfolio(AuthMember.from(member));

		// then
		assertThat(response.getPortfolios())
			.asList()
			.extracting("dailyGain", "dailyGainRate", "totalGain", "totalGainRate")
			.containsExactlyInAnyOrder(Tuple.tuple(
				Money.won(-30000),
				Percentage.from(-0.2),
				Money.won(-150000),
				Percentage.from(-0.5556)
			));
	}

	@DisplayName("회원이 포트폴리오들을 삭제한다")
	@Test
	void deletePortfolios() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding portfolioHolding = portFolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		PurchaseHistory purchaseHistory1 = purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding));
		PurchaseHistory purchaseHistory2 = purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding));
		PortfolioGainHistory portfolioGainHistory = portfolioGainHistoryRepository.save(
			createPortfolioGainHistory(portfolio));
		Portfolio portfolio2 = portfolioRepository.save(createPortfolioWithRandomName(member));

		PortfoliosDeleteRequest request = new PortfoliosDeleteRequest(List.of(portfolio.getId(), portfolio2.getId()));

		// when
		service.deletePortfolios(request, AuthMember.from(member));

		// then
		assertThat(portfolioRepository.existsById(portfolio.getId())).isFalse();
		assertThat(portFolioHoldingRepository.existsById(portfolioHolding.getId())).isFalse();
		assertThat(purchaseHistoryRepository.existsById(purchaseHistory1.getId())).isFalse();
		assertThat(purchaseHistoryRepository.existsById(purchaseHistory2.getId())).isFalse();
		assertThat(portfolioGainHistoryRepository.existsById(portfolioGainHistory.getId())).isFalse();
		assertThat(portfolioRepository.existsById(portfolio2.getId())).isFalse();
	}

	private Member createMember() {
		return createMember("일개미1234", "kim1234@gmail.com");
	}

	private Member createMember(String nickname, String email) {
		return Member.builder()
			.nickname(nickname)
			.email(email)
			.password("kim1234@")
			.provider("local")
			.build();
	}

	private Portfolio createPortfolio(Member member) {
		return createPortfolio(member, "내꿈은 워렌버핏");
	}

	private Portfolio createPortfolio(Member member, String name) {
		return Portfolio.builder()
			.name(name)
			.securitiesFirm("토스증권")
			.budget(Money.won(1000000L))
			.targetGain(Money.won(1500000L))
			.maximumLoss(Money.won(900000L))
			.member(member)
			.targetGainIsActive(false)
			.maximumLossIsActive(false)
			.build();
	}

	private Portfolio createPortfolioWithRandomName(Member member) {
		String randomPostfix = UUID.randomUUID().toString().substring(0, 10);
		return Portfolio.builder()
			.name("내꿈은 워렌버핏" + randomPostfix)
			.securitiesFirm("토스증권")
			.budget(Money.won(1000000L))
			.targetGain(Money.won(1500000L))
			.maximumLoss(Money.won(900000L))
			.member(member)
			.targetGainIsActive(false)
			.maximumLossIsActive(false)
			.build();
	}

	private PortfolioGainHistory createPortfolioGainHistory(Portfolio portfolio) {
		Bank bank = Bank.getInstance();
		return PortfolioGainHistory.builder()
			.totalGain(bank.toWon(portfolio.calculateTotalGain()))
			.dailyGain(bank.toWon(portfolio.calculateDailyGain(PortfolioGainHistory.empty())))
			.cash(bank.toWon(portfolio.calculateBalance()))
			.currentValuation(bank.toWon(portfolio.calculateTotalCurrentValuation()))
			.portfolio(portfolio)
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

	private static Stream<Arguments> provideInvalidTargetGain() {
		return Stream.of(
			Arguments.of(900000L),
			Arguments.of(1000000L)
		);
	}

	private static Stream<Arguments> provideInvalidMaximumLoss() {
		return Stream.of(
			Arguments.of(1000000L),
			Arguments.of(1100000L)
		);
	}

	private Map<String, Object> createModifiedPortfolioRequestBodyMap(String name) {
		return createModifiedPortfolioRequestBodyMap(
			name,
			"미래에셋증권",
			1500000L,
			2000000L,
			900000L);
	}

	private Map<String, Object> createModifiedPortfolioRequestBodyMap(String name, String securitiesFirm, long budget,
		long targetGain,
		long maximumLoss) {
		Map<String, Object> body = new HashMap<>();
		body.put("name", name);
		body.put("securitiesFirm", securitiesFirm);
		body.put("budget", budget);
		body.put("targetGain", targetGain);
		body.put("maximumLoss", maximumLoss);
		return body;
	}

	private Map<String, Object> createAddPortfolioRequestBodyMap() {
		return createAddPortfolioRequestBodyMap("토스증권");
	}

	private Map<String, Object> createAddPortfolioRequestBodyMap(String securitiesFirm) {
		return createAddPortfolioRequestBodyMap(securitiesFirm, 1000000L, 1500000L, 900000L);
	}

	private Map<String, Object> createAddPortfolioRequestBodyMap(String securitiesFirm, Long budget, Long targetGain,
		Long maximumLoss) {
		Map<String, Object> body = new HashMap<>();
		body.put("name", "내꿈은 워렌버핏");
		body.put("securitiesFirm", securitiesFirm);
		body.put("budget", budget);
		body.put("targetGain", targetGain);
		body.put("maximumLoss", maximumLoss);
		return body;
	}
}