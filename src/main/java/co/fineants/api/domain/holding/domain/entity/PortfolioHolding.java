package co.fineants.api.domain.holding.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.hibernate.annotations.BatchSize;

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
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
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
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@ToString(exclude = {"stock", "portfolio", "purchaseHistory"})
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

	// 종목 총 손익 = (종목 현재가 - 종목 평균 매입가) * 개수
	public Expression calculateTotalGain() {
		Expression averageCostPerShare = calculateAverageCostPerShare();
		return currentPrice.minus(averageCostPerShare).times(calculateNumShares().getValue().intValue());
	}

	// 종목 평균 매입가 = 총 투자 금액 / 개수
	public Expression calculateAverageCostPerShare() {
		return calculateTotalInvestmentAmount().divide(calculateNumShares());
	}

	// 총 매입 개수 = 매입 내역들의 매입개수의 합계
	public Count calculateNumShares() {
		return purchaseHistory.stream()
			.map(PurchaseHistory::getNumShares)
			.reduce(Count.zero(), Count::add);
	}

	// 총 투자 금액 = 투자 금액들의 합계
	public Expression calculateTotalInvestmentAmount() {
		return purchaseHistory.stream()
			.map(PurchaseHistory::calculateInvestmentAmount)
			.reduce(Money.wonZero(), Expression::plus);
	}

	// 종목 총 손익율 = 총 손익 / 총 투자 금액
	public RateDivision calculateTotalReturnRate() {
		Expression totalGain = calculateTotalGain();
		Expression totalInvestmentAmount = calculateTotalInvestmentAmount();
		return totalGain.divide(totalInvestmentAmount);
	}

	// 평가 금액(현재 가치) = 현재가 * 개수
	public Expression calculateCurrentValuation() {
		return currentPrice.times(calculateNumShares().getValue().intValue());
	}

	// 당일 변동 금액 = 종목 현재가 - 직전 거래일의 종가
	public Expression calculateDailyChange(Expression lastDayClosingPrice) {
		return currentPrice.minus(lastDayClosingPrice);
	}

	// 당일 변동율 = ((종목 현재가 - 직전 거래일 종가) / 직전 거래일 종가) * 100%
	public RateDivision calculateDailyChangeRate(Expression lastDayClosingPrice) {
		return currentPrice.minus(lastDayClosingPrice).divide(lastDayClosingPrice);
	}

	// 예상 연간 배당율 = (예상 연간 배당금 / 현재 가치) * 100
	public RateDivision calculateAnnualExpectedDividendYield() {
		Expression annualDividend = calculateAnnualExpectedDividend();
		Expression currentValuation = calculateCurrentValuation();
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

	public void applyCurrentPrice(CurrentPriceRedisRepository manager) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		this.currentPrice = stock.getCurrentPrice(manager).reduce(bank, to);
	}

	public PortfolioPieChartItem createPieChartItem(RateDivision weight) {
		String name = stock.getCompanyName();
		Expression currentValuation = calculateCurrentValuation();
		Expression totalGain = calculateTotalGain();
		RateDivision totalReturnRate = calculateTotalReturnRate();

		Bank bank = Bank.getInstance();
		Percentage weightPercentage = weight.toPercentage(bank, Currency.KRW);
		Percentage totalReturnPercentage = totalReturnRate.toPercentage(bank, Currency.KRW);
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
}
