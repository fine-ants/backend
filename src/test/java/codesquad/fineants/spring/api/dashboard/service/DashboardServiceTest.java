package codesquad.fineants.spring.api.dashboard.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
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
import codesquad.fineants.spring.api.dashboard.response.DashboardLineChartResponse;
import codesquad.fineants.spring.api.dashboard.response.DashboardPieChartResponse;
import codesquad.fineants.spring.api.dashboard.response.OverviewResponse;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.purchase_history.request.PurchaseHistoryCreateRequest;

public class DashboardServiceTest extends AbstractContainerBaseTest {
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private PortfolioRepository portfolioRepository;
	@Autowired
	private DashboardService dashboardService;
	@Autowired
	private PurchaseHistoryRepository purchaseHistoryRepository;
	@Autowired
	private PortfolioGainHistoryRepository portfolioGainHistoryRepository;
	@Autowired
	private PortfolioHoldingRepository portfolioHoldingRepository;
	@Autowired
	private StockRepository stockRepository;
	@Autowired
	private StockDividendRepository stockDividendRepository;
	@MockBean
	private CurrentPriceManager currentPriceManager;

	@AfterEach
	void tearDown() {
		purchaseHistoryRepository.deleteAllInBatch();
		portfolioHoldingRepository.deleteAllInBatch();
		portfolioGainHistoryRepository.deleteAllInBatch();
		portfolioRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
		stockDividendRepository.deleteAllInBatch();
		stockRepository.deleteAllInBatch();
	}

	@Test
	void getOverviewWhenNoPortfolio() {
		// given
		Member member = Member.builder()
			.email("member@member.com").password("password").nickname("nick").build();
		AuthMember authMember = AuthMember.from(memberRepository.save(member));

		// when
		OverviewResponse response = dashboardService.getOverview(authMember);

		// then
		Assertions.assertAll(
			() -> assertThat(response)
				.extracting(
					OverviewResponse::getUsername,
					OverviewResponse::getTotalValuation,
					OverviewResponse::getTotalInvestment,
					OverviewResponse::getTotalGain,
					OverviewResponse::getTotalGainRate,
					OverviewResponse::getTotalAnnualDividend,
					OverviewResponse::getTotalAnnualDividendYield
				)
				.usingComparatorForType(Money::compareTo, Money.class)
				.usingComparatorForType(codesquad.fineants.domain.common.money.Percentage::compareTo,
					codesquad.fineants.domain.common.money.Percentage.class)
				.containsExactly(member.getNickname(), Money.zero(), Money.zero(), Money.zero(),
					codesquad.fineants.domain.common.money.Percentage.zero(), Money.zero(),
					codesquad.fineants.domain.common.money.Percentage.zero())
		);
	}

	@DisplayName("사용자는 포트폴리오의 오버뷰를 조회한다")
	@Test
	void getOverviewWithPortfolio() {
		// given
		Member member = Member.builder().email("member@member.com").password("password").nickname("nick").build();
		AuthMember authMember = AuthMember.from(memberRepository.save(member));

		Portfolio portfolio = portfolioRepository.save(Portfolio.builder()
			.name("내꿈은 워렌버핏")
			.securitiesFirm("토스")
			.budget(Money.won(1000000L))
			.targetGain(Money.won(1500000L))
			.maximumLoss(Money.won(900000L))
			.member(member)
			.build());
		Stock stock = stockRepository.save(Stock.builder()
			.companyName("삼성전자보통주")
			.tickerSymbol("005930")
			.companyNameEng("SamsungElectronics")
			.stockCode("KR7005930003")
			.market(Market.KOSPI)
			.build());
		stockDividendRepository.saveAll(createStockDividendWith(stock));

		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(
			PortfolioHolding.of(portfolio, stock, Money.won(72900L)));

		purchaseHistoryRepository.save(
			PurchaseHistory.builder()
				.portfolioHolding(portfolioHolding)
				.purchaseDate(LocalDateTime.now())
				.purchasePricePerShare(Money.won(50000.0))
				.numShares(Count.from(3L))
				.build()
		);

		given(currentPriceManager.hasCurrentPrice(anyString()))
			.willReturn(true);
		given(currentPriceManager.getCurrentPrice(anyString()))
			.willReturn(Optional.of(Money.won(72900L)));
		// when
		OverviewResponse response = dashboardService.getOverview(authMember);

		// then
		Assertions.assertAll(
			() -> assertThat(response)
				.extracting(
					OverviewResponse::getUsername,
					OverviewResponse::getTotalValuation,
					OverviewResponse::getTotalInvestment,
					OverviewResponse::getTotalGain,
					OverviewResponse::getTotalGainRate,
					OverviewResponse::getTotalAnnualDividend,
					OverviewResponse::getTotalAnnualDividendYield
				).usingComparatorForType(Money::compareTo, Money.class)
				.usingComparatorForType(codesquad.fineants.domain.common.money.Percentage::compareTo,
					codesquad.fineants.domain.common.money.Percentage.class)
				.containsExactlyInAnyOrder(
					member.getNickname(),
					Money.won(1068700L),
					Money.won(150000L),
					Money.won(68700L),
					codesquad.fineants.domain.common.money.Percentage.from(0.458),
					Money.won(3249L),
					codesquad.fineants.domain.common.money.Percentage.from(0.0149)
				)
		);
	}

