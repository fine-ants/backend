package co.fineants.api.domain.portfolio.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.gainhistory.repository.PortfolioGainHistoryRepository;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.portfolio.domain.dto.request.PortfolioCreateRequest;
import co.fineants.api.domain.portfolio.domain.dto.request.PortfolioModifyRequest;
import co.fineants.api.domain.portfolio.domain.dto.response.PortFolioCreateResponse;
import co.fineants.api.domain.portfolio.domain.dto.response.PortfoliosResponse;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.errors.errorcode.MemberErrorCode;
import co.fineants.api.global.errors.exception.BadRequestException;
import co.fineants.api.global.errors.exception.ConflictException;
import co.fineants.api.global.errors.exception.FineAntsException;
import co.fineants.api.global.errors.exception.ForBiddenException;
import co.fineants.api.global.util.ObjectMapperUtil;

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
	private CurrentPriceRedisRepository currentPriceRedisRepository;

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
		PortFolioCreateResponse response = service.createPortfolio(request, member.getId());

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
		Throwable throwable = catchThrowable(() -> service.createPortfolio(request, member.getId()));

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
		Throwable throwable = catchThrowable(() -> service.createPortfolio(request, member.getId()));

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
		Throwable throwable = catchThrowable(() -> service.createPortfolio(request, member.getId()));

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
		Throwable throwable = catchThrowable(() -> service.createPortfolio(request, member.getId()));

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

		setAuthentication(member);
		// when
		service.updatePortfolio(request, portfolioId, member.getId());

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

		setAuthentication(member);
		// when
		service.updatePortfolio(request, portfolioId, member.getId());

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
		portfolioRepository.save(createPortfolio(
				member,
				duplicatedName,
				Money.won(1000000),
				Money.won(1500000),
				Money.won(900000)
			)
		);
		Portfolio originPortfolio = portfolioRepository.save(createPortfolio(member));

		Map<String, Object> body = createModifiedPortfolioRequestBodyMap(duplicatedName);

		PortfolioModifyRequest request = ObjectMapperUtil.deserialize(ObjectMapperUtil.serialize(body),
			PortfolioModifyRequest.class);
		Long portfolioId = originPortfolio.getId();

		setAuthentication(member);
		// when
		Throwable throwable = catchThrowable(
			() -> service.updatePortfolio(request, portfolioId, member.getId()));

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
		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(
			() -> service.updatePortfolio(request, portfolioId, hacker.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(ForBiddenException.class)
			.hasMessage(MemberErrorCode.FORBIDDEN_MEMBER.getMessage());
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
		Map<String, Object> body = createModifiedPortfolioRequestBodyMap("내꿈은 찰리몽거", "미래에셋증권", budget, targetGain,
			maximumLoss);

		PortfolioModifyRequest request = ObjectMapperUtil.deserialize(ObjectMapperUtil.serialize(body),
			PortfolioModifyRequest.class);
		Long portfolioId = originPortfolio.getId();

		setAuthentication(member);
		// when
		Throwable throwable = catchThrowable(
			() -> service.updatePortfolio(request, portfolioId, member.getId()));

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

		setAuthentication(member);
		// when
		Throwable throwable = catchThrowable(
			() -> service.updatePortfolio(request, portfolioId, member.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage("최대 손실 금액은 예산 보다 작아야 합니다");
	}

	@DisplayName("사용자가 나의 포트폴리오들을 처음 조회한다")
	@Test
	void readMyAllPortfolio() {
		// given
		Member member = memberRepository.save(createMember());
		List<Portfolio> portfolios = new ArrayList<>();
		for (int i = 1; i <= 25; i++) {
			String name = String.format("portfolio%d", i);
			portfolios.add(createPortfolio(member, name));
		}
		portfolioRepository.saveAll(portfolios);

		// when
		PortfoliosResponse response = service.readMyAllPortfolio(member.getId());

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
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding portfolioHolding = portFolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(3);
		Money purchasePerShare = Money.won(90000);
		String memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo, portfolioHolding));
		portfolioGainHistoryRepository.save(
			PortfolioGainHistory.create(
				Money.won(-120000L),
				Money.won(-120000L),
				Money.won(730000L),
				Money.won(150000L),
				portfolio
			)
		);
		currentPriceRedisRepository.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 40000L));
		// when
		PortfoliosResponse response = service.readMyAllPortfolio(member.getId());

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

	@DisplayName("회원이 포트폴리오를 삭제한다")
	@Test
	void deletePortfolio() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createSamsungStock());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		PortfolioGainHistory portfolioGainHistory = portfolioGainHistoryRepository.save(
			PortfolioGainHistory.empty(portfolio));
		PortfolioHolding portfolioHolding = portFolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(3);
		Money purchasePerShare = Money.won(50000);
		String memo = "첫구매";
		PurchaseHistory purchaseHistory = purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo, portfolioHolding));

		setAuthentication(member);
		// when
		service.deletePortfolio(portfolio.getId(), member.getId());

		// then
		assertAll(
			() -> assertThat(portfolioRepository.existsById(portfolio.getId())).isFalse(),
			() -> assertThat(portfolioGainHistoryRepository.existsById(portfolioGainHistory.getId())).isFalse(),
			() -> assertThat(portFolioHoldingRepository.existsById(portfolioHolding.getId())).isFalse(),
			() -> assertThat(purchaseHistoryRepository.existsById(purchaseHistory.getId())).isFalse()
		);
	}

	@DisplayName("회원은 다른 회원의 포트폴리오를 삭제할 수 없다")
	@Test
	void deletePortfolio_whenDeleteOtherMemberPortfolio_thenThrowException() {
		// given
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(() -> service.deletePortfolio(portfolio.getId(), hacker.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(ForBiddenException.class)
			.hasMessage(MemberErrorCode.FORBIDDEN_MEMBER.getMessage());
	}

	@DisplayName("회원이 포트폴리오들을 삭제한다")
	@Test
	void deletePortfolios() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding portfolioHolding = portFolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(3);
		Money purchasePerShare = Money.won(50000);
		String memo = "첫구매";
		PurchaseHistory purchaseHistory1 = purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo, portfolioHolding));
		PurchaseHistory purchaseHistory2 = purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo, portfolioHolding));

		PortfolioGainHistory portfolioGainHistory = portfolioGainHistoryRepository.save(
			PortfolioGainHistory.empty(portfolio));
		Portfolio portfolio2 = portfolioRepository.save(createPortfolio(member, "portfolio2"));

		List<Long> portfolioIds = Arrays.asList(portfolio.getId(), portfolio2.getId());
		setAuthentication(member);
		// when
		service.deletePortfolios(portfolioIds);

		// then
		assertThat(portfolioRepository.existsById(portfolio.getId())).isFalse();
		assertThat(portFolioHoldingRepository.existsById(portfolioHolding.getId())).isFalse();
		assertThat(purchaseHistoryRepository.existsById(purchaseHistory1.getId())).isFalse();
		assertThat(purchaseHistoryRepository.existsById(purchaseHistory2.getId())).isFalse();
		assertThat(portfolioGainHistoryRepository.existsById(portfolioGainHistory.getId())).isFalse();
		assertThat(portfolioRepository.existsById(portfolio2.getId())).isFalse();
	}

	@DisplayName("회원은 다수의 포트폴리오 삭제할때 다른 회원의 포트폴리오를 삭제할 수 없다")
	@Test
	void deletePortfolios_whenDeleteOtherMemberPortfolio_thenThrowException() {
		// given
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Portfolio portfolio2 = portfolioRepository.save(createPortfolio(member, "portfolio2"));
		List<Long> portfolioIds = Arrays.asList(portfolio.getId(), portfolio2.getId());

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(() -> service.deletePortfolios(portfolioIds));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage(MemberErrorCode.FORBIDDEN_MEMBER.getMessage());
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
