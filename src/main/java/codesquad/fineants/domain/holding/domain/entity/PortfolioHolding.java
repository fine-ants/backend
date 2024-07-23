package codesquad.fineants.domain.holding.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.hibernate.annotations.BatchSize;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Bank;
import codesquad.fineants.domain.common.money.Currency;
import codesquad.fineants.domain.common.money.Expression;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.common.money.RateDivision;
import codesquad.fineants.domain.dividend.domain.entity.StockDividend;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioPieChartItem;
import codesquad.fineants.domain.kis.repository.ClosingPriceRepository;
import codesquad.fineants.domain.kis.repository.CurrentPriceRepository;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.purchasehistory.domain.entity.PurchaseHistory;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@ToString(exclude = {"stock", "portfolio", "purchaseHistory"})
@Entity
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
		this.portfolio = portfolio;
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
				if (history.isSatisfiedDividend(stockDividend.getExDividendDate())) {
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
						.filter(history ->
							history.getPurchaseDate().isBefore(stockDividend.getExDividendDate().atStartOfDay()))
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

	public void applyCurrentPrice(CurrentPriceRepository manager) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		this.currentPrice = stock.getCurrentPrice(manager).reduce(bank, to);
	}

	public RateDivision calculateWeightBy(Expression portfolioAsset) {
		Expression currentValuation = calculateCurrentValuation();
		return currentValuation.divide(portfolioAsset);
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
}
