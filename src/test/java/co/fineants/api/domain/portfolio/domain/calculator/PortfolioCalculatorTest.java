package co.fineants.api.domain.portfolio.domain.calculator;

import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.RateDivision;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.stock.domain.entity.Stock;

class PortfolioCalculatorTest extends AbstractContainerBaseTest {

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

		PortfolioCalculator calculator = new PortfolioCalculator();
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

		PortfolioCalculator calculator = new PortfolioCalculator();
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

		PortfolioCalculator calculator = new PortfolioCalculator();
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

		PortfolioCalculator calculator = new PortfolioCalculator();
		// when
		Expression result = calculator.calCashWeightBy(portfolio);
		// then
		Expression balance = Money.won(880_000L);
		Expression totalAsset = Money.won(1_030_000L);
		Expression expected = RateDivision.of(balance, totalAsset);
		Assertions.assertThat(result).isEqualByComparingTo(expected);
	}
}
