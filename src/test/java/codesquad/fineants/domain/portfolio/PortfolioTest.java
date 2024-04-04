package codesquad.fineants.domain.portfolio;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioPieChartItem;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioSectorChartItem;

@ActiveProfiles("test")
class PortfolioTest {

	private Portfolio portfolio;

	private Stock stock;

	private Stock stock2;

	@BeforeEach
	void init() {
		portfolio = Portfolio.builder()
			.budget(Money.from(1000000L))
			.targetGain(Money.from(1500000L))
			.maximumLoss(Money.from(900000L))
			.build();

		stock = Stock.builder()
			.stockCode("KR7005930003")
			.tickerSymbol("005930")
			.companyName("삼성전자보통주")
			.companyNameEng("SamsungElectronics")
			.market(Market.KOSPI)
			.sector("전기전자")
			.build();

		stock2 = Stock.builder()
			.stockCode("KR7000020008")
			.tickerSymbol("000020")
			.companyName("동화약품보통주")
			.companyNameEng("DongwhaPharm")
			.market(Market.KOSPI)
			.sector("의약품")
			.build();
	}

	@DisplayName("포트폴리오의 총 손익을 계산한다")
	@Test
	void calculateTotalGain() {
		// given
		PortfolioHolding portFolioHolding = PortfolioHolding.of(portfolio, stock, Money.from(20000L));

		PurchaseHistory purchaseHistory1 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(5L))
			.purchasePricePerShare(Money.from(10000.0))
			.portfolioHolding(portFolioHolding)
			.build();

		PurchaseHistory purchaseHistory2 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(5L))
			.purchasePricePerShare(Money.from(10000.0))
			.portfolioHolding(portFolioHolding)
			.build();

		portFolioHolding.addPurchaseHistory(purchaseHistory1);
		portFolioHolding.addPurchaseHistory(purchaseHistory2);

		portfolio.addPortfolioStock(portFolioHolding);

		// when
		Money result = portfolio.calculateTotalGain();

		// then
		assertThat(result).isEqualByComparingTo(Money.from(100000L));
	}

	@DisplayName("포트폴리오의 총 손익율 계산한다")
	@Test
	void calculateTotalReturnRate() {
		// given
		PortfolioHolding portFolioHolding = PortfolioHolding.of(portfolio, stock, Money.from(20000L));

		PurchaseHistory purchaseHistory1 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(5L))
			.purchasePricePerShare(Money.from(10000.0))
			.build();

		PurchaseHistory purchaseHistory2 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(5L))
			.purchasePricePerShare(Money.from(10000.0))
			.build();

		portFolioHolding.addPurchaseHistory(purchaseHistory1);
		portFolioHolding.addPurchaseHistory(purchaseHistory2);

		portfolio.addPortfolioStock(portFolioHolding);

		// when
		Double result = portfolio.calculateTotalGainRate();

		// then
		assertThat(result).isEqualTo(100.00);
	}

	@DisplayName("사용자는 포트폴리오의 파이 차트를 요청한다")
	@Test
	void createPieChart() {
		// given
		PortfolioHolding holding1 = PortfolioHolding.of(portfolio, stock, Money.from(20000L));
		PortfolioHolding holding2 = PortfolioHolding.of(portfolio, stock2, Money.from(20000L));

		PurchaseHistory purchaseHistory1 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(5L))
			.purchasePricePerShare(Money.from(10000.0))
			.portfolioHolding(holding1)
			.build();

		PurchaseHistory purchaseHistory2 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(5L))
			.purchasePricePerShare(Money.from(20000.0))
			.portfolioHolding(holding2)
			.build();

		holding1.addPurchaseHistory(purchaseHistory1);
		holding2.addPurchaseHistory(purchaseHistory2);

		portfolio.addPortfolioStock(holding1);
		portfolio.addPortfolioStock(holding2);

		// when
		List<PortfolioPieChartItem> items = portfolio.createPieChart();

		// then
		assertThat(items)
			.asList()
			.hasSize(3)
			.extracting("name", "valuation", "totalGain")
			.containsExactlyInAnyOrder(
				Tuple.tuple("현금", 850000L, 0L),
				Tuple.tuple("삼성전자보통주", 100000L, 50000L),
				Tuple.tuple("동화약품보통주", 100000L, 0L));
	}

	@DisplayName("사용자는 포트폴리오의 섹터 차트를 요청한다")
	@Test
	void createSectorChart() {
		// given
		PortfolioHolding holding1 = PortfolioHolding.of(portfolio, stock, Money.from(20000L));
		PortfolioHolding holding2 = PortfolioHolding.of(portfolio, stock2, Money.from(20000L));

		PurchaseHistory purchaseHistory1 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(5L))
			.purchasePricePerShare(Money.from(10000.0))
			.portfolioHolding(holding1)
			.build();

		PurchaseHistory purchaseHistory2 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(5L))
			.purchasePricePerShare(Money.from(20000.0))
			.portfolioHolding(holding2)
			.build();

		holding1.addPurchaseHistory(purchaseHistory1);
		holding2.addPurchaseHistory(purchaseHistory2);

		portfolio.addPortfolioStock(holding1);
		portfolio.addPortfolioStock(holding2);

		// when
		List<PortfolioSectorChartItem> items = portfolio.createSectorChart();

		// then
		assertThat(items)
			.asList()
			.hasSize(3)
			.extracting("sector")
			.containsExactlyInAnyOrder("현금", "의약품", "전기전자");
	}
}
