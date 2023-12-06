package codesquad.fineants.domain.portfolio_holding;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock_dividend.StockDividend;

@ActiveProfiles("test")
class PortfolioHoldingTest {

	@DisplayName("한 종목의 총 투자 금액을 계산한다")
	@Test
	void calculateTotalInvestmentAmount() {
		// given
		Portfolio portfolio = createPortfolio();
		Stock stock = createStock();
		PortfolioHolding portFolioHolding = PortfolioHolding.of(portfolio, stock, 10000L);

		PurchaseHistory purchaseHistory1 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(5L)
			.purchasePricePerShare(10000.0)
			.portfolioHolding(portFolioHolding)
			.build();

		PurchaseHistory purchaseHistory2 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(5L)
			.purchasePricePerShare(10000.0)
			.portfolioHolding(portFolioHolding)
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
		Portfolio portfolio = createPortfolio();
		Stock stock = createStock();
		PortfolioHolding portFolioHolding = PortfolioHolding.of(portfolio, stock, 10000L);

		PurchaseHistory purchaseHistory1 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(5L)
			.purchasePricePerShare(10000.0)
			.portfolioHolding(portFolioHolding)
			.build();

		PurchaseHistory purchaseHistory2 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(5L)
			.purchasePricePerShare(10000.0)
			.portfolioHolding(portFolioHolding)
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
		Portfolio portfolio = createPortfolio();
		Stock stock = createStock();
		PortfolioHolding portFolioHolding = PortfolioHolding.of(portfolio, stock, 20000L);

		PurchaseHistory purchaseHistory1 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(5L)
			.purchasePricePerShare(10000.0)
			.portfolioHolding(portFolioHolding)
			.build();

		PurchaseHistory purchaseHistory2 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(5L)
			.purchasePricePerShare(10000.0)
			.portfolioHolding(portFolioHolding)
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
		Portfolio portfolio = createPortfolio();
		Stock stock = createStock();
		PortfolioHolding portFolioHolding = PortfolioHolding.of(portfolio, stock, 20000L);

		PurchaseHistory purchaseHistory1 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(5L)
			.purchasePricePerShare(10000.0)
			.portfolioHolding(portFolioHolding)
			.build();

		PurchaseHistory purchaseHistory2 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(5L)
			.purchasePricePerShare(10000.0)
			.portfolioHolding(portFolioHolding)
			.build();

		portFolioHolding.addPurchaseHistory(purchaseHistory1);
		portFolioHolding.addPurchaseHistory(purchaseHistory2);

		// when
		double result = portFolioHolding.calculateTotalReturnRate();

		// then
		assertThat(result).isEqualTo(100.0);
	}

	@DisplayName("포트폴리오 종목의 월별 배당금을 계산한다")
	@Test
	void calculateMonthlyDividends() {
		// given
		Portfolio portfolio = createPortfolio();
		Stock stock = createStock();
		List<StockDividend> stockDividends = createStockDividends(stock);
		stockDividends.forEach(stock::addStockDividend);
		Long currentPrice = 60000L;
		PortfolioHolding portfolioHolding = createPortfolioHolding(portfolio, stock, currentPrice);
		PurchaseHistory purchaseHistory = createPurchaseHistory(
			LocalDateTime.of(2023, 3, 1, 9, 30),
			portfolioHolding
		);
		portfolioHolding.addPurchaseHistory(purchaseHistory);

		// when
		Map<Integer, Long> result = portfolioHolding.createMonthlyDividendMap();

		// then
		Map<Integer, Long> expected = new HashMap<>();
		expected.put(1, 0L);
		expected.put(2, 0L);
		expected.put(3, 0L);
		expected.put(4, 0L);
		expected.put(5, 1083L);
		expected.put(6, 0L);
		expected.put(7, 0L);
		expected.put(8, 1083L);
		expected.put(9, 0L);
		expected.put(10, 0L);
		expected.put(11, 1083L);
		expected.put(12, 0L);
		assertThat(result).isEqualTo(expected);
	}

	private List<StockDividend> createStockDividends(Stock stock) {
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
				stock)
		);
	}

	private StockDividend createStockDividend(LocalDate exDividendDate, LocalDate recordDate, LocalDate paymentDate,
		Stock stock) {
		return StockDividend.builder()
			.id(System.currentTimeMillis())
			.dividend(361L)
			.recordDate(recordDate)
			.exDividendDate(exDividendDate)
			.paymentDate(paymentDate)
			.stock(stock)
			.build();
	}

	private Portfolio createPortfolio() {
		return Portfolio.builder()
			.id(1L)
			.budget(1000000L)
			.targetGain(1500000L)
			.maximumLoss(900000L)
			.build();
	}

	private Stock createStock() {
		return Stock.builder()
			.tickerSymbol("005930")
			.stockCode("KR7005930003")
			.companyName("삼성전자보통주")
			.companyNameEng("SamsungElectronics")
			.market(Market.KOSPI)
			.build();
	}

	private PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock, Long currentPrice) {
		return PortfolioHolding.builder()
			.id(System.currentTimeMillis())
			.portfolio(portfolio)
			.stock(stock)
			.currentPrice(currentPrice)
			.build();
	}

	private PurchaseHistory createPurchaseHistory(LocalDateTime purchaseDate, PortfolioHolding portfolioHolding) {
		return PurchaseHistory.builder()
			.id(System.currentTimeMillis())
			.purchaseDate(purchaseDate)
			.purchasePricePerShare(50000.0)
			.numShares(3L)
			.memo("구매 메모")
			.portfolioHolding(portfolioHolding)
			.build();
	}
}
