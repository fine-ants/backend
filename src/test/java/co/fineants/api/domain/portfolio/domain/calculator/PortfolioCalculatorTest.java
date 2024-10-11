package co.fineants.api.domain.portfolio.domain.calculator;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.common.money.RateDivision;
import co.fineants.api.domain.common.money.Sum;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.repository.CurrentPriceMemoryRepository;
import co.fineants.api.domain.kis.repository.PriceRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.stock.domain.entity.Stock;

class PortfolioCalculatorTest extends AbstractContainerBaseTest {

	private PriceRepository currentPriceRepository;
	private PortfolioCalculator calculator;

	@BeforeEach
	void setUp() {
		currentPriceRepository = new CurrentPriceMemoryRepository();
		calculator = new PortfolioCalculator(currentPriceRepository);
	}

	@DisplayName("포트폴리오 총 손익을 계산한다")
	@Test
	void calTotalGainBy() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		PurchaseHistory history = createPurchaseHistory(null, LocalDateTime.now(), Count.from(3), Money.won(40000L),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		currentPriceRepository.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 50000L));
		// when
		Expression result = calculator.calTotalGainBy(portfolio);
		// then
		Assertions.assertThat(result).isEqualByComparingTo(Money.won(30000L));
	}

	@DisplayName("단 한개의 포트폴리오 종목이라도 현재가를 가져오지 못하면 포트폴리오 총 손익을 계산하지 못한다")
	@Test
	void calTotalGainBy_givenNoCurrentPrice_whenCalTotalGain_thenThrowException() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		PurchaseHistory history = createPurchaseHistory(null, LocalDateTime.now(), Count.from(3), Money.won(40000L),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);
		// when
		Throwable throwable = Assertions.catchThrowable(() -> calculator.calTotalGainBy(portfolio));
		// then
		Assertions.assertThat(throwable)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage(String.format("Failed to calculate total gain for portfolio, portfolio:%s", portfolio));
	}

	@DisplayName("포트폴리오의 총 평가 금액을 계산한다")
	@Test
	void calTotalCurrentValuation() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		PurchaseHistory history = createPurchaseHistory(null, LocalDateTime.now(), Count.from(3), Money.won(40000L),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		long currentPrice = 50_000L;
		currentPriceRepository.savePrice(stock, currentPrice);
		// when
		Expression result = calculator.calTotalCurrentValuationBy(portfolio);
		// then
		Expression expected = Money.won(150_000L);
		Assertions.assertThat(result).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오 종목의 평가 금액 비중을 계산한다")
	@Test
	void calCurrentValuationWeightBy() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		PurchaseHistory history = createPurchaseHistory(null, LocalDateTime.now(), Count.from(3), Money.won(40000L),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		long currentPrice = 50_000L;
		currentPriceRepository.savePrice(stock, currentPrice);
		Expression totalAsset = calculator.calTotalAssetBy(portfolio);
		// when
		Expression result = calculator.calCurrentValuationWeightBy(holding, totalAsset);
		// then
		Expression expected = RateDivision.of(Money.won(150_000L), Money.won(1_030_000L));
		Assertions.assertThat(result).isEqualByComparingTo(expected);
	}

	@DisplayName("사용자는 포트폴리오의 섹터 차트를 요청한다")
	@Test
	void calSectorChartBy() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		Stock stock2 = createDongwhaPharmStock();
		PortfolioHolding holding1 = PortfolioHolding.of(portfolio, stock);
		PortfolioHolding holding2 = PortfolioHolding.of(portfolio, stock2);

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(5);
		Money purchasePerShare = Money.won(10000);
		String memo = "첫구매";
		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo,
			holding1);

		numShares = Count.from(5);
		purchasePerShare = Money.won(20000);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo,
			holding2);

		holding1.addPurchaseHistory(purchaseHistory1);
		holding2.addPurchaseHistory(purchaseHistory2);

		portfolio.addHolding(holding1);
		portfolio.addHolding(holding2);

		currentPriceRepository.savePrice(stock, 20_000L);
		currentPriceRepository.savePrice(stock2, 20_000L);
		// when
		Map<String, List<Expression>> result = calculator.calSectorChartBy(portfolio);

		// then
		Map<String, List<Expression>> expected = Map.of(
			"현금", List.of(Money.won(850_000)),
			"의약품", List.of(Money.won(100_000)),
			"전기전자", List.of(Money.won(100_000))
		);
		Bank bank = Bank.getInstance();
		Map<String, List<Money>> actual = result.entrySet().stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> entry.getValue().stream()
					.map(bank::toWon)
					.toList())
			);
		assertThat(actual)
			.usingComparatorForType(Expression::compareTo, Expression.class)
			.isEqualTo(expected);
	}

	@DisplayName("포트폴리오 종목의 예상되는 연간 배당금율을 계산한다")
	@Test
	void calAnnualExpectedDividendYieldBy() {
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		long currentPrice = 50_000L;
		currentPriceRepository.savePrice(stock, currentPrice);
		createStockDividendWith(stock).forEach(stock::addStockDividend);
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		PurchaseHistory history = createPurchaseHistory(null, LocalDateTime.now(), Count.from(3), Money.won(40000L),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		// when
		Expression result = calculator.calAnnualExpectedDividendYieldBy(holding);
		// then
		RateDivision expected = RateDivision.of(Money.won(1083), Money.won(150_000));
		Assertions.assertThat(result).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오 종목의 손익율을 계산한다")
	@Test
	void calTotalGainRate() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		PurchaseHistory history = createPurchaseHistory(null, LocalDateTime.now(), Count.from(3), Money.won(40000L),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		long currentPrice = 50_000L;
		currentPriceRepository.savePrice(stock, currentPrice);
		// when
		Percentage actual = calculator.calTotalReturnPercentage(holding);
		// then
		Percentage expected = Percentage.from(0.25);
		Assertions.assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오 총 투자금액을 계산한다")
	@Test
	void calTotalInvestmentBy() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
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
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		PurchaseHistory history = createPurchaseHistory(null, LocalDateTime.now(), Count.from(3), Money.won(40000L),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		currentPriceRepository.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 50000L));
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
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		PurchaseHistory history = createPurchaseHistory(null, LocalDateTime.now(), Count.from(3), Money.won(40000L),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		currentPriceRepository.savePrice(stock, 50000L);
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
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
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

	@DisplayName("포트폴리오 종목의 당일 변동 금액을 계산한다")
	@Test
	void calDailyChange() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		PurchaseHistory history = createPurchaseHistory(null, LocalDateTime.now(), Count.from(3), Money.won(40000L),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		currentPriceRepository.savePrice(stock, 50_000L);
		Expression closingPrice = Money.won(40_000L);
		// when
		Expression actual = calculator.calDailyChange(holding, closingPrice);
		// then
		Expression expected = Money.won(10_000L);
		Assertions.assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오 종목의 예상 연간 배당금을 계산한다")
	@Test
	void calAnnualExpectedDividendBy() {
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		long currentPrice = 50_000L;
		currentPriceRepository.savePrice(stock, currentPrice);
		createStockDividendWith(stock).forEach(stock::addStockDividend);
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		PurchaseHistory history = createPurchaseHistory(null, LocalDateTime.now(), Count.from(3), Money.won(40000L),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);
		// when
		Expression actual = calculator.calAnnualExpectedDividendBy(holding);
		// then
		Expression expected = Money.won(1083);
		Assertions.assertThat(actual).isEqualByComparingTo(expected);
	}
}
