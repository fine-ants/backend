package codesquad.fineants.domain.portfolio_holding;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.BatchSize;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Bank;
import codesquad.fineants.domain.common.money.Expression;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock_dividend.StockDividend;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioPieChartItem;
import lombok.Builder;
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

	@Builder
	public PortfolioHolding(LocalDateTime createAt, LocalDateTime modifiedAt, Long id,
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

	public static PortfolioHolding of(Portfolio portfolio, Stock stock, Money currentPrice) {
		return PortfolioHolding.builder()
			.currentPrice(currentPrice)
			.portfolio(portfolio)
			.stock(stock)
			.build();
	}

	//== 연관관계 메소드 ==//
	public void addPurchaseHistory(PurchaseHistory purchaseHistory) {
		if (!this.purchaseHistory.contains(purchaseHistory)) {
			this.purchaseHistory.add(purchaseHistory);
		}
	}

	// 종목 총 손익 = (종목 현재가 - 종목 평균 매입가) * 개수
	public Money calculateTotalGain() {
		Money averageCostPerShare = calculateAverageCostPerShare();
		Money amount = currentPrice.subtract(averageCostPerShare).multiply(calculateNumShares());
		return Bank.getInstance().toWon(amount);
	}

	// 종목 평균 매입가 = 총 투자 금액 / 개수
	public Money calculateAverageCostPerShare() {
		return calculateTotalInvestmentAmount().divide(calculateNumShares());
	}

	// 총 매입 개수 = 매입 내역들의 매입개수의 합계
	public Count calculateNumShares() {
		return purchaseHistory.stream()
			.map(PurchaseHistory::getNumShares)
			.reduce(Count.zero(), Count::add);
	}

	// 총 투자 금액 = 투자 금액들의 합계
	public Money calculateTotalInvestmentAmount() {
		Expression amount = purchaseHistory.stream()
			.map(PurchaseHistory::calculateInvestmentAmount)
			.reduce(Money.wonZero(), Expression::plus);
		return Bank.getInstance().toWon(amount);
	}

	// 종목 총 손익율 = 총 손익 / 총 투자 금액
	public double calculateTotalReturnRate() {
		Money totalGain = calculateTotalGain();
		Money totalInvestmentAmount = calculateTotalInvestmentAmount();
		return totalGain.divide(totalInvestmentAmount).toPercentage();
	}

	// 평가 금액(현재 가치) = 현재가 * 개수
	public Money calculateCurrentValuation() {
		return currentPrice.multiply(calculateNumShares());
	}

	// 당일 변동 금액 = 종목 현재가 - 직전 거래일의 종가
	public Money calculateDailyChange(Money lastDayClosingPrice) {
		return currentPrice.subtract(lastDayClosingPrice);
	}

	// 당일 변동율 = ((종목 현재가 - 직전 거래일 종가) / 직전 거래일 종가) * 100%
	public double calculateDailyChangeRate(Money lastDayClosingPrice) {
		return currentPrice.subtract(lastDayClosingPrice).divide(lastDayClosingPrice).toPercentage();
	}

	// 예상 연간 배당율 = (예상 연간 배당금 / 현재 가치) * 100
	public double calculateAnnualExpectedDividendYield() {
		Money annualDividend = calculateAnnualExpectedDividend();
		Money currentValuation = calculateCurrentValuation();
		return annualDividend.divide(currentValuation).toPercentage();
	}

	// 연간 배당금 = 종목의 배당금 합계
	public Money calculateAnnualDividend() {
		List<StockDividend> stockDividends = stock.getCurrentYearDividends();

		Money totalDividend = Money.zero();
		for (PurchaseHistory history : purchaseHistory) {
			for (StockDividend stockDividend : stockDividends) {
				if (history.isSatisfiedDividend(stockDividend.getExDividendDate())) {
					totalDividend = totalDividend.add(stockDividend.calculateDividendSum(history.getNumShares()));
				}
			}
		}
		return totalDividend;
	}

	// 예상 연간 배당금 계산
	public Money calculateAnnualExpectedDividend() {
		Money annualDividend = calculateAnnualDividend();
		Money annualExpectedDividend = stock.createMonthlyExpectedDividends(purchaseHistory, LocalDate.now())
			.values()
			.stream()
			.reduce(Money.zero(), Money::add);
		return annualDividend.add(annualExpectedDividend);
	}

	public Money calculateCurrentMonthDividend() {
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
			.reduce(Money.zero(), Money::add);
	}

	// 월별 배당금 계산, key=월, value=배당금 합계
	public Map<Integer, Money> createMonthlyDividendMap(LocalDate currentLocalDate) {
		log.debug("currentLocalDate : {}", currentLocalDate);
		Map<Integer, Money> monthlyDividends = stock.createMonthlyDividends(purchaseHistory, currentLocalDate);
		Map<Integer, Money> monthlyExpectedDividends = stock.createMonthlyExpectedDividends(purchaseHistory,
			currentLocalDate);
		monthlyExpectedDividends.forEach((month, dividend) -> monthlyDividends.merge(month, dividend, Money::add));
		return monthlyDividends;
	}

	public void applyCurrentPrice(CurrentPriceManager manager) {
		this.currentPrice = stock.getCurrentPrice(manager);
	}

	public double calculateWeightBy(Money portfolioAsset) {
		Money currentValuation = calculateCurrentValuation();
		return currentValuation.divide(portfolioAsset).toPercentage();
	}

	public PortfolioPieChartItem createPieChartItem(Double weight) {
		String name = stock.getCompanyName();
		Money currentValuation = calculateCurrentValuation();
		Money totalGain = calculateTotalGain();
		Double totalReturnRate = calculateTotalReturnRate();
		return PortfolioPieChartItem.stock(name, currentValuation, weight, totalGain, totalReturnRate);
	}

	public Money getLastDayClosingPrice(LastDayClosingPriceManager manager) {
		return stock.getClosingPrice(manager);
	}
}
