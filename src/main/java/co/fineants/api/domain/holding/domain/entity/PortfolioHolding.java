package co.fineants.api.domain.holding.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.annotations.BatchSize;

import co.fineants.api.domain.BaseEntity;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.common.money.RateDivision;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioPieChartItem;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.PriceRepository;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
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
	private List<PurchaseHistory> purchaseHistories = new ArrayList<>();

	protected PortfolioHolding() {
		this.purchaseHistories = new ArrayList<>();
	}

	private PortfolioHolding(Long id, Portfolio portfolio, Stock stock) {
		super(LocalDateTime.now(), LocalDateTime.now());
		this.id = id;
		this.portfolio = portfolio;
		this.stock = stock;
	}

	public static PortfolioHolding of(Portfolio portfolio, Stock stock) {
		return of(null, portfolio, stock);
	}

	public static PortfolioHolding of(Long id, Portfolio portfolio, Stock stock) {
		return new PortfolioHolding(id, portfolio, stock);
	}

	//== 연관관계 메소드 시작 ==//
	public void addPurchaseHistory(PurchaseHistory purchaseHistory) {
		if (!this.purchaseHistories.contains(purchaseHistory)) {
			this.purchaseHistories.add(purchaseHistory);
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
	//== 연관관계 메소드 종료 ==//

	/**
	 * 매입 이력들의 평균 매입가 계산 후 반환.
	 *
	 * @param calculator 포트폴리오 계산기 객체
	 * @return 평균 매입가
	 */
	public Expression calculateAverageCostPerShare(PortfolioCalculator calculator) {
		return calculator.calAverageCostPerShare(purchaseHistories);
	}

	public Count calculateNumShares(PortfolioCalculator calculator) {
		return calculator.calNumShares(purchaseHistories);
	}

	public Expression calculateAnnualExpectedDividend(PortfolioCalculator calculator) {
		return calculator.calAnnualExpectedDividend(stock, purchaseHistories);
	}

	public Expression calculateCurrentMonthDividend(PortfolioCalculator calculator) {
		return calculator.calCurrentMonthExpectedDividend(stock, purchaseHistories);
	}

	/**
	 * 포트폴리오 종목의 월별 배당금 합계를 가진 맵을 생성 후 반환.
	 * <p>
	 * 결과 맵의 배당금 합계에는 실제 월별 배당금과 예상되는 월별 배당금 합계가 포함되어 있습니다.
	 * </p>
	 *
	 * @param currentLocalDate 기준 일자
	 * @return 월별 배당금 합계 맵
	 */
	public Map<Month, Expression> createMonthlyDividendMap(LocalDate currentLocalDate) {
		Map<Month, Expression> result = new EnumMap<>(Month.class);
		Map<Month, Expression> monthlyDividends = stock.createMonthlyDividends(purchaseHistories, currentLocalDate);
		Map<Month, Expression> monthlyExpectedDividends = stock.createMonthlyExpectedDividends(purchaseHistories,
			currentLocalDate);
		monthlyDividends.forEach(
			(month, dividend) -> result.merge(month, dividend, Expression::plus));
		monthlyExpectedDividends.forEach(
			(month, dividend) -> result.merge(month, dividend, Expression::plus));
		return result;
	}

	public Map<Month, Expression> createMonthlyDividendMap(PortfolioCalculator calculator, LocalDate currentLocalDate) {
		return calculator.calMonthlyDividendMap(stock, purchaseHistories, currentLocalDate);
	}

	/**
	 * 포트폴리오 종목의 파이차트 요소를 생성후 반환.
	 * @param weight 종목의 비중
	 * @param currentValuation 현재 평가금액
	 * @param totalGain 총 손익
	 * @param totalGainRate 총 손익율
	 * @return 파이차트 요소
	 */
	public PortfolioPieChartItem createPieChartItem(RateDivision weight, Expression currentValuation,
		Expression totalGain, Percentage totalGainRate) {
		String name = stock.getCompanyName();

		Bank bank = Bank.getInstance();
		Percentage weightPercentage = weight.toPercentage(bank, Currency.KRW);
		return PortfolioPieChartItem.stock(name, currentValuation, weightPercentage, totalGain, totalGainRate);
	}

	public Optional<Money> fetchPrice(PriceRepository repository) {
		return stock.fetchPrice(repository);
	}

	public Expression fetchClosingPrice(ClosingPriceRepository manager) {
		return stock.getClosingPrice(manager);
	}

	public boolean hasAuthorization(Long memberId) {
		return portfolio.hasAuthorization(memberId);
	}

	public List<PurchaseHistory> getPurchaseHistories() {
		return Collections.unmodifiableList(purchaseHistories);
	}

	@Override
	public String toString() {
		return String.format("PortfolioHolding(id=%d, portfolio=%d, stock=%s)", id, portfolio.getId(),
			stock.getTickerSymbol());
	}

	public Expression calculateTotalInvestmentAmount(PortfolioCalculator calculator) {
		return calculator.calTotalInvestment(purchaseHistories);
	}
}
