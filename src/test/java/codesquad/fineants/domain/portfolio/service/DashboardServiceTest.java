package codesquad.fineants.domain.portfolio.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.dividend.domain.entity.StockDividend;
import codesquad.fineants.domain.dividend.repository.StockDividendRepository;
import codesquad.fineants.domain.holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.holding.repository.PortfolioHoldingRepository;
import codesquad.fineants.domain.kis.client.KisCurrentPrice;
import codesquad.fineants.domain.kis.repository.CurrentPriceRepository;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.portfolio.domain.dto.response.DashboardLineChartResponse;
import codesquad.fineants.domain.portfolio.domain.dto.response.DashboardPieChartResponse;
import codesquad.fineants.domain.portfolio.domain.dto.response.OverviewResponse;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.repository.PortfolioRepository;
import codesquad.fineants.domain.portfolio_gain_history.domain.entity.PortfolioGainHistory;
import codesquad.fineants.domain.portfolio_gain_history.repository.PortfolioGainHistoryRepository;
import codesquad.fineants.domain.purchasehistory.domain.dto.request.PurchaseHistoryCreateRequest;
import codesquad.fineants.domain.purchasehistory.domain.entity.PurchaseHistory;
import codesquad.fineants.domain.purchasehistory.repository.PurchaseHistoryRepository;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockRepository;

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
	@Autowired
	private CurrentPriceRepository currentPriceRepository;

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
		Member member = createMember();
		member = memberRepository.save(member);

		// when
		OverviewResponse response = dashboardService.getOverview(member.getId());

		// then
		Member finalMember = member;
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
				.containsExactly(finalMember.getNickname(), Money.zero(), Money.zero(), Money.zero(),
					codesquad.fineants.domain.common.money.Percentage.zero(), Money.zero(),
					codesquad.fineants.domain.common.money.Percentage.zero())
		);
	}

	@DisplayName("사용자는 포트폴리오의 오버뷰를 조회한다")
	@Test
	void getOverviewWithPortfolio() {
		// given
		Member member = memberRepository.save(createMember());

		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
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

		currentPriceRepository.addCurrentPrice(KisCurrentPrice.create(stock.getTickerSymbol(), 72900L));
		// when
		OverviewResponse response = dashboardService.getOverview(member.getId());

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
		Stock stock = stockRepository.save(createSamsungStock());
		portfolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));

		currentPriceRepository.addCurrentPrice(KisCurrentPrice.create(stock.getTickerSymbol(), 50000L));
		// when
		OverviewResponse response = dashboardService.getOverview(member.getId());

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
		Member member = createMember();
		member = memberRepository.save(member);

		Money budget = Money.won(1000000);
		Money targetGain = Money.won(1500000);
		Money maximumLoss = Money.won(900000);
		Portfolio portfolio = portfolioRepository.save(createPortfolio(
				member,
				"내꿈은 워렌버핏",
				budget,
				targetGain,
				maximumLoss
			)
		);
		Portfolio portfolio1 = portfolioRepository.save(createPortfolio(
				member,
				"내꿈은 워렌버핏1",
				budget,
				targetGain,
				maximumLoss
			)
		);
		Stock stock = stockRepository.save(createSamsungStock());

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
		currentPriceRepository.addCurrentPrice(KisCurrentPrice.create(stock.getTickerSymbol(), 60000L));
		// when
		List<DashboardPieChartResponse> responses = dashboardService.getPieChart(member.getId());

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
		Member member = createMember();
		member = memberRepository.save(member);

		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		portfolioGainHistoryRepository.save(PortfolioGainHistory.builder()
			.totalGain(Money.won(100L))
			.dailyGain(Money.won(50L))
			.currentValuation(Money.won(60L))
			.cash(Money.won(20L))
			.portfolio(portfolio)
			.build());
		portfolioGainHistoryRepository.save(PortfolioGainHistory.builder()
			.totalGain(Money.won(100L))
			.dailyGain(Money.won(50L))
			.currentValuation(Money.won(60L))
			.cash(Money.won(20L))
			.portfolio(portfolio)
			.build());

		// when
		List<DashboardLineChartResponse> responses = dashboardService.getLineChart(member.getId());

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
	
	private List<StockDividend> createStockDividendWith(Stock stock) {
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
