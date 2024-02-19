package codesquad.fineants.spring.api.dashboard;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

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
import codesquad.fineants.spring.AbstractContainerBaseTest;
import codesquad.fineants.spring.api.dashboard.response.DashboardLineChartResponse;
import codesquad.fineants.spring.api.dashboard.response.DashboardPieChartResponse;
import codesquad.fineants.spring.api.dashboard.response.OverviewResponse;
import codesquad.fineants.spring.api.dashboard.service.DashboardService;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;

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
	@MockBean
	private CurrentPriceManager currentPriceManager;

	@AfterEach
	void tearDown() {
		purchaseHistoryRepository.deleteAllInBatch();
		portfolioHoldingRepository.deleteAllInBatch();
		portfolioGainHistoryRepository.deleteAllInBatch();
		portfolioRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
		stockRepository.deleteAllInBatch();
	}

	@Test
	void getOverviewWhenNoPortfolio() {
		// given
		Member member = Member.builder().email("member@member.com").password("password").nickname("nick").build();
		AuthMember authMember = AuthMember.from(memberRepository.save(member));

		// when
		OverviewResponse response = dashboardService.getOverview(authMember);

		// then
		assertThat(response.getUsername()).isEqualTo(member.getNickname());
		assertThat(response.getTotalValuation()).isEqualTo(0);
	}

	@Test
	void getOverviewWithPortfolio() {
		// given
		Member member = Member.builder().email("member@member.com").password("password").nickname("nick").build();
		AuthMember authMember = AuthMember.from(memberRepository.save(member));

		Portfolio portfolio = portfolioRepository.save(Portfolio.builder()
			.name("내꿈은 워렌버핏")
			.securitiesFirm("토스")
			.budget(1000000L)
			.targetGain(1500000L)
			.maximumLoss(900000L)
			.member(member)
			.build());
		Stock stock = stockRepository.save(Stock.builder()
			.companyName("삼성전자보통주")
			.tickerSymbol("005930")
			.companyNameEng("SamsungElectronics")
			.stockCode("KR7005930003")
			.market(Market.KOSPI)
			.build());

		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(
			PortfolioHolding.of(portfolio, stock, 100L));

		PurchaseHistory purchaseHistory = purchaseHistoryRepository.save(
			PurchaseHistory.builder()
				.portfolioHolding(portfolioHolding)
				.purchaseDate(LocalDateTime.now())
				.purchasePricePerShare(100.0)
				.numShares(10L)
				.build()
		);

		given(currentPriceManager.hasCurrentPrice(anyString()))
			.willReturn(true);
		given(currentPriceManager.getCurrentPrice(anyString()))
			.willReturn(60000L);
		// when
		OverviewResponse response = dashboardService.getOverview(authMember);

		// then
		assertThat(response.getUsername()).isEqualTo(member.getNickname());
		assertThat((double)response.getTotalInvestment()).isEqualTo(
			purchaseHistory.getNumShares() * purchaseHistory.getPurchasePricePerShare());
		assertThat(response.getTotalGainRate() > 0).isTrue();
	}

	@Test
	void getPieChartTest() {
		// given
		Member member = Member.builder().email("member@member.com").password("password").nickname("nick").build();
		AuthMember authMember = AuthMember.from(memberRepository.save(member));

		Portfolio portfolio = portfolioRepository.save(Portfolio.builder()
			.name("내꿈은 워렌버핏")
			.securitiesFirm("토스")
			.budget(1000000L)
			.targetGain(1500000L)
			.maximumLoss(900000L)
			.member(member)
			.build());
		Portfolio portfolio1 = portfolioRepository.save(Portfolio.builder()
			.name("내꿈은 워렌버핏1")
			.securitiesFirm("토스")
			.budget(1000000L)
			.targetGain(1500000L)
			.maximumLoss(900000L)
			.member(member)
			.build());
		Stock stock = stockRepository.save(Stock.builder()
			.companyName("삼성전자보통주")
			.tickerSymbol("005930")
			.companyNameEng("SamsungElectronics")
			.stockCode("KR7005930003")
			.market(Market.KOSPI)
			.build());

		portfolioHoldingRepository.save(PortfolioHolding.of(portfolio, stock, 100L));
		portfolioHoldingRepository.save(PortfolioHolding.of(portfolio1, stock, 100L));

		given(currentPriceManager.hasCurrentPrice(anyString()))
			.willReturn(true);
		given(currentPriceManager.getCurrentPrice(anyString()))
			.willReturn(60000L);
		// when
		List<DashboardPieChartResponse> responses = dashboardService.getPieChart(authMember);

		// then
		assertThat(responses.get(0).getName()).isEqualTo("내꿈은 워렌버핏");
		assertThat(responses.size()).isEqualTo(2);
		assertThat(responses.get(0).getWeight()).isEqualTo(50.0);
	}

	@Test
	void getLineChartTest() {
		// given
		Member member = Member.builder().email("member@member.com").password("password").nickname("nick").build();
		AuthMember authMember = AuthMember.from(memberRepository.save(member));

		Portfolio portfolio = portfolioRepository.save(Portfolio.builder()
			.name("내꿈은 워렌버핏")
			.securitiesFirm("토스")
			.budget(1000000L)
			.targetGain(1500000L)
			.maximumLoss(900000L)
			.member(member)
			.build());
		PortfolioGainHistory portfolioGainHistory = portfolioGainHistoryRepository.save(PortfolioGainHistory.builder()
			.totalGain(100L)
			.dailyGain(50L)
			.currentValuation(60L)
			.cash(20L)
			.portfolio(portfolio)
			.build());
		PortfolioGainHistory portfolioGainHistory1 = portfolioGainHistoryRepository.save(PortfolioGainHistory.builder()
			.totalGain(100L)
			.dailyGain(50L)
			.currentValuation(60L)
			.cash(20L)
			.portfolio(portfolio)
			.build());

		// when
		List<DashboardLineChartResponse> responses = dashboardService.getLineChart(authMember);

		// then
		assertThat(responses.get(0).getTime()).isEqualTo(
			LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		assertThat(responses.size()).isEqualTo(1);
		assertThat(responses.get(0).getValue()).isEqualTo(160L);
	}

}