	@DisplayName("사용자는 포트폴리오의 종목은 있지만 매입 이력이 없어도 오버뷰를 정상 계산해야 한다")
	@Test
	void getOverview_whenHoldingNotHavePurchaseHistory_thenOK() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		portfolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));

		given(currentPriceManager.hasCurrentPrice(anyString()))
			.willReturn(true);
		given(currentPriceManager.getCurrentPrice(anyString()))
			.willReturn(Optional.of(Money.won(50000L)));

		// when
		OverviewResponse response = dashboardService.getOverview(AuthMember.from(member));

		// then
		assertThat(response)
			.extracting(
				OverviewResponse::getUsername,
				OverviewResponse::getTotalValuation,
				OverviewResponse::getTotalInvestment,
				OverviewResponse::getTotalGain,
				OverviewResponse::getTotalGainRate,
				OverviewResponse::getTotalAnnualDividend,
				OverviewResponse::getTotalAnnualDividendYield
			).usingComparatorForType(Money::compareTo, Money.class)
			.containsExactlyInAnyOrder(
				member.getNickname(),
				Money.won(1000000L),
				Money.zero(),
				Money.zero(),
				codesquad.fineants.domain.common.money.Percentage.zero(),
				Money.zero(),
				codesquad.fineants.domain.common.money.Percentage.zero()
			);
	}

	@Test
	void getPieChartTest() {
		// given
		Member member = Member.builder().email("member@member.com").password("password").nickname("nick").build();
		AuthMember authMember = AuthMember.from(memberRepository.save(member));

		Portfolio portfolio = portfolioRepository.save(Portfolio.builder()
			.name("내꿈은 워렌버핏")
			.securitiesFirm("토스")
			.budget(Money.won(1000000L))
			.targetGain(Money.won(1500000L))
			.maximumLoss(Money.won(900000L))
			.member(member)
			.build());
		Portfolio portfolio1 = portfolioRepository.save(Portfolio.builder()
			.name("내꿈은 워렌버핏1")
			.securitiesFirm("토스")
			.budget(Money.won(1000000L))
			.targetGain(Money.won(1500000L))
			.maximumLoss(Money.won(900000L))
			.member(member)
			.build());
		Stock stock = stockRepository.save(Stock.builder()
			.companyName("삼성전자보통주")
			.tickerSymbol("005930")
			.companyNameEng("SamsungElectronics")
			.stockCode("KR7005930003")
			.market(Market.KOSPI)
			.build());

		PortfolioHolding holding1 = portfolioHoldingRepository.save(
			PortfolioHolding.of(portfolio, stock, Money.won(100L)));
		PortfolioHolding holding2 = portfolioHoldingRepository.save(
			PortfolioHolding.of(portfolio1, stock, Money.won(100L)));

		purchaseHistoryRepository.save(PurchaseHistory.of(holding1,
			PurchaseHistoryCreateRequest.create(
				LocalDateTime.now(),
				Count.from(3L),
				Money.won(70000.0),
				"첫구매"
			)
		));
		purchaseHistoryRepository.save(PurchaseHistory.of(holding2,
			PurchaseHistoryCreateRequest.create(
				LocalDateTime.now(),
				Count.from(3L),
				Money.won(60000.0),
				"첫구매"
			)
		));

		given(currentPriceManager.hasCurrentPrice(anyString()))
			.willReturn(true);
		given(currentPriceManager.getCurrentPrice(anyString()))
			.willReturn(Optional.of(Money.won(60000L)));
		// when
		List<DashboardPieChartResponse> responses = dashboardService.getPieChart(authMember);

		// then
		Assertions.assertAll(
			() -> assertThat(responses.get(0).getWeight().toPercentage()).isCloseTo(50.76,
				Percentage.withPercentage(0.1)),
			() -> assertThat(responses.get(1).getWeight().toPercentage()).isCloseTo(49.24,
				Percentage.withPercentage(0.1)),
			() -> assertThat(responses)
				.asList()
				.hasSize(2)
				.extracting("id")
				.containsExactlyInAnyOrder(portfolio1.getId(), portfolio.getId())
		);
	}

	@Test
	void getLineChartTest() {
		// given
		Member member = Member.builder().email("member@member.com").password("password").nickname("nick").build();
		AuthMember authMember = AuthMember.from(memberRepository.save(member));

		Portfolio portfolio = portfolioRepository.save(Portfolio.builder()
			.name("내꿈은 워렌버핏")
			.securitiesFirm("토스")
			.budget(Money.won(1000000L))
			.targetGain(Money.won(1500000L))
			.maximumLoss(Money.won(900000L))
			.member(member)
			.build());
		PortfolioGainHistory portfolioGainHistory = portfolioGainHistoryRepository.save(PortfolioGainHistory.builder()
			.totalGain(Money.won(100L))
			.dailyGain(Money.won(50L))
			.currentValuation(Money.won(60L))
			.cash(Money.won(20L))
			.portfolio(portfolio)
			.build());
		PortfolioGainHistory portfolioGainHistory1 = portfolioGainHistoryRepository.save(PortfolioGainHistory.builder()
			.totalGain(Money.won(100L))
			.dailyGain(Money.won(50L))
			.currentValuation(Money.won(60L))
			.cash(Money.won(20L))
			.portfolio(portfolio)
			.build());

		// when
		List<DashboardLineChartResponse> responses = dashboardService.getLineChart(authMember);

		// then
		Assertions.assertAll(
			() -> assertThat(responses.stream().findAny().orElseThrow())
				.extracting(DashboardLineChartResponse::getTime, DashboardLineChartResponse::getValue)
				.usingComparatorForType(Money::compareTo, Money.class)
				.containsExactly(
					LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
					Money.won(160L)
				),
			() -> assertThat(responses.size()).isEqualTo(1)
		);
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
		return Portfolio.builder()
			.name("내꿈은 워렌버핏")
			.securitiesFirm("토스")
			.budget(Money.won(1000000L))
			.targetGain(Money.won(1500000L))
			.maximumLoss(Money.won(900000L))
			.member(member)
			.targetGainIsActive(false)
			.maximumLossIsActive(false)
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
}
