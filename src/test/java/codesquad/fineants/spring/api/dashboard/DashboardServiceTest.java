package codesquad.fineants.spring.api.dashboard;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.portfolio_holding.PortfolioHoldingRepository;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.purchase_history.PurchaseHistoryRepository;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.spring.api.dashboard.response.DashboardPieChartResponse;
import codesquad.fineants.spring.api.dashboard.response.OverviewResponse;
import codesquad.fineants.spring.api.dashboard.service.DashboardService;

@ActiveProfiles("test")
@SpringBootTest
public class DashboardServiceTest {
	@Autowired
	MemberRepository memberRepository;
	@Autowired
	PortfolioRepository portfolioRepository;
	@Autowired
	DashboardService dashboardService;
	@Autowired
	PurchaseHistoryRepository purchaseHistoryRepository;
	@Autowired
	PortfolioHoldingRepository portfolioHoldingRepository;
	@Autowired
	StockRepository stockRepository;

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

		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(
			PortfolioHolding.of(portfolio, stock, 100L));
		PortfolioHolding portfolioHolding1 = portfolioHoldingRepository.save(
			PortfolioHolding.of(portfolio1, stock, 100L));

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

		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(
			PortfolioHolding.of(portfolio, stock, 100L));
		PortfolioHolding portfolioHolding1 = portfolioHoldingRepository.save(
			PortfolioHolding.of(portfolio1, stock, 100L));

		// when
		List<DashboardPieChartResponse> responses = dashboardService.getPieChart(authMember);

		// then
		assertThat(responses.get(0).getName()).isEqualTo("내꿈은 워렌버핏");
		assertThat(responses.size()).isEqualTo(2);
		assertThat(responses.get(0).getWeight()).isEqualTo(50.0);
	}

}
