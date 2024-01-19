package codesquad.fineants.domain.portfolio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioDividendChartItem;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioPieChartItem;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioSectorChartItem;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"member", "portfolioHoldings"})
@Entity
public class Portfolio extends BaseEntity {
	@OneToMany(mappedBy = "portfolio")
	private final List<PortfolioHolding> portfolioHoldings = new ArrayList<>();
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String securitiesFirm;
	private Long budget;
	private Long targetGain;
	private Long maximumLoss;
	private Boolean targetGainIsActive;
	private Boolean maximumIsActive;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@Builder
	public Portfolio(Long id, String name, String securitiesFirm, Long budget, Long targetGain, Long maximumLoss,
		Boolean targetGainIsActive, Boolean maximumIsActive, Member member) {
		this.id = id;
		this.name = name;
		this.securitiesFirm = securitiesFirm;
		this.budget = budget;
		this.targetGain = targetGain;
		this.maximumLoss = maximumLoss;
		this.targetGainIsActive = targetGainIsActive;
		this.maximumIsActive = maximumIsActive;
		this.member = member;
	}

	//== 연관관계 메소드 ==//
	public void addPortfolioStock(PortfolioHolding portFolioHolding) {
		if (!portfolioHoldings.contains(portFolioHolding)) {
			portfolioHoldings.add(portFolioHolding);
		}
	}

	public void change(Portfolio changePortfolio) {
		this.name = changePortfolio.name;
		this.securitiesFirm = changePortfolio.securitiesFirm;
		this.budget = changePortfolio.budget;
		this.targetGain = changePortfolio.targetGain;
		this.maximumLoss = changePortfolio.maximumLoss;
	}

	public boolean hasAuthorization(Long memberId) {
		return member.hasAuthorization(memberId);
	}

	// 포트폴리오 총 손익 = 모든 종목 총 손익의 합계
	// 종목 총 손익 = (종목 현재가 - 종목 평균 매입가) * 개수
	// 종목 평균 매입가 = 종목의 총 투자 금액 / 총 주식 개수
	public long calculateTotalGain() {
		return portfolioHoldings.stream()
			.mapToLong(PortfolioHolding::calculateTotalGain)
			.sum();
	}

	// 포트폴리오 총 손익율 = (포트폴리오 총 손익 / 포트폴리오 총 투자 금액) * 100%
	public Integer calculateTotalGainRate() {
		long totalInvestmentAmount = calculateTotalInvestmentAmount();
		if (totalInvestmentAmount == 0) {
			return 0;
		}
		long totalGain = calculateTotalGain();
		log.info("totalGain : {}", totalGain);
		log.info("totalInvestmentAmount : {}", totalInvestmentAmount);
		log.info("result : {}", (int)(((double)calculateTotalGain() / (double)totalInvestmentAmount) * 100));
		return (int)(((double)calculateTotalGain() / (double)totalInvestmentAmount) * 100);
	}

	// 포트폴리오 총 투자 금액 = 각 종목들의 구입가들의 합계
	public Long calculateTotalInvestmentAmount() {
		return portfolioHoldings.stream()
			.mapToLong(PortfolioHolding::calculateTotalInvestmentAmount)
			.sum();
	}

	// 포트폴리오 당일 손익 = 모든 종목들의 평가 금액 합계 - 이전일 포트폴리오의 모든 종목들의 평가 금액 합계
	// 단, 이전일의 포트포릴오의 모든 종목들의 평가금액이 없는 경우 총 투자금액으로 뺀다
	public Long calculateDailyGain(PortfolioGainHistory previousHistory) {
		Long previousCurrentValuation = previousHistory.getCurrentValuation();
		if (previousCurrentValuation == 0) {
			return calculateTotalCurrentValuation() - calculateTotalInvestmentAmount();
		}
		return calculateTotalCurrentValuation() - previousCurrentValuation;
	}

	// 포트폴리오 평가 금액(현재 가치) = 모든 종목들의 평가금액 합계
	public Long calculateTotalCurrentValuation() {
		return portfolioHoldings.stream()
			.mapToLong(PortfolioHolding::calculateCurrentValuation)
			.sum();
	}

