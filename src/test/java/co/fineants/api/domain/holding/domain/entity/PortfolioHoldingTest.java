package co.fineants.api.domain.holding.domain.entity;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.RateDivision;
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.stock.domain.entity.Stock;

class PortfolioHoldingTest extends AbstractContainerBaseTest {

	@DisplayName("한 종목의 총 투자 금액을 계산한다")
	@Test
	void calculateTotalInvestmentAmount() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding portFolioHolding = PortfolioHolding.of(portfolio, stock, Money.won(10000L));

		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", portFolioHolding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.dollar(10), "첫구매", portFolioHolding);

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
		Stock stock = createSamsungStock();
		PortfolioHolding portFolioHolding = PortfolioHolding.of(portfolio, stock, Money.won(10000L));

		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", portFolioHolding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", portFolioHolding);

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
		Stock stock = createSamsungStock();
		PortfolioHolding portFolioHolding = PortfolioHolding.of(portfolio, stock, Money.won(20000L));

		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", portFolioHolding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", portFolioHolding);

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
		Stock stock = createSamsungStock();
		PortfolioHolding portFolioHolding = PortfolioHolding.of(portfolio, stock, Money.won(20000L));

		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", portFolioHolding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", portFolioHolding);

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
		Stock stock = createSamsungStock();
		List<StockDividend> stockDividends = createStockDividends(stock);
		stockDividends.forEach(stock::addStockDividend);
		Long currentPrice = 60000L;
		PortfolioHolding portfolioHolding = createPortfolioHolding(portfolio, stock, currentPrice);

		PurchaseHistory purchaseHistory = createPurchaseHistory(null, LocalDateTime.of(2023, 3, 1, 9, 30),
			Count.from(3), Money.won(50000), "첫구매", portfolioHolding);
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
			.toList();
		assertThat(moneys)
			.usingComparatorForType(Expression::compareTo, Expression.class)
			.isEqualTo(expected.values());
	}

	private List<StockDividend> createStockDividends(Stock stock) {
		return List.of(
			createStockDividend(
				LocalDate.of(2022, 12, 31),
				LocalDate.of(2023, 4, 14),
				stock),
			createStockDividend(
				LocalDate.of(2023, 3, 31),
				LocalDate.of(2023, 5, 17),
				stock),
			createStockDividend(
				LocalDate.of(2023, 6, 30),
				LocalDate.of(2023, 8, 16),
				stock),
			createStockDividend(
				LocalDate.of(2023, 9, 30),
				LocalDate.of(2023, 11, 20),
				stock)
		);
	}
}
