package codesquad.fineants.domain.portfolio.domain.entity;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Bank;
import codesquad.fineants.domain.common.money.Expression;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.RateDivision;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio_holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.purchase_history.domain.entity.PurchaseHistory;
import codesquad.fineants.domain.stock.domain.entity.Market;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.portfolio_holding.domain.dto.response.PortfolioPieChartItem;
import codesquad.fineants.domain.portfolio_holding.domain.dto.response.PortfolioSectorChartItem;

@ActiveProfiles("test")
class PortfolioTest {

	@DisplayName("포트폴리오의 총 손익을 계산한다")
	@Test
	void calculateTotalGain() {
		// given
		Portfolio portfolio = createPortfolio();
		Stock stock = createSamsungStock();
		PortfolioHolding portFolioHolding = PortfolioHolding.of(portfolio, stock, Money.won(20000L));

		PurchaseHistory purchaseHistory1 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(5L))
			.purchasePricePerShare(Money.won(10000.0))
			.portfolioHolding(portFolioHolding)
			.build();

		PurchaseHistory purchaseHistory2 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(5L))
			.purchasePricePerShare(Money.won(10000.0))
			.portfolioHolding(portFolioHolding)
			.build();

		portFolioHolding.addPurchaseHistory(purchaseHistory1);
		portFolioHolding.addPurchaseHistory(purchaseHistory2);

		portfolio.addPortfolioStock(portFolioHolding);

		// when
		Expression result = portfolio.calculateTotalGain();

		// then
		Money amount = Bank.getInstance().toWon(result);
		assertThat(amount).isEqualByComparingTo(Money.won(100000L));
	}

	@DisplayName("포트폴리오의 총 손익율 계산한다")
	@Test
	void calculateTotalReturnRate() {
		// given
		Portfolio portfolio = createPortfolio();
		Stock stock = createSamsungStock();
		PortfolioHolding portFolioHolding = PortfolioHolding.of(portfolio, stock, Money.won(20000L));

		PurchaseHistory purchaseHistory1 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(5L))
			.purchasePricePerShare(Money.won(10000.0))
			.build();

		PurchaseHistory purchaseHistory2 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(5L))
			.purchasePricePerShare(Money.won(10000.0))
			.build();

		portFolioHolding.addPurchaseHistory(purchaseHistory1);
		portFolioHolding.addPurchaseHistory(purchaseHistory2);

		portfolio.addPortfolioStock(portFolioHolding);

		// when
		RateDivision result = portfolio.calculateTotalGainRate();

		// then
		Money totalGainAmount = Money.won(100000);
		Money totalInvestmentAmount = Money.won(100000);
		RateDivision expected = totalGainAmount.divide(totalInvestmentAmount);
		assertThat(result).isEqualByComparingTo(expected);
	}

	@DisplayName("사용자는 포트폴리오의 파이 차트를 요청한다")
	@Test
	void createPieChart() {
		// given
		Portfolio portfolio = createPortfolio();
		Stock stock = createSamsungStock();
		Stock stock2 = createDongHwa();
		PortfolioHolding holding1 = PortfolioHolding.of(portfolio, stock, Money.won(20000L));
		PortfolioHolding holding2 = PortfolioHolding.of(portfolio, stock2, Money.won(20000L));

		PurchaseHistory purchaseHistory1 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(5L))
			.purchasePricePerShare(Money.won(10000.0))
			.portfolioHolding(holding1)
			.build();

		PurchaseHistory purchaseHistory2 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(5L))
			.purchasePricePerShare(Money.won(20000.0))
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
			.usingComparatorForType(Money::compareTo, Money.class)
			.containsExactlyInAnyOrder(
				Tuple.tuple("현금", Money.won(850000L), Money.zero()),
				Tuple.tuple("삼성전자보통주", Money.won(100000L), Money.won(50000L)),
				Tuple.tuple("동화약품보통주", Money.won(100000L), Money.zero()));
	}

	@DisplayName("사용자는 포트폴리오의 섹터 차트를 요청한다")
	@Test
	void createSectorChart() {
		// given
		Portfolio portfolio = createPortfolio();
		Stock stock = createSamsungStock();
		Stock stock2 = createDongHwa();
		PortfolioHolding holding1 = PortfolioHolding.of(portfolio, stock, Money.won(20000L));
		PortfolioHolding holding2 = PortfolioHolding.of(portfolio, stock2, Money.won(20000L));

		PurchaseHistory purchaseHistory1 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(5L))
			.purchasePricePerShare(Money.won(10000.0))
			.portfolioHolding(holding1)
			.build();

		PurchaseHistory purchaseHistory2 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(5L))
			.purchasePricePerShare(Money.won(20000.0))
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

	private Portfolio createPortfolio() {
		return Portfolio.builder()
			.budget(Money.won(1000000L))
			.targetGain(Money.won(1500000L))
			.maximumLoss(Money.won(900000L))
			.build();
	}

	private Stock createSamsungStock() {
		return Stock.builder()
			.stockCode("KR7005930003")
			.tickerSymbol("005930")
			.companyName("삼성전자보통주")
			.companyNameEng("SamsungElectronics")
			.market(Market.KOSPI)
			.sector("전기전자")
			.build();
	}

	private Stock createDongHwa() {
		return Stock.builder()
			.stockCode("KR7000020008")
			.tickerSymbol("000020")
			.companyName("동화약품보통주")
			.companyNameEng("DongwhaPharm")
			.market(Market.KOSPI)
			.sector("의약품")
			.build();
	}
}