	// 포트폴리오 당일 손익율 = (당일 포트폴리오 가치 총합 - 이전 포트폴리오 가치 총합) / 이전 포트폴리오 가치 총합
	// 단, 이전 포트폴리오가 없는 경우 ((당일 포트폴리오 가치 총합 - 당일 포트폴리오 총 투자 금액) / 당일 포트폴리오 총 투자 금액) * 100%
	public Integer calculateDailyGainRate(PortfolioGainHistory prevHistory) {
		double prevCurrentValuation = prevHistory.getCurrentValuation();
		if (prevCurrentValuation == 0) {
			double currentValuation = calculateTotalCurrentValuation();
			double totalInvestmentAmount = calculateTotalInvestmentAmount();
			return (int)(((currentValuation - totalInvestmentAmount) / totalInvestmentAmount) * 100);
		}
		double currentValuation = calculateTotalCurrentValuation();
		return (int)(((currentValuation - prevCurrentValuation) / prevCurrentValuation) * 100);
	}

	// 포트폴리오 당월 예상 배당금 = 각 종목들에 해당월의 배당금 합계
	public long calculateCurrentMonthDividend() {
		return portfolioHoldings.stream()
			.mapToLong(PortfolioHolding::calculateCurrentMonthDividend)
			.sum();
	}

	public Integer getNumberOfShares() {
		return portfolioHoldings.size();
	}

	// 잔고 = 예산 - 총 투자 금액
	public Long calculateBalance() {
		return budget - calculateTotalInvestmentAmount();
	}

	// 총 연간 배당금 = 각 종목들의 연배당금의 합계
	public Long calculateAnnualDividend() {
		return portfolioHoldings.stream()
			.map(portfolioHolding -> portfolioHolding.createMonthlyDividendMap(LocalDate.now()))
			.mapToLong(map -> map.values().stream()
				.mapToLong(Long::longValue).sum())
			.sum();
	}

	// 총 연간배당율 = 모든 종목들의 연 배당금 합계 / 모든 종목들의 총 가치의 합계) * 100
	public Integer calculateAnnualDividendYield() {
		double currentValuation = calculateTotalCurrentValuation();
		if (currentValuation == 0) {
			return 0;
		}
		double totalAnnualDividend = calculateAnnualDividend();
		return (int)((totalAnnualDividend / currentValuation) * 100);
	}

	// 최대손실율 = ((예산 - 최대손실금액) / 예산) * 100
	public Integer calculateMaximumLossRate() {
		return (int)(((double)(budget - maximumLoss) / (double)budget) * 100);
	}

	// 투자대비 연간 배당율 = 포트폴리오 총 연배당금 / 포트폴리오 투자금액 * 100
	public Integer calculateAnnualInvestmentDividendYield() {
		double totalInvestmentAmount = calculateTotalInvestmentAmount();
		if (totalInvestmentAmount == 0) {
			return 0;
		}
		double totalAnnualDividend = calculateAnnualDividend();
		return (int)((totalAnnualDividend / totalInvestmentAmount) * 100);
	}

	public PortfolioGainHistory createPortfolioGainHistory(PortfolioGainHistory history) {
		Long totalGain = calculateTotalGain();
		Long dailyGain = calculateDailyGain(history);
		Long currentValuation = calculateTotalCurrentValuation();
		Long cash = calculateBalance();
		return PortfolioGainHistory.builder()
			.totalGain(totalGain)
			.dailyGain(dailyGain)
			.currentValuation(currentValuation)
			.cash(cash)
			.portfolio(this)
			.build();
	}

	public List<PortfolioHolding> changeCurrentPriceFromHoldings(CurrentPriceManager manager) {
		List<PortfolioHolding> result = new ArrayList<>();
		for (PortfolioHolding portfolioHolding : portfolioHoldings) {
			String tickerSymbol = portfolioHolding.getStock().getTickerSymbol();
			if (manager.hasCurrentPrice(tickerSymbol)) {
				portfolioHolding.changeCurrentPrice(manager.getCurrentPrice(tickerSymbol));
				result.add(portfolioHolding);
			}
		}
		return result;
	}

	// 포트폴리오 모든 종목들에 주식 현재가 적용
	public void applyCurrentPriceAllHoldingsBy(CurrentPriceManager manager) {
		for (PortfolioHolding portfolioHolding : portfolioHoldings) {
			portfolioHolding.applyCurrentPrice(manager);
		}
	}

