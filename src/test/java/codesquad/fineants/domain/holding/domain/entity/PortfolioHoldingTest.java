package codesquad.fineants.domain.holding.domain.entity;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Bank;
import codesquad.fineants.domain.common.money.Currency;
import codesquad.fineants.domain.common.money.Expression;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.RateDivision;
import codesquad.fineants.domain.dividend.domain.entity.StockDividend;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.purchasehistory.domain.entity.PurchaseHistory;
import codesquad.fineants.domain.stock.domain.entity.Stock;

class PortfolioHoldingTest extends AbstractContainerBaseTest {

	@DisplayName("한 종목의 총 투자 금액을 계산한다")
	@Test
	void calculateTotalInvestmentAmount() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createStock();
		PortfolioHolding portFolioHolding = PortfolioHolding.of(portfolio, stock, Money.won(10000L));

		PurchaseHistory purchaseHistory1 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(5L))
			.purchasePricePerShare(Money.won(10000.0))
			.portfolioHolding(portFolioHolding)
			.build();

		PurchaseHistory purchaseHistory2 = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(5L))
			.purchasePricePerShare(Money.dollar(10))
			.portfolioHolding(portFolioHolding)
			.build();

		portFolioHolding.addPurchaseHistory(purchaseHistory1);
		portFolioHolding.addPurchaseHistory(purchaseHistory2);

		Bank.getInstance().addRate(Currency.KRW, Currency.USD, 1000);
		Bank.getInstance().addRate(Currency.USD, Currency.KRW, (double)1 / 1000);
		// when
		Expression result = portFolioHolding.calculateTotalInvestmentAmount();

		// then
		Money totalInvestmentAmount = Bank.getInstance().reduce(result, Currency.KRW);
		assertThat(totalInvestmentAmount).isEqualByComparingTo(Money.won(100000L));
	}

	@DisplayName("한 종목의 평균 매입가를 계산한다")
	@Test
	void calculateAverageCostPerShare() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createStock();
		PortfolioHolding portFolioHolding = PortfolioHolding.of(portfolio, stock, Money.won(10000L));

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

		// when
		Expression money = portFolioHolding.calculateAverageCostPerShare();

		// then
		assertThat(money).isEqualByComparingTo(Money.won(10000.0));
	}

	@DisplayName("한 종목의 총 손익을 계산한다")
	@Test
	void calculateTotalGain() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createStock();
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

		// when
		Expression result = portFolioHolding.calculateTotalGain();

		// then
		Money won = Bank.getInstance().toWon(result);
		assertThat(won).isEqualByComparingTo(Money.won(100000L));
	}

	@DisplayName("한 종목의 총 손익율 계산한다")
	@Test
	void calculateTotalReturnRate() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createStock();
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

		// when
		RateDivision result = portFolioHolding.calculateTotalReturnRate();

		// then
		Expression totalGain = Money.won(100000);
		Expression totalInvestmentAmount = Money.won(100000);
		RateDivision expected = totalGain.divide(totalInvestmentAmount);
		assertThat(result).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오 종목의 월별 배당금을 계산한다")
	@Test
	void calculateMonthlyDividends() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
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
		Map<Integer, Expression> result = portfolioHolding.createMonthlyDividendMap(LocalDate.of(2023, 12, 15));

		// then
		Map<Integer, Expression> expected = new HashMap<>();
		expected.put(1, Money.zero());
		expected.put(2, Money.zero());
		expected.put(3, Money.zero());
		expected.put(4, Money.zero());
		expected.put(5, Money.won(1083L));
		expected.put(6, Money.zero());
		expected.put(7, Money.zero());
		expected.put(8, Money.won(1083L));
		expected.put(9, Money.zero());
		expected.put(10, Money.zero());
		expected.put(11, Money.won(1083L));
		expected.put(12, Money.zero());
		assertThat(result.keySet())
			.isEqualTo(expected.keySet());

		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		List<Money> moneys = result.values().stream()
			.map(expression -> expression.reduce(bank, to))
			.collect(Collectors.toList());
		assertThat(moneys)
			.usingComparatorForType(Expression::compareTo, Expression.class)
			.isEqualTo(expected.values());
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
			.dividend(Money.won(361L))
			.recordDate(recordDate)
			.exDividendDate(exDividendDate)
			.paymentDate(paymentDate)
			.stock(stock)
			.build();
	}

	private PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock, Long currentPrice) {
		return PortfolioHolding.builder()
			.id(System.currentTimeMillis())
			.portfolio(portfolio)
			.stock(stock)
			.currentPrice(Money.won(currentPrice))
			.build();
	}

	private PurchaseHistory createPurchaseHistory(LocalDateTime purchaseDate, PortfolioHolding portfolioHolding) {
		return PurchaseHistory.builder()
			.id(System.currentTimeMillis())
			.purchaseDate(purchaseDate)
			.purchasePricePerShare(Money.won(50000.0))
			.numShares(Count.from(3L))
			.memo("구매 메모")
			.portfolioHolding(portfolioHolding)
			.build();
	}
}
