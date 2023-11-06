package codesquad.fineants.domain.portfolio_holding;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock_dividend.StockDividend;
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

	@OneToMany(mappedBy = "portFolioHolding")
	private final List<PurchaseHistory> purchaseHistory = new ArrayList<>();

	@Transient
	private Long currentPrice;    // 현재가

	@Builder
	private PortfolioHolding(Long id, Long currentPrice, Portfolio portfolio, Stock stock) {
		this.id = id;
		this.currentPrice = currentPrice;
		this.portfolio = portfolio;
		this.stock = stock;
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

	public long calculateNumShares() {
		return purchaseHistory.stream()
			.mapToLong(PurchaseHistory::getNumShares)
			.sum();
	}

	// 총 투자 금액 = 투자 금액들의 합계
	public long calculateTotalInvestmentAmount() {
		return purchaseHistory.stream()
			.mapToLong(PurchaseHistory::calculateInvestmentAmount)
			.sum();
	}

	// 종목 총 손익율 = 총 손익 / 총 투자 금액
	public Integer calculateTotalReturnRate() {
		double totalInvestmentAmount = (double)calculateTotalInvestmentAmount();
		if (totalInvestmentAmount == 0) {
			return 0;
		}
		double totalGain = (double)calculateTotalGain();
		int result = (int)((totalGain / totalInvestmentAmount) * 100);
		log.debug("totalReturnRate : {}", result);
		return result;
	}

	// 평가 금액(현재 가치) = 현재가 * 개수
	public long calculateCurrentValuation() {
		return currentPrice * calculateNumShares();
	}

	public boolean hasMonthlyDividend(LocalDateTime monthDateTime) {
		return stock.hasMonthlyDividend(monthDateTime);
	}

	public long readDividend(LocalDateTime monthDateTime) {
		return stock.readDividend(monthDateTime) * calculateNumShares();
	}

	// 당일 손익 = 현재 평가금액 - 총 투자 금액
	public Long calculateDailyChange() {
		return calculateCurrentValuation() - calculateTotalInvestmentAmount();
	}

	// 당일 손익율 = ((현재 평가금액 - 총 투자 금액) / 총 투자 금액) * 100
	public Integer calculateDailyChangeRate() {
		long currentValuation = calculateCurrentValuation();
		long totalInvestmentAmount = calculateTotalInvestmentAmount();
		if (totalInvestmentAmount == 0) {
			return 0;
		}
		return (int)(((double)(currentValuation - totalInvestmentAmount) / (double)totalInvestmentAmount) * 100);
	}

	// 연간배당율 = (연간배당금 / 현재 가치) * 100
	public Integer calculateAnnualDividendYield() {
		double currentValuation = calculateCurrentValuation();
		if (currentValuation == 0) {
			return 0;
		}
		double annualDividend = calculateAnnualDividend();
		return (int)((annualDividend / currentValuation) * 100);
	}

	// 연간 배당금 = 종목의 배당금 합계
	public long calculateAnnualDividend() {
		long annualDividend = stock.getStockDividends().stream()
			.mapToLong(StockDividend::getDividend)
			.sum();
		return annualDividend * calculateNumShares();
	}

	public void changeCurrentPrice(long currentPrice) {
		this.currentPrice = currentPrice;
	}
}
