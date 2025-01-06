package co.fineants.api.domain.portfolio.domain.calculator;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.common.money.RateDivision;
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioPieChartItem;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.repository.CurrentPriceMemoryRepository;
import co.fineants.api.domain.kis.repository.PriceRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.global.common.time.LocalDateTimeService;

class PortfolioCalculatorTest extends AbstractContainerBaseTest {

	private PriceRepository currentPriceRepository;
	private PortfolioCalculator calculator;

	@SpyBean
	private LocalDateTimeService localDateTimeService;

	@BeforeEach
	void setUp() {
		currentPriceRepository = new CurrentPriceMemoryRepository();
		calculator = new PortfolioCalculator(currentPriceRepository, localDateTimeService);
		given(localDateTimeService.getLocalDateWithNow())
			.willReturn(LocalDate.of(2024, 5, 1));
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
		assertThat(result).isEqualByComparingTo(Money.won(30000L));
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
		Throwable throwable = catchThrowable(() -> calculator.calTotalGainBy(portfolio));
		// then
		assertThat(throwable)
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
		assertThat(result).isEqualByComparingTo(expected);
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
		assertThat(result).isEqualByComparingTo(expected);
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
		assertThat(result)
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
		assertThat(result).isEqualByComparingTo(expected);
	}

	@DisplayName("한 종목의 총 투자 금액을 계산한다")
	@Test
	void calculateTotalInvestmentAmount() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = PortfolioHolding.of(portfolio, stock);

		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);

		holding.addPurchaseHistory(purchaseHistory1);
		holding.addPurchaseHistory(purchaseHistory2);
		// when
		Expression actual = calculator.calTotalInvestmentBy(holding);

		// then
		Money expected = Money.won(100000);
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오 종목의 월별 배당금을 계산한다")
	@Test
	void calculateMonthlyDividends() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		List<StockDividend> stockDividends = createStockDividendWith(stock);
		stockDividends.forEach(stock::addStockDividend);
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);

		PurchaseHistory purchaseHistory = createPurchaseHistory(null, LocalDateTime.of(2023, 3, 1, 9, 30),
			Count.from(3), Money.won(50000), "첫구매", holding);
		holding.addPurchaseHistory(purchaseHistory);
		LocalDate currentLocalDate = LocalDate.of(2023, 12, 15);
		// when
		Map<Month, Expression> actual = calculator.calMonthlyDividendMapBy(holding, currentLocalDate);

		// then
		Map<Month, Expression> expected = new EnumMap<>(Month.class);
		for (Month month : Month.values()) {
			expected.put(month, Money.zero());
		}
		expected.put(Month.MAY, Money.won(1083L));
		expected.put(Month.AUGUST, Money.won(1083L));
		expected.put(Month.NOVEMBER, Money.won(1083L));

		assertThat(actual.values())
			.usingElementComparator(Expression::compareTo)
			.containsExactlyElementsOf(expected.values());
	}

	@DisplayName("포트폴리오의 잔고를 계산한다")
	@Test
	void calBalanceBy() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = PortfolioHolding.of(portfolio, stock);

		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);

		holding.addPurchaseHistory(purchaseHistory1);
		holding.addPurchaseHistory(purchaseHistory2);
		portfolio.addHolding(holding);
		// when
		Expression actual = calculator.calBalanceBy(portfolio);

		// then
		Money expected = Money.won(900_000);
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오 종목들의 총 손익 계산한다")
	@Test
	void calTotalGainBy_givenHoldings() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		currentPriceRepository.savePrice(stock, 50_000L);
		PortfolioHolding holding = PortfolioHolding.of(portfolio, stock);

		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);

		holding.addPurchaseHistory(purchaseHistory1);
		holding.addPurchaseHistory(purchaseHistory2);
		portfolio.addHolding(holding);
		// when
		Expression actual = calculator.calTotalGainBy(holding);

		// then
		Money expected = Money.won(400_000);
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오 종목들의 총 투자금액 계산한다")
	@Test
	void calTotalInvestmentOfHolding() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		currentPriceRepository.savePrice(stock, 50_000L);
		PortfolioHolding holding = PortfolioHolding.of(portfolio, stock);

		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);

		holding.addPurchaseHistory(purchaseHistory1);
		holding.addPurchaseHistory(purchaseHistory2);
		portfolio.addHolding(holding);
		// when
		Expression actual = calculator.calTotalInvestmentOfHolding(List.of(holding));

		// then
		Money expected = Money.won(100_000);
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오 종목들의 총 평가 금액 합계를 계산한다")
	@Test
	void calTotalCurrentValuation_givenHoldingList_whenCalTotalCurrentValuation_thenReturnSumOfAllHoldings() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		currentPriceRepository.savePrice(stock, 50_000L);
		PortfolioHolding holding = PortfolioHolding.of(portfolio, stock);

		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);

		holding.addPurchaseHistory(purchaseHistory1);
		holding.addPurchaseHistory(purchaseHistory2);
		portfolio.addHolding(holding);
		// when
		Expression actual = calculator.calTotalCurrentValuation(List.of(holding));

		// then
		Money expected = Money.won(500_000);
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("단일 포트폴리오 종목의 총 평가금액을 계산한다")
	@Test
	void calTotalCurrentValuationBy_whenCalTotalCurrentValuationByHolding_thenReturnSumOfHolding() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		currentPriceRepository.savePrice(stock, 50_000L);
		PortfolioHolding holding = PortfolioHolding.of(portfolio, stock);

		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);

		holding.addPurchaseHistory(purchaseHistory1);
		holding.addPurchaseHistory(purchaseHistory2);
		portfolio.addHolding(holding);
		// when
		Expression actual = calculator.calTotalCurrentValuationBy(holding);

		// then
		Money expected = Money.won(500_000);
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오의 총 자산을 계산한다")
	@Test
	void calTotalAssetBy_givenPortfolio_whenCalTotalAssetBy_thenReturnSumOfPortfolio() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		currentPriceRepository.savePrice(stock, 50_000L);
		PortfolioHolding holding = PortfolioHolding.of(portfolio, stock);

		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);

		holding.addPurchaseHistory(purchaseHistory1);
		holding.addPurchaseHistory(purchaseHistory2);
		portfolio.addHolding(holding);
		// when
		Expression actual = calculator.calTotalAssetBy(portfolio);

		// then
		Money expected = Money.won(1_400_000);
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오의 당일 손익을 계산한다")
	@Test
	void calDailyGain_givenPortfolio_whenCalDailyGain_thenReturnSumOfPortfolio() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		currentPriceRepository.savePrice(stock, 50_000L);
		PortfolioHolding holding = PortfolioHolding.of(portfolio, stock);

		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);

		holding.addPurchaseHistory(purchaseHistory1);
		holding.addPurchaseHistory(purchaseHistory2);
		portfolio.addHolding(holding);

		PortfolioGainHistory history = PortfolioGainHistory.empty(portfolio);
		// when
		Expression actual = calculator.calDailyGain(history, portfolio);
		// then
		Money expected = Money.won(400_000);
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오의 당일 손익율을 계산한다")
	@Test
	void calDailyGainRateBy() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		currentPriceRepository.savePrice(stock, 50_000L);
		PortfolioHolding holding = PortfolioHolding.of(portfolio, stock);

		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);

		holding.addPurchaseHistory(purchaseHistory1);
		holding.addPurchaseHistory(purchaseHistory2);
		portfolio.addHolding(holding);

		PortfolioGainHistory history = PortfolioGainHistory.empty(portfolio);
		// when
		Expression actual = calculator.calDailyGainRateBy(history, portfolio);
		// then
		Expression expected = RateDivision.of(Money.won(400_000), Money.won(100_000));
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오의 이번달 배당금을 계산 후 반환한다")
	@Test
	void calCurrentMonthDividendBy() {
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		createStockDividendWith(stock).forEach(stock::addStockDividend);
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		PurchaseHistory history = createPurchaseHistory(null, LocalDate.of(2024, 3, 28).atStartOfDay(), Count.from(3),
			Money.won(40000L),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		// when
		Expression actual = calculator.calCurrentMonthDividendBy(portfolio);
		// then
		Expression expected = Money.won(1083);
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오 종목들의 이번달의 월간 배당금 합계를 계산한다")
	@Test
	void calCurrentMonthDividendBy_givenHoldings_whenCalCurrentMonthDividend_thenReturnSumOfHoldings() {
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		createStockDividendWith(stock).forEach(stock::addStockDividend);
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		PurchaseHistory history = createPurchaseHistory(null, LocalDate.of(2024, 3, 28).atStartOfDay(), Count.from(3),
			Money.won(40000L),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);
		// when
		Expression actual = calculator.calCurrentMonthDividendBy(List.of(holding));
		// then
		Expression expected = Money.won(1083);
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("종목과 매입이력이 주어질때 이번달 배당금 합계를 계산한다")
	@Test
	void givenStockAndPurchaseHistories_whenCalCurrentMonthExpectDividend_thenReturnSumOfDividend() {
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		createStockDividendWith(stock).forEach(stock::addStockDividend);
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		PurchaseHistory history = createPurchaseHistory(null, LocalDate.of(2024, 3, 28).atStartOfDay(), Count.from(3),
			Money.won(40000L),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		// when
		Expression actual = calculator.calCurrentMonthExpectedDividend(stock, List.of(history));
		// then
		Expression expected = Money.won(1083);
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오의 연간 배당금을 계산한다")
	@Test
	void calAnnualDividendBy_givenPortfolio_whenCalAnnualDividend_thenReturnSumOfAnnualDividend() {
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		createStockDividendWith(stock).forEach(stock::addStockDividend);
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		PurchaseHistory history = createPurchaseHistory(null, LocalDate.of(2024, 3, 28).atStartOfDay(), Count.from(3),
			Money.won(40000L),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);
		// when
		Expression actual = calculator.calAnnualDividendBy(localDateTimeService, portfolio);
		// then
		Expression expected = Money.won(4_332);
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오의 연간 배당율을 계산한다")
	@Test
	void calAnnualDividendYieldBy_givenPortfolio_whenCalAnnualDividendYield_thenReturnPercentOfAnnualDividend() {
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		createStockDividendWith(stock).forEach(stock::addStockDividend);
		currentPriceRepository.savePrice(stock, 50_000);
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		PurchaseHistory history = createPurchaseHistory(null, LocalDate.of(2024, 3, 28).atStartOfDay(), Count.from(3),
			Money.won(40_000),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);
		// when
		Expression actual = calculator.calAnnualDividendYieldBy(localDateTimeService, portfolio);
		// then
		Expression expected = RateDivision.of(Money.won(4_332), Money.won(150_000));
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오의 투자대비 연간배당율을 계산한다")
	@Test
	void calAnnualInvestmentDividendYieldBy() {
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		createStockDividendWith(stock).forEach(stock::addStockDividend);
		currentPriceRepository.savePrice(stock, 50_000);
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		PurchaseHistory history = createPurchaseHistory(null, LocalDate.of(2024, 3, 28).atStartOfDay(), Count.from(3),
			Money.won(40_000),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);
		// when
		Expression actual = calculator.calAnnualInvestmentDividendYieldBy(localDateTimeService, portfolio);
		// then
		Expression expected = RateDivision.of(Money.won(4_332), Money.won(120_000));
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("연간 배당금과 총 투자금액을 이용하여 투자대비 연간 배당율을 계산한다")
	@Test
	void calAnnualInvestmentDividendYield() {
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		createStockDividendWith(stock).forEach(stock::addStockDividend);
		currentPriceRepository.savePrice(stock, 50_000);
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		PurchaseHistory history = createPurchaseHistory(null, LocalDate.of(2024, 3, 28).atStartOfDay(), Count.from(3),
			Money.won(40_000),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		Expression annualDividend = calculator.calAnnualDividendBy(localDateTimeService, portfolio);
		Expression totalInvestment = calculator.calTotalInvestmentBy(portfolio);
		// when
		Expression actual = calculator.calAnnualInvestmentDividendYield(annualDividend, totalInvestment);
		// then
		Expression expected = RateDivision.of(Money.won(4_332), Money.won(120_000));
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오의 최대손실비율을 계산한다")
	@Test
	void calMaximumLossRateBy_givenPortfolio_whenCalMaximumLossRate_thenReturnPercentOfMaximumLoss() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		// when
		Expression actual = calculator.calMaximumLossRateBy(portfolio);
		// then
		Expression expected = RateDivision.of(Money.won(100_000), Money.won(1_000_000));
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("예산과 최대손실금액이 주어지고 최대 손실비율을 계산한다")
	@Test
	void calMaximumLossRate_givenBudgetAndMaximumLoss_whenCalMaximumLossRate_thenReturnPercentageOfMaximumLoss() {
		// given
		Money budget = Money.won(1_000_000);
		Money maximumLoss = Money.won(900_000);
		// when
		Expression actual = calculator.calMaximumLossRate(budget, maximumLoss);
		// then
		Expression expected = RateDivision.of(Money.won(100_000), Money.won(1_000_000));
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오의 목표수익율을 계산한다")
	@Test
	void calTargetGainRateBy_givenPortfolio_whenCalTargetGainRate_thenReturnPercentageOfTargetGain() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		// when
		Expression actual = calculator.calTargetGainRateBy(portfolio);
		// then
		Expression expected = RateDivision.of(Money.won(500_000), Money.won(1_000_000));
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("예산과 목표수익금액이 주어지고 목표수익율을 계산한다")
	@Test
	void calTargetGainRate_givenBudgetAndTargetGain_whenCalTargetGainRate_thenReturnPercentageOfTargetGain() {
		// given
		Expression budget = Money.won(1_000_000);
		Expression targetGain = Money.won(1_500_000);
		// when
		Expression actual = calculator.calTargetGainRate(budget, targetGain);
		// then
		Expression expected = RateDivision.of(Money.won(500_000), Money.won(1_000_000));
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오의 현금 비중 계산한다")
	@Test
	void calCashWeightBy_givenPortfolio_whenCalCashWeight_thenReturnPercentageOfCash() {
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
		assertThat(result).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오 종목의 평가 금액 비중을 계산한다")
	@Test
	void calCurrentValuationWeightBy_givenHolding_whenCalCurrentValuationWeight_thenReturnPercentageOfHolding() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		currentPriceRepository.savePrice(stock, 50_000L);
		PortfolioHolding holding = PortfolioHolding.of(portfolio, stock);

		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);

		holding.addPurchaseHistory(purchaseHistory1);
		holding.addPurchaseHistory(purchaseHistory2);
		portfolio.addHolding(holding);

		Expression totalAsset = calculator.calTotalAssetBy(portfolio);
		// when
		Expression actual = calculator.calCurrentValuationWeightBy(holding, totalAsset);
		// then
		Expression expected = RateDivision.of(Money.won(500_000), Money.won(1_400_000));
		assertThat(actual).isEqualByComparingTo(expected);
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

	@DisplayName("포트폴리오 종목들의 섹터 차트를 계산한다")
	@Test
	void calSectorChart() {
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

		currentPriceRepository.savePrice(stock, 20_000);
		currentPriceRepository.savePrice(stock2, 20_000);

		Expression balance = calculator.calBalanceBy(portfolio);
		// when
		Map<String, List<Expression>> result = calculator.calSectorChart(List.of(holding1, holding2), balance);

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

	@DisplayName("총 자산 대비 평가금액 비중을 계산한다")
	@Test
	void calCurrentValuationWeight() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		currentPriceRepository.savePrice(stock, 50_000);
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);

		holding.addPurchaseHistory(purchaseHistory1);
		holding.addPurchaseHistory(purchaseHistory2);
		portfolio.addHolding(holding);

		Expression currentValuation = calculator.calTotalCurrentValuationBy(portfolio);
		Expression totalAsset = calculator.calTotalAssetBy(portfolio);
		// when
		Expression actual = calculator.calCurrentValuationWeight(currentValuation, totalAsset);
		// then
		Expression expected = RateDivision.of(Money.won(500_000), Money.won(1_400_000));
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오가 목표수익금액에 도달했는지 검사한다")
	@Test
	void reachedTargetGainBy() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		currentPriceRepository.savePrice(stock, 150_000);
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);

		holding.addPurchaseHistory(purchaseHistory1);
		holding.addPurchaseHistory(purchaseHistory2);
		portfolio.addHolding(holding);
		// when
		boolean actual = calculator.reachedTargetGainBy(portfolio);
		// then
		assertThat(actual).isTrue();
	}

	@DisplayName("포트폴리오가 최대손실금액에 도달했는지 검사한다")
	@Test
	void reachedMaximumLossBy() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		currentPriceRepository.savePrice(stock, 0);
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);

		holding.addPurchaseHistory(purchaseHistory1);
		holding.addPurchaseHistory(purchaseHistory2);
		portfolio.addHolding(holding);
		// when
		boolean actual = calculator.reachedMaximumLossBy(portfolio);
		// then
		assertThat(actual).isTrue();
	}

	@DisplayName("포트폴리오의 파이 차트를 계산한다")
	@Test
	void calPieChartItemBy_givenPortfolio_whenCalPieChartItem_thenReturnPieChartItemList() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		currentPriceRepository.savePrice(stock, 50_000);
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);

		holding.addPurchaseHistory(purchaseHistory1);
		holding.addPurchaseHistory(purchaseHistory2);
		portfolio.addHolding(holding);
		// when
		List<PortfolioPieChartItem> actual = calculator.calPieChartItemBy(portfolio);
		// then
		Money expectedBalance = Money.won(900_000);
		Money expectedTotalAsset = Money.won(1_400_000);
		Percentage weight = RateDivision.of(expectedBalance, expectedTotalAsset)
			.toPercentage(Bank.getInstance(), Currency.KRW);
		PortfolioPieChartItem cash = PortfolioPieChartItem.cash(weight, expectedBalance);

		Money expectedCurrentValuation = Money.won(500_000);
		Percentage expectedWeight = RateDivision.of(Money.won(500_000), Money.won(1_400_000))
			.toPercentage(Bank.getInstance(), Currency.KRW);
		Money expectedTotalGain = Money.won(400_000);
		Percentage totalGainRate = RateDivision.of(Money.won(400_000), Money.won(100_000))
			.toPercentage(Bank.getInstance(), Currency.KRW);
		PortfolioPieChartItem stockPieChartItem = PortfolioPieChartItem.stock(stock.getCompanyName(),
			expectedCurrentValuation, expectedWeight, expectedTotalGain, totalGainRate);
		List<PortfolioPieChartItem> expected = List.of(cash, stockPieChartItem);
		assertThat(actual)
			.hasSize(2)
			.usingComparatorForType(Expression::compareTo, Expression.class)
			.containsExactlyElementsOf(expected);
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
		Map<Month, Expression> actual = calculator.calTotalDividendBy(portfolio, currentLocalDate);
		// then
		Map<Month, Expression> expected = new EnumMap<>(Month.class);
		for (Month month : Month.values()) {
			expected.put(month, Money.zero());
		}
		expected.put(Month.APRIL, Money.won(1083L));
		expected.put(Month.MAY, Money.won(1083L));
		expected.put(Month.AUGUST, Money.won(1083L));
		expected.put(Month.NOVEMBER, Money.won(1083L));

		Expression[] actualArray = actual.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.map(Map.Entry::getValue)
			.toArray(Expression[]::new);
		Expression[] expectedArray = expected.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.map(Map.Entry::getValue)
			.toArray(Expression[]::new);

		for (int i = 0; i < actualArray.length; i++) {
			assertThat(actualArray[i]).isEqualByComparingTo(expectedArray[i]);
		}
	}

	@DisplayName("포트폴리오 종목들의 월별 배당금 합계를 계산한다")
	@Test
	void calTotalDividend() {
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
		Map<Month, Expression> actual = calculator.calTotalDividend(List.of(holding), currentLocalDate);
		// then
		Map<Month, Expression> expected = new EnumMap<>(Month.class);
		for (Month month : Month.values()) {
			expected.put(month, Money.zero());
		}
		expected.put(Month.APRIL, Money.won(1083L));
		expected.put(Month.MAY, Money.won(1083L));
		expected.put(Month.AUGUST, Money.won(1083L));
		expected.put(Month.NOVEMBER, Money.won(1083L));

		assertThat(actual)
			.usingRecursiveComparison().comparingOnlyFieldsOfTypes(Expression.class)
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
		assertThat(result).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오 종목의 손익율을 계산한다")
	@Test
	void calTotalGainPercentage() {
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
		Percentage actual = calculator.calTotalGainPercentage(holding);
		// then
		Percentage expected = Percentage.from(0.25);
		assertThat(actual).isEqualByComparingTo(expected);
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
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오 종목의 당일 손익 변동율을 계산한다")
	@Test
	void calDailyChangeRate() {
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
		Expression actual = calculator.calDailyChangeRate(holding, closingPrice);
		// then
		Expression expected = RateDivision.of(Money.won(10_000L), closingPrice);
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오 종목의 현재가를 가져온다")
	@Test
	void fetchCurrentPrice() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		PurchaseHistory history = createPurchaseHistory(null, LocalDateTime.now(), Count.from(3), Money.won(40000L),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		currentPriceRepository.savePrice(stock, 50_000L);
		// when
		Expression actual = calculator.fetchCurrentPrice(holding);
		// then
		Expression expected = Money.won(50_000);
		assertThat(actual).isEqualByComparingTo(expected);
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
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("한 종목의 평균 매입가를 계산한다")
	@Test
	void calculateAverageCostPerShare() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = PortfolioHolding.of(portfolio, stock);

		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);

		holding.addPurchaseHistory(purchaseHistory1);
		holding.addPurchaseHistory(purchaseHistory2);

		// when
		Expression actual = calculator.calAverageCostPerShareBy(holding);

		// then
		Expression expected = Money.won(10000);
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("매입 이력들의 평균 매입가를 계산한다")
	@Test
	void calAverageCostPerShare() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = PortfolioHolding.of(portfolio, stock);

		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);

		holding.addPurchaseHistory(purchaseHistory1);
		holding.addPurchaseHistory(purchaseHistory2);

		// when
		Expression actual = calculator.calAverageCostPerShare(List.of(purchaseHistory1, purchaseHistory2));

		// then
		Expression expected = Money.won(10000);
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("매입 이력들의 주식 개수 합계를 계산한다")
	@Test
	void calNumShares_givenPurchaseHistories_whenCalNumShares_thenReturnSumOfNumShares() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = PortfolioHolding.of(portfolio, stock);

		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);

		holding.addPurchaseHistory(purchaseHistory1);
		holding.addPurchaseHistory(purchaseHistory2);
		// when
		Count actual = calculator.calNumShares(List.of(purchaseHistory1, purchaseHistory2));
		// then
		Count expected = Count.from(10);
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오 종목의 주식 개수를 계산한다")
	@Test
	void calNumSharesBy() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = PortfolioHolding.of(portfolio, stock);

		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);
		holding.addPurchaseHistory(purchaseHistory1);
		holding.addPurchaseHistory(purchaseHistory2);

		// when
		Count actual = calculator.calNumSharesBy(holding);

		// then
		Count expected = Count.from(10);
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("매입 이력들의 총 투자 금액을 계산한다")
	@Test
	void calTotalInvestment_givenPurchaseHistories_whenCalTotalInvestment_thenReturnSumOfPurchaseHistories() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = PortfolioHolding.of(portfolio, stock);

		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, LocalDateTime.now(), Count.from(5),
			Money.won(10000), "첫구매", holding);
		holding.addPurchaseHistory(purchaseHistory1);
		holding.addPurchaseHistory(purchaseHistory2);

		// when
		Expression actual = calculator.calTotalInvestment(List.of(purchaseHistory1, purchaseHistory2));

		// then
		Expression expected = Money.won(100_000);
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("종목과 매입이력들이 주어졌을때 연간 예상되는 배당금 합계를 계산한다")
	@Test
	void calAnnualExpectedDividend_givenStockAndPurchaseHistories_whenCalAnnualExpectedDividend_thenReturnSum() {
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
		Expression actual = calculator.calAnnualExpectedDividend(stock, List.of(history), localDateTimeService);
		// then
		Expression expected = Money.won(1083);
		assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("포트폴리오 종목의 월별 배당금 계산한다")
	@Test
	void calMonthlyDividendMapBy() {
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		long currentPrice = 50_000L;
		currentPriceRepository.savePrice(stock, currentPrice);
		createStockDividendWith(stock).forEach(stock::addStockDividend);
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		PurchaseHistory history = createPurchaseHistory(null, purchaseDate, Count.from(3), Money.won(40000L),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		LocalDate currentLocalDate = LocalDate.of(2024, 1, 16);
		// when
		Map<Month, Expression> actual = calculator.calMonthlyDividendMapBy(holding, currentLocalDate);
		// then
		Map<Month, Expression> expected = new EnumMap<>(Month.class);
		for (Month month : Month.values()) {
			expected.put(month, Money.zero());
		}
		expected.put(Month.APRIL, Money.won(1083));
		expected.put(Month.MAY, Money.won(1083));
		expected.put(Month.AUGUST, Money.won(1083));
		expected.put(Month.NOVEMBER, Money.won(1083));

		Expression[] actualArray = actual.values().toArray(Expression[]::new);
		Expression[] expectedArray = expected.values().toArray(Expression[]::new);

		for (int i = 0; i < actualArray.length; i++) {
			assertThat(actualArray[i]).isEqualByComparingTo(expectedArray[i]);
		}
	}

	@DisplayName("종목과 매입 이력들의 월별 배당금 합계를 계산한다")
	@Test
	void calMonthlyDividendMap() {
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		long currentPrice = 50_000L;
		currentPriceRepository.savePrice(stock, currentPrice);
		createStockDividendWith(stock).forEach(stock::addStockDividend);
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		PurchaseHistory history = createPurchaseHistory(null, purchaseDate, Count.from(3), Money.won(40000L),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		LocalDate currentLocalDate = LocalDate.of(2024, 1, 16);
		// when
		Map<Month, Expression> actual = calculator.calMonthlyDividendMap(stock, List.of(history), currentLocalDate);
		// then
		Map<Month, Expression> expected = new EnumMap<>(Month.class);
		for (Month month : Month.values()) {
			expected.put(month, Money.zero());
		}
		expected.put(Month.APRIL, Money.won(1083));
		expected.put(Month.MAY, Money.won(1083));
		expected.put(Month.AUGUST, Money.won(1083));
		expected.put(Month.NOVEMBER, Money.won(1083));

		Expression[] actualArray = actual.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.map(Map.Entry::getValue)
			.toArray(Expression[]::new);
		Expression[] expectedArray = expected.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.map(Map.Entry::getValue)
			.toArray(Expression[]::new);

		for (int i = 0; i < actualArray.length; i++) {
			assertThat(actualArray[i]).isEqualByComparingTo(expectedArray[i]);
		}
	}

	@DisplayName("포트폴리오 종목과 총 자산이 주어졌을때 하나의 파이 차트 요소를 계산한다")
	@Test
	void calPortfolioPieChartItemBy() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		long currentPrice = 50_000L;
		currentPriceRepository.savePrice(stock, currentPrice);
		createStockDividendWith(stock).forEach(stock::addStockDividend);
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		PurchaseHistory history = createPurchaseHistory(null, purchaseDate, Count.from(3), Money.won(40000L),
			"메모", holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		Expression totalAsset = calculator.calTotalAssetBy(portfolio);
		// when
		PortfolioPieChartItem actual = calculator.calPortfolioPieChartItemBy(holding, totalAsset);
		// then
		PortfolioPieChartItem expected = PortfolioPieChartItem.stock(
			stock.getCompanyName(),
			Money.won(150_000),
			RateDivision.of(Money.won(150_000), Money.won(1_030_000)).toPercentage(Bank.getInstance(), Currency.KRW),
			Money.won(30_000),
			RateDivision.of(Money.won(30_000), Money.won(120_000)).toPercentage(Bank.getInstance(), Currency.KRW));
		assertThat(actual).isEqualTo(expected);
	}
}
