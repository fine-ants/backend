package co.fineants.api.domain.holding.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.hibernate.annotations.BatchSize;
import org.jetbrains.annotations.NotNull;

import co.fineants.api.domain.BaseEntity;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.common.money.RateDivision;
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioPieChartItem;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.PriceRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.stock.domain.entity.Stock;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Entity
@EqualsAndHashCode(of = {"portfolio", "stock"}, callSuper = false)
public class PortfolioHolding extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "portfolio_id")
	private Portfolio portfolio;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ticker_symbol")
	private Stock stock;

	@BatchSize(size = 1000)
	@OneToMany(mappedBy = "portfolioHolding", fetch = FetchType.LAZY)
	private List<PurchaseHistory> purchaseHistory = new ArrayList<>();

	@Transient
	private Money currentPrice;    // 현재가

	protected PortfolioHolding() {
		this.purchaseHistory = new ArrayList<>();
	}

	private PortfolioHolding(LocalDateTime createAt, LocalDateTime modifiedAt, Long id,
		Portfolio portfolio, Stock stock, Money currentPrice) {
		super(createAt, modifiedAt);
		this.id = id;
		this.portfolio = portfolio;
		this.stock = stock;
		this.currentPrice = currentPrice;
	}

	public static PortfolioHolding empty(Portfolio portfolio, Stock stock) {
		return of(portfolio, stock, Money.zero());
	}

	public static PortfolioHolding of(Portfolio portfolio, Stock stock) {
		return of(portfolio, stock, null);
	}

	public static PortfolioHolding of(Portfolio portfolio, Stock stock, Money currentPrice) {
		return of(null, portfolio, stock, currentPrice);
	}

	public static PortfolioHolding of(Long id, Portfolio portfolio, Stock stock, Money currentPrice) {
		return new PortfolioHolding(LocalDateTime.now(), null, id, portfolio, stock, currentPrice);
	}

	//== 연관관계 메소드 ==//
	public void addPurchaseHistory(PurchaseHistory purchaseHistory) {
		if (!this.purchaseHistory.contains(purchaseHistory)) {
			this.purchaseHistory.add(purchaseHistory);
			purchaseHistory.setHolding(this);
		}
	}

	public void setPortfolio(Portfolio portfolio) {
		if (this.portfolio != null && this.portfolio.getPortfolioHoldings().contains(this)) {
			this.portfolio.removeHolding(this);
		}
		this.portfolio = portfolio;
		if (portfolio != null && !portfolio.getPortfolioHoldings().contains(this)) {
			portfolio.addHolding(this);
		}
	}

	/**
	 * 포트폴리오 종목의 총 손익을 계산 후 반환.
	 * <p>
	 * TotalGain = (CurrentPrice - AverageCostPerShare) * NumShares
	 * </p>
	 * @param currentPrice 종목의 현재가
	 * @return 포트폴리오 종목의 총 손익
	 */
	public Expression calculateTotalGain(@NotNull Expression currentPrice) {
		Expression averageCostPerShare = calculateAverageCostPerShare();
		int numShares = calculateNumShares().intValue();
		return currentPrice.minus(averageCostPerShare).times(numShares);
	}

	/**
	 * 포트폴리오 종목의 평균 매입가 계산 후 반환.
	 * <p>
	 * AverageCostPerShare = TotalInvestmentAmount / NumShares
	 * </p>
	 * @return 종목 평균 매입가
	 */
	public Expression calculateAverageCostPerShare() {
		return calculateTotalInvestmentAmount().divide(calculateNumShares());
	}

	/**
	 * 포트폴리오 종목 매입 개수 계산 후 반환.
	 * <p>
	 * NumShares = sum(PurchaseHistory.NumShares)
	 * </p>
	 * @return 종목 매입 합계
	 */
	public Count calculateNumShares() {
		return purchaseHistory.stream()
			.map(PurchaseHistory::getNumShares)
			.reduce(Count.zero(), Count::add);
	}

	/**
	 * 포트폴리오 종목의 총 투자금액 계산 후 반환.
	 * <p>
	 * TotalInvestmentAmount = sum(PurchaseHistory.InvestmentAmount)
	 * </p>
	 * @return 총 투자금액 합계
	 */
	public Expression calculateTotalInvestmentAmount() {
		return purchaseHistory.stream()
			.map(PurchaseHistory::calculateInvestmentAmount)
			.reduce(Money.wonZero(), Expression::plus);
	}

	/**
	 * 포트폴리오 종목의 총 손익율 계산 후 반환.
	 * <p>
	 * TotalGainRate = (TotalGain / TotalInvestmentAmount)
	 * </p>
	 * @param currentPrice 종목의 현재가
	 * @return 종목의 총 손익율
	 */
	public RateDivision calculateTotalGainRate(Expression currentPrice) {
		Expression totalGain = calculateTotalGain(currentPrice);
		Expression totalInvestmentAmount = calculateTotalInvestmentAmount();
		return totalGain.divide(totalInvestmentAmount);
	}

	/**
	 * 포트폴리오 종목의 평가 금액을 계산 후 반환.
	 * <p>
	 * CurrentValuation = CurrentPrice * NumShares
	 * </p>
	 * @param currentPrice 종목의 현재가
	 * @return 포트폴리오 종목의 평가 금액
	 */
	public Expression calculateCurrentValuation(@NotNull Expression currentPrice) {
		return currentPrice.times(calculateNumShares().getValue().intValue());
	}

	/**
	 * 포트폴리오 종목의 당일 변동 금액 계산 후 반환.
	 * <p>
	 * DailyChange = CurrentPrice - ClosingPrice
	 * </p>
	 * @param currentPrice 종목 현재가
	 * @param closingPrice 종목 종가
	 * @return 당일 변동 금액
	 */
	public Expression calculateDailyChange(@NotNull Expression currentPrice, @NotNull Expression closingPrice) {
		return currentPrice.minus(closingPrice);
	}

	// 당일 변동율 = ((종목 현재가 - 직전 거래일 종가) / 직전 거래일 종가) * 100%
	public RateDivision calculateDailyChangeRate(Expression lastDayClosingPrice) {
		return currentPrice.minus(lastDayClosingPrice).divide(lastDayClosingPrice);
	}

	// 예상 연간 배당율 = (예상 연간 배당금 / 현재 가치) * 100
	public RateDivision calculateAnnualExpectedDividendYield(Expression currentPrice) {
		Expression annualDividend = calculateAnnualExpectedDividend();
		Expression currentValuation = calculateCurrentValuation(currentPrice);
		return annualDividend.divide(currentValuation);
	}

	// 연간 배당금 = 종목의 배당금 합계
	public Expression calculateAnnualDividend() {
		List<StockDividend> stockDividends = stock.getCurrentYearDividends();

		Expression totalDividend = Money.zero();
		for (PurchaseHistory history : purchaseHistory) {
			for (StockDividend stockDividend : stockDividends) {
				if (stockDividend.isSatisfiedBy(history)) {
					totalDividend = totalDividend.plus(stockDividend.calculateDividendSum(history.getNumShares()));
				}
			}
		}
		return totalDividend;
	}

	// 예상 연간 배당금 계산
	public Expression calculateAnnualExpectedDividend() {
		Expression annualDividend = calculateAnnualDividend();
		Expression annualExpectedDividend = stock.createMonthlyExpectedDividends(purchaseHistory, LocalDate.now())
			.values()
			.stream()
			.reduce(Money.zero(), Expression::plus);
		return annualDividend.plus(annualExpectedDividend);
	}

	public Expression calculateCurrentMonthDividend() {
		List<StockDividend> stockDividends = stock.getCurrentMonthDividends();
		return stockDividends.stream()
			.flatMap(stockDividend ->
				Stream.of(
					purchaseHistory.stream()
						.filter(stockDividend::isPurchaseDateBeforeExDividendDate)
						.map(PurchaseHistory::getNumShares)
						.reduce(Count.zero(), Count::add)
						.multiply(stockDividend.getDividend())
				)
			)
			.reduce(Money.zero(), Expression::plus);
	}

	// 월별 배당금 계산, key=월, value=배당금 합계
	public Map<Integer, Expression> createMonthlyDividendMap(LocalDate currentLocalDate) {
		log.debug("currentLocalDate : {}", currentLocalDate);
		Map<Integer, Expression> monthlyDividends = stock.createMonthlyDividends(purchaseHistory, currentLocalDate);
		Map<Integer, Expression> monthlyExpectedDividends = stock.createMonthlyExpectedDividends(purchaseHistory,
			currentLocalDate);
		monthlyExpectedDividends.forEach(
			(month, dividend) -> monthlyDividends.merge(month, dividend, Expression::plus));
		return monthlyDividends;
	}

	public void applyCurrentPrice(PriceRepository manager) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		this.currentPrice = stock.getCurrentPrice(manager).reduce(bank, to);
	}

	public PortfolioPieChartItem createPieChartItem(RateDivision weight, Expression currentValuation,
		Expression totalGain, Percentage totalReturnPercentage) {
		String name = stock.getCompanyName();

		Bank bank = Bank.getInstance();
		Percentage weightPercentage = weight.toPercentage(bank, Currency.KRW);
		return PortfolioPieChartItem.stock(name, currentValuation, weightPercentage, totalGain, totalReturnPercentage);
	}

	public Expression getLastDayClosingPrice(ClosingPriceRepository manager) {
		return stock.getClosingPrice(manager);
	}

	public boolean hasAuthorization(Long memberId) {
		return portfolio.hasAuthorization(memberId);
	}

	public List<PurchaseHistory> getPurchaseHistory() {
		return Collections.unmodifiableList(purchaseHistory);
	}

	public Optional<Money> fetchPrice(PriceRepository repository) {
		return stock.fetchPrice(repository);
	}

	@Override
	public String toString() {
		return String.format("PortfolioHolding(id=%d, portfolio=%d, stock=%s)", id, portfolio.getId(),
			stock.getTickerSymbol());
	}
}
