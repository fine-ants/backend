package codesquad.fineants.domain.portfolio.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.dividend.repository.StockDividendRepository;
import codesquad.fineants.domain.gainhistory.domain.entity.PortfolioGainHistory;
import codesquad.fineants.domain.gainhistory.repository.PortfolioGainHistoryRepository;
import codesquad.fineants.domain.holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.holding.repository.PortfolioHoldingRepository;
import codesquad.fineants.domain.kis.client.KisCurrentPrice;
import codesquad.fineants.domain.kis.repository.CurrentPriceRedisRepository;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.portfolio.domain.dto.response.DashboardLineChartResponse;
import codesquad.fineants.domain.portfolio.domain.dto.response.DashboardPieChartResponse;
import codesquad.fineants.domain.portfolio.domain.dto.response.OverviewResponse;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.repository.PortfolioRepository;
import codesquad.fineants.domain.purchasehistory.domain.dto.request.PurchaseHistoryCreateRequest;
import codesquad.fineants.domain.purchasehistory.domain.entity.PurchaseHistory;
import codesquad.fineants.domain.purchasehistory.repository.PurchaseHistoryRepository;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockRepository;

class DashboardServiceTest extends AbstractContainerBaseTest {
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
	private CurrentPriceRedisRepository currentPriceRedisRepository;

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

		LocalDateTime purchaseDate = LocalDateTime.now();
		Count numShares = Count.from(3);
		Money purchasePricePerShare = Money.won(50000.0);
		String memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePricePerShare, memo, portfolioHolding));

		currentPriceRedisRepository.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 72900L));
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
					Money.won(2166),
					codesquad.fineants.domain.common.money.Percentage.from(0.0099)
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

		currentPriceRedisRepository.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 50000L));
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
		currentPriceRedisRepository.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 60000L));
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
		portfolioGainHistoryRepository.save(
			PortfolioGainHistory.create(
				Money.won(100),
				Money.won(50),
				Money.won(60),
				Money.won(20),
				portfolio
			)
		);
		portfolioGainHistoryRepository.save(
			PortfolioGainHistory.create(
				Money.won(100),
				Money.won(50),
				Money.won(60),
				Money.won(20),
				portfolio
			)
		);

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
			() -> assertThat(responses).asList().hasSize(1)
		);
	}
}
