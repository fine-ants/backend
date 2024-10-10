package co.fineants.api.domain.portfolio.domain.calculator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.RateDivision;
import co.fineants.api.domain.common.money.Sum;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.kis.repository.CurrentPriceMemoryRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.stock.domain.entity.Stock;

class PortfolioCalculatorTest extends AbstractContainerBaseTest {

	private PortfolioCalculator calculator = new PortfolioCalculator(new CurrentPriceMemoryRepository());

	@DisplayName("포트폴리오 총 손익을 계산한다")
	@Test
	void calTotalGainBy() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock, 50000L);
		PurchaseHistory history = createPurchaseHistory(null, LocalDateTime.now(), Count.from(3), Money.won(40000L),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);
		// when
		Expression result = calculator.calTotalGainBy(portfolio);
		// then
		Assertions.assertThat(result)
			.isEqualByComparingTo(Money.won(30000L));
	}

	@DisplayName("포트폴리오 총 투자금액을 계산한다")
	@Test
	void calTotalInvestmentBy() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock, 50000L);
		PurchaseHistory history = createPurchaseHistory(null, LocalDateTime.now(), Count.from(3), Money.won(40000L),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		// when
		Expression result = calculator.calTotalInvestmentBy(portfolio);
		// then
		Assertions.assertThat(result)
			.isEqualByComparingTo(Money.won(120_000L));
	}

	@DisplayName("포트폴리오 총 손익율을 계산한다")
	@Test
	void calTotalGainRateBy() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock, 50000L);
		PurchaseHistory history = createPurchaseHistory(null, LocalDateTime.now(), Count.from(3), Money.won(40000L),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		// when
		Expression result = calculator.calTotalGainRateBy(portfolio);
		// then
		Expression totalGain = Money.won(30_000L);
		Expression totalInvestment = Money.won(120_000L);
		RateDivision expected = RateDivision.of(totalGain, totalInvestment);
		Assertions.assertThat(result).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오의 현금 비중 계산한다")
	@Test
	void calCashWeightBy() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock, 50000L);
		PurchaseHistory history = createPurchaseHistory(null, LocalDateTime.now(), Count.from(3), Money.won(40000L),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		// when
		Expression result = calculator.calCashWeightBy(portfolio);
		// then
		Expression balance = Money.won(880_000L);
		Expression totalAsset = Money.won(1_030_000L);
		Expression expected = RateDivision.of(balance, totalAsset);
		Assertions.assertThat(result).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오의 월별 전체 배당금 합계를 게산합니다.")
	@Test
	void calTotalDividendBy() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		createStockDividendWith(stock).forEach(stock::addStockDividend);
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock, 50000L);
		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		PurchaseHistory history = createPurchaseHistory(null, purchaseDate, Count.from(3), Money.won(40000L),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		LocalDate currentLocalDate = LocalDate.of(2024, 1, 16);
		// when
		Map<Integer, Expression> actual = calculator.calTotalDividendBy(portfolio, currentLocalDate);
		// then
		Map<Integer, Expression> expected = Map.ofEntries(
			Map.entry(1, Money.zero()),
			Map.entry(2, Money.zero()),
			Map.entry(3, Money.zero()),
			Map.entry(4, Money.won(1083)),
			Map.entry(5, Money.won(1083)),
			Map.entry(6, Money.zero()),
			Map.entry(7, Money.zero()),
			Map.entry(8, Money.won(1083)),
			Map.entry(9, Money.zero()),
			Map.entry(10, Money.zero()),
			Map.entry(11, Money.won(1083)),
			Map.entry(12, Money.zero())
		);
		Bank bank = Bank.getInstance();
		actual.replaceAll((k, v) -> v instanceof Sum ? bank.toWon(v) : v);
		Assertions.assertThat(actual)
			.usingComparatorForType(Expression::compareTo, Expression.class)
			.isEqualTo(expected);
	}
}
