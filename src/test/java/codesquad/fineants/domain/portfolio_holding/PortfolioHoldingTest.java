package codesquad.fineants.domain.portfolio_holding;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;

@ActiveProfiles("test")
class PortfolioHoldingTest {

	private Portfolio portfolio;

	private Stock stock;

	@BeforeEach
	void init() {
		portfolio = Portfolio.builder()
			.budget(1000000L)
			.targetGain(1500000L)
			.maximumLoss(900000L)
			.build();

		stock = Stock.builder()
			.stockCode("KR7005930003")
			.tickerSymbol("005930")
			.companyName("삼성전자보통주")
			.companyNameEng("SamsungElectronics")
			.market(Market.KOSPI)
			.build();
	}

	@DisplayName("한 종목의 총 투자 금액을 계산한다")
	@Test
	void calculateTotalInvestmentAmount() {
		// given
		PortfolioHolding portFolioHolding = PortfolioHolding.of(portfolio, stock, 10000L);

		PurchaseHistory purchaseHistory1 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(5L)
			.purchasePricePerShare(10000.0)
			.portFolioHolding(portFolioHolding)
			.build();

		PurchaseHistory purchaseHistory2 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(5L)
			.purchasePricePerShare(10000.0)
			.portFolioHolding(portFolioHolding)
			.build();

		portFolioHolding.addPurchaseHistory(purchaseHistory1);
		portFolioHolding.addPurchaseHistory(purchaseHistory2);

		// when
		long result = portFolioHolding.calculateTotalInvestmentAmount();

		// then
		assertThat(result).isEqualTo(100000L);
	}

	@DisplayName("한 종목의 평균 매입가를 계산한다")
	@Test
	void calculateAverageCostPerShare() {
		// given
		PortfolioHolding portFolioHolding = PortfolioHolding.of(portfolio, stock, 10000L);

		PurchaseHistory purchaseHistory1 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(5L)
			.purchasePricePerShare(10000.0)
			.portFolioHolding(portFolioHolding)
			.build();

		PurchaseHistory purchaseHistory2 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(5L)
			.purchasePricePerShare(10000.0)
			.portFolioHolding(portFolioHolding)
			.build();

		portFolioHolding.addPurchaseHistory(purchaseHistory1);
		portFolioHolding.addPurchaseHistory(purchaseHistory2);

		// when
		Double result = portFolioHolding.calculateAverageCostPerShare();

		// then
		assertThat(result).isEqualTo(10000.0);
	}

	@DisplayName("한 종목의 총 손익을 계산한다")
	@Test
	void calculateTotalGain() {
		// given
		PortfolioHolding portFolioHolding = PortfolioHolding.of(portfolio, stock, 20000L);

		PurchaseHistory purchaseHistory1 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(5L)
			.purchasePricePerShare(10000.0)
			.portFolioHolding(portFolioHolding)
			.build();

		PurchaseHistory purchaseHistory2 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(5L)
			.purchasePricePerShare(10000.0)
			.portFolioHolding(portFolioHolding)
			.build();

		portFolioHolding.addPurchaseHistory(purchaseHistory1);
		portFolioHolding.addPurchaseHistory(purchaseHistory2);

		// when
		long result = portFolioHolding.calculateTotalGain();

		// then
		assertThat(result).isEqualTo(100000L);
	}

	@DisplayName("한 종목의 총 손익율 계산한다")
	@Test
	void calculateTotalReturnRate() {
		// given
		PortfolioHolding portFolioHolding = PortfolioHolding.of(portfolio, stock, 20000L);

		PurchaseHistory purchaseHistory1 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(5L)
			.purchasePricePerShare(10000.0)
			.portFolioHolding(portFolioHolding)
			.build();

		PurchaseHistory purchaseHistory2 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(5L)
			.purchasePricePerShare(10000.0)
			.portFolioHolding(portFolioHolding)
			.build();

		portFolioHolding.addPurchaseHistory(purchaseHistory1);
		portFolioHolding.addPurchaseHistory(purchaseHistory2);

		// when
		double result = portFolioHolding.calculateTotalReturnRate();

		// then
		assertThat(result).isEqualTo(100.0);
	}

}
