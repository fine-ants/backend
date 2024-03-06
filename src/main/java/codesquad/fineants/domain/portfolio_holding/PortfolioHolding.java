package codesquad.fineants.domain.portfolio_holding;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

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
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock_dividend.StockDividend;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioPieChartItem;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@ToString(exclude = {"stock", "portfolio", "purchaseHistory"})
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
	private final List<PurchaseHistory> purchaseHistory = new ArrayList<>();

	@Transient
	private Long currentPrice;    // 현재가

	@Builder
	private PortfolioHolding(Long id, Portfolio portfolio, Stock stock, Long currentPrice) {
		this.id = id;
		this.portfolio = portfolio;
		this.stock = stock;
		this.currentPrice = currentPrice;
	}

	public static PortfolioHolding empty(Portfolio portfolio, Stock stock) {
		return of(portfolio, stock, 0L);
	}

	public static PortfolioHolding of(Portfolio portfolio, Stock stock, Long currentPrice) {
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
	public long calculateTotalGain() {
		return (long)(currentPrice - calculateAverageCostPerShare()) * calculateNumShares();
	}

	// 종목 평균 매입가 = 총 투자 금액 / 개수
	public Double calculateAverageCostPerShare() {
		long numShares = calculateNumShares();
		if (numShares == 0) {
			return 0.0;
		}
		return (double)(calculateTotalInvestmentAmount() / numShares);
	}

	// 총 매입 개수 = 매입 내역들의 매입개수의 합계
	public Long calculateNumShares() {
		return purchaseHistory.stream()
			.mapToLong(PurchaseHistory::getNumShares)
			.sum();
	}

	// 총 투자 금액 = 투자 금액들의 합계
	public Long calculateTotalInvestmentAmount() {
		return purchaseHistory.stream()
			.mapToLong(PurchaseHistory::calculateInvestmentAmount)
			.sum();
	}

	// 종목 총 손익율 = 총 손익 / 총 투자 금액
	public Double calculateTotalReturnRate() {
		double totalInvestmentAmount = calculateTotalInvestmentAmount();
		if (totalInvestmentAmount == 0) {
			return 0.0;
		}
		double totalGain = calculateTotalGain();
		return (totalGain / totalInvestmentAmount) * 100;
	}

	// 평가 금액(현재 가치) = 현재가 * 개수
	public Long calculateCurrentValuation() {
		return currentPrice * calculateNumShares();
	}

	// 당일 변동 금액 = 종목 현재가 - 직전 거래일의 종가
	public Long calculateDailyChange(long lastDayClosingPrice) {
		return currentPrice - lastDayClosingPrice;
	}

	// 당일 변동율 = ((종목 현재가 - 직전 거래일 종가) / 직전 거래일 종가) * 100%
	public Double calculateDailyChangeRate(Long lastDayClosingPrice) {
		if (lastDayClosingPrice == 0) {
			return 0.0;
		}
		double dailyChange = currentPrice.doubleValue() - lastDayClosingPrice.doubleValue();
		return (dailyChange / lastDayClosingPrice.doubleValue()) * 100;
	}

	// 예상 연간 배당율 = (예상 연간 배당금 / 현재 가치) * 100
	public Double calculateAnnualExpectedDividendYield() {
		double currentValuation = calculateCurrentValuation();
		if (currentValuation == 0) {
			return 0.0;
		}
		double annualDividend = calculateAnnualExpectedDividend();
		return (annualDividend / currentValuation) * 100;
	}

	// 연간 배당금 = 종목의 배당금 합계
	public Long calculateAnnualDividend() {
		List<StockDividend> stockDividends = stock.getCurrentYearDividends();

		long totalDividend = 0L;
		for (PurchaseHistory history : purchaseHistory) {
			for (StockDividend stockDividend : stockDividends) {
				if (history.isSatisfiedDividend(stockDividend.getExDividendDate())) {
					totalDividend += stockDividend.calculateDividendSum(history.getNumShares());
				}
			}
		}
		return totalDividend;
	}

	// 예상 연간 배당금 계산
	public Long calculateAnnualExpectedDividend() {
		long annualDividend = calculateAnnualDividend();
		long annualExpectedDividend = stock.createMonthlyExpectedDividends(purchaseHistory, LocalDate.now())
			.values()
			.stream()
			.mapToLong(Long::valueOf)
			.sum();
		return annualDividend + annualExpectedDividend;
	}

	public void changeCurrentPrice(long currentPrice) {
		this.currentPrice = currentPrice;
	}

	public Long calculateCurrentMonthDividend() {
		List<StockDividend> stockDividends = stock.getCurrentMonthDividends();
		return stockDividends.stream()
			.flatMapToLong(stockDividend ->
				LongStream.of(purchaseHistory.stream()
					.filter(history ->
						history.getPurchaseDate().isBefore(stockDividend.getExDividendDate().atStartOfDay()))
					.mapToLong(PurchaseHistory::getNumShares)
					.sum() * stockDividend.getDividend()))
			.sum();
	}

	// 월별 배당금 계산, key=월, value=배당금 합계
	public Map<Integer, Long> createMonthlyDividendMap(LocalDate currentLocalDate) {
		Map<Integer, Long> monthlyDividends = stock.createMonthlyDividends(purchaseHistory, currentLocalDate);
		Map<Integer, Long> monthlyExpectedDividends = stock.createMonthlyExpectedDividends(purchaseHistory,
			currentLocalDate);
		monthlyExpectedDividends.forEach((month, dividend) -> monthlyDividends.merge(month, dividend, Long::sum));
		return monthlyDividends;
	}

	public void applyCurrentPrice(CurrentPriceManager manager) {
		this.currentPrice = stock.getCurrentPrice(manager);
	}

	public Double calculateWeightBy(Double portfolioAsset) {
		return portfolioAsset != 0.0 ? calculateCurrentValuation().doubleValue() / portfolioAsset * 100 : 0.0;
	}

	public PortfolioPieChartItem createPieChartItem(Double weight) {
		String name = stock.getCompanyName();
		Long currentValuation = calculateCurrentValuation();
		Long totalGain = calculateTotalGain();
		Double totalReturnRate = calculateTotalReturnRate();
		return new PortfolioPieChartItem(name, currentValuation, weight, totalGain, totalReturnRate);
	}

	public Long getLastDayClosingPrice(LastDayClosingPriceManager manager) {
		return stock.getLastDayClosingPrice(manager);
	}
}