	// 목표 수익률 = ((목표 수익 금액 - 예산) / 예산) * 100
	public Integer calculateTargetReturnRate() {
		return (int)(((targetGain.doubleValue() - budget.doubleValue()) / budget.doubleValue()) * 100);
	}

	// 총 자산 = 잔고 + 평가금액 합계
	public Long calculateTotalAsset() {
		return calculateBalance() + calculateTotalCurrentValuation();
	}

	// 목표수익금액 알림 변경
	public void changeTargetGainNotification(Boolean isActive) {
		this.targetGainIsActive = isActive;
	}

	// 최대손실금액의 알림 변경
	public void changeMaximumLossNotification(Boolean isActive) {
		this.maximumIsActive = isActive;
	}

	// 포트폴리오가 목표수익금액에 도달했는지 검사
	public boolean reachedTargetGain() {
		return budget + calculateTotalGain() >= targetGain;
	}

	// 포트폴리오가 최대손실금액에 도달했는지 검사
	public boolean reachedMaximumLoss() {
		return budget + calculateTotalGain() <= maximumLoss;
	}

	// 파이 차트 생성
	public List<PortfolioPieChartItem> createPieChart() {
		List<PortfolioPieChartItem> stocks = portfolioHoldings.stream()
			.map(portfolioHolding -> portfolioHolding.createPieChartItem(calculateWeightBy(portfolioHolding)))
			.collect(Collectors.toList());
		PortfolioPieChartItem cash = PortfolioPieChartItem.cash(calculateCashWeight(), calculateBalance());

		List<PortfolioPieChartItem> result = new ArrayList<>(stocks);
		result.add(cash);
		return result;
	}

	// 현금 비중 계산, 현금 비중 = 잔고 / 총자산
	public Double calculateCashWeight() {
		return calculateBalance().doubleValue() / calculateTotalAsset().doubleValue() * 100;
	}

	// 포트폴리오 종목 비중 계산, 종목 비중 = 종목 평가 금액 / 총자산
	private Double calculateWeightBy(PortfolioHolding holding) {
		return holding.calculateWeightBy(calculateTotalAsset().doubleValue());
	}

	private Double calculateWeightBy(Double currentValuation) {
		return currentValuation / calculateTotalAsset().doubleValue() * 100;
	}

	// 배당금 차트 생성
	public List<PortfolioDividendChartItem> createDividendChart(LocalDate currentLocalDate) {
		Map<Integer, Long> totalDividendMap = portfolioHoldings.stream()
			.flatMap(
				portfolioHolding -> portfolioHolding.createMonthlyDividendMap(currentLocalDate).entrySet().stream())
			.collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingLong(Map.Entry::getValue)));

		return totalDividendMap.entrySet().stream()
			.map(entry -> new PortfolioDividendChartItem(entry.getKey(), entry.getValue()))
			.collect(Collectors.toList());
	}

	public List<PortfolioSectorChartItem> createSectorChart() {
		return calculateSectorCurrentValuationMap().entrySet().stream()
			.map(mappingSectorChartItem())
			.collect(Collectors.toList());
	}

	private Map<String, List<Long>> calculateSectorCurrentValuationMap() {
		Map<String, List<Long>> sectorCurrentValuationMap = portfolioHoldings.stream()
			.collect(Collectors.groupingBy(portfolioHolding -> portfolioHolding.getStock().getSector(),
				Collectors.mapping(PortfolioHolding::calculateCurrentValuation, Collectors.toList())));
		// 섹션 차트에 현금 추가
		sectorCurrentValuationMap.put("현금", List.of(calculateBalance()));
		return sectorCurrentValuationMap;
	}

	private Function<Map.Entry<String, List<Long>>, PortfolioSectorChartItem> mappingSectorChartItem() {
		return entry -> {
			Double currentValuation = entry.getValue().stream().mapToDouble(Double::valueOf).sum();
			Double weight = calculateWeightBy(currentValuation);
			return new PortfolioSectorChartItem(entry.getKey(), weight);
		};
	}

	public boolean isSameName(Portfolio changePortfolio) {
		return this.name.equals(changePortfolio.name);
	}

	// 예산이 0원인지 검사합니다.
	public boolean isBudgetEmpty() {
		return this.budget == 0;
	}
}
