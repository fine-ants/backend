package codesquad.fineants.domain.portfolio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.BatchSize;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.MoneyConverter;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.notification.type.NotificationType;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.spring.api.common.errors.errorcode.PortfolioErrorCode;
import codesquad.fineants.spring.api.common.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.common.errors.exception.FineAntsException;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.notification.manager.NotificationSentManager;
import codesquad.fineants.spring.api.notification.response.NotifyMessage;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioDividendChartItem;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioPieChartItem;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioSectorChartItem;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@NamedEntityGraphs({
	@NamedEntityGraph(name = "Portfolio.withAll", attributeNodes = {
		@NamedAttributeNode("member"),
		@NamedAttributeNode(value = "portfolioHoldings", subgraph = "portfolioHoldings")
	}, subgraphs = {
		@NamedSubgraph(name = "portfolioHoldings", attributeNodes = {
			@NamedAttributeNode("stock")
		})})
})
@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"member", "portfolioHoldings"})
@Entity
@Table(name = "portfolio", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"name", "member_id"})
})
public class Portfolio extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String securitiesFirm;
	@Convert(converter = MoneyConverter.class)
	@Column(precision = 19, nullable = false)
	private Money budget;
	@Convert(converter = MoneyConverter.class)
	@Column(precision = 19, nullable = false)
	private Money targetGain;
	@Convert(converter = MoneyConverter.class)
	@Column(precision = 19, nullable = false)
	private Money maximumLoss;
	private Boolean targetGainIsActive;
	private Boolean maximumLossIsActive;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@BatchSize(size = 1000)
	@OneToMany(mappedBy = "portfolio")
	private final List<PortfolioHolding> portfolioHoldings = new ArrayList<>();

	private Portfolio(String name, String securitiesFirm, Money budget, Money targetGain, Money maximumLoss,
		Boolean targetGainIsActive, Boolean maximumLossIsActive, Member member) {
		this(null, name, securitiesFirm, budget, targetGain, maximumLoss, targetGainIsActive, maximumLossIsActive,
			member);
	}

	@Builder
	public Portfolio(Long id, String name, String securitiesFirm, Money budget, Money targetGain, Money maximumLoss,
		Boolean targetGainIsActive, Boolean maximumLossIsActive, Member member) {
		validateBudget(budget, targetGain, maximumLoss);
		this.id = id;
		this.name = name;
		this.securitiesFirm = securitiesFirm;
		this.budget = budget;
		this.targetGain = targetGain;
		this.maximumLoss = maximumLoss;
		this.targetGainIsActive = targetGainIsActive;
		this.maximumLossIsActive = maximumLossIsActive;
		this.member = member;
	}

	private void validateBudget(Money budget, Money targetGain, Money maximumLoss) {
		if (budget.isZero()) {
			return;
		}
		// 목표 수익 금액이 0원이 아닌 상태에서 예산 보다 큰지 검증
		if (!targetGain.isZero() && budget.compareTo(targetGain) >= 0) {
			throw new FineAntsException(PortfolioErrorCode.TARGET_GAIN_LOSS_IS_EQUAL_LESS_THAN_BUDGET);
		}
		// 최대 손실 금액이 예산 보다 작은지 검증
		if (!maximumLoss.isZero() && budget.compareTo(maximumLoss) <= 0) {
			throw new BadRequestException(PortfolioErrorCode.MAXIMUM_LOSS_IS_EQUAL_GREATER_THAN_BUDGET);
		}
	}

	public static Portfolio noActive(String name, String securitiesFirm, Money budget, Money targetGain,
		Money maximumLoss, Member member) {
		return new Portfolio(name, securitiesFirm, budget, targetGain, maximumLoss, false, false, member);
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

	// 포트폴리오 총 손익율 = (포트폴리오 총 손익 / 포트폴리오 총 투자 금액) * 100%
	public double calculateTotalGainRate() {
		Money totalInvestmentAmount = calculateTotalInvestmentAmount();
		if (totalInvestmentAmount.isZero()) {
			return 0;
		}
		return calculateTotalGain().divide(totalInvestmentAmount).toPercentage();
	}

	// 포트폴리오 총 손익 = 모든 종목 총 손익의 합계
	// 종목 총 손익 = (종목 현재가 - 종목 평균 매입가) * 개수
	// 종목 평균 매입가 = 종목의 총 투자 금액 / 총 주식 개수
	public Money calculateTotalGain() {
		return portfolioHoldings.stream()
			.map(PortfolioHolding::calculateTotalGain)
			.reduce(Money.zero(), Money::add);
	}

	// 포트폴리오 총 투자 금액 = 각 종목들의 구입가들의 합계
	public Money calculateTotalInvestmentAmount() {
		return portfolioHoldings.stream()
			.map(PortfolioHolding::calculateTotalInvestmentAmount)
			.reduce(Money.zero(), Money::add);
	}

	// 포트폴리오 당일 손익 = 모든 종목들의 평가 금액 합계 - 이전일 포트폴리오의 모든 종목들의 평가 금액 합계
	// 단, 이전일의 포트포릴오의 모든 종목들의 평가금액이 없는 경우 총 투자금액으로 뺀다
	public Money calculateDailyGain(PortfolioGainHistory previousHistory) {
		Money previousCurrentValuation = previousHistory.getCurrentValuation();
		if (previousCurrentValuation.isZero()) {
			return calculateTotalCurrentValuation().subtract(calculateTotalInvestmentAmount());
		}
		return calculateTotalCurrentValuation().subtract(previousCurrentValuation);
	}

	// 포트폴리오 평가 금액(현재 가치) = 모든 종목들의 평가금액 합계
	public Money calculateTotalCurrentValuation() {
		return portfolioHoldings.stream()
			.map(PortfolioHolding::calculateCurrentValuation)
			.reduce(Money.zero(), Money::add);
	}

	// 포트폴리오 당일 손익율 = (당일 포트폴리오 가치 총합 - 이전 포트폴리오 가치 총합) / 이전 포트폴리오 가치 총합
	// 단, 이전 포트폴리오가 없는 경우 ((당일 포트폴리오 가치 총합 - 당일 포트폴리오 총 투자 금액) / 당일 포트폴리오 총 투자 금액) * 100%
	public double calculateDailyGainRate(PortfolioGainHistory prevHistory) {
		Money prevCurrentValuation = prevHistory.getCurrentValuation();
		Money currentValuation = calculateTotalCurrentValuation();
		if (prevCurrentValuation.isZero()) {
			Money totalInvestmentAmount = calculateTotalInvestmentAmount();

			return currentValuation.subtract(totalInvestmentAmount).divide(totalInvestmentAmount).toPercentage();
		}
		return currentValuation.subtract(prevCurrentValuation).divide(prevCurrentValuation).toPercentage();
	}

	// 포트폴리오 당월 예상 배당금 = 각 종목들에 해당월의 배당금 합계
	public Money calculateCurrentMonthDividend() {
		return portfolioHoldings.stream()
			.map(PortfolioHolding::calculateCurrentMonthDividend)
			.reduce(Money.zero(), Money::add);
	}

	public Count getNumberOfShares() {
		return Count.from(portfolioHoldings.size());
	}

	// 잔고 = 예산 - 총 투자 금액
	public Money calculateBalance() {
		return budget.subtract(calculateTotalInvestmentAmount());
	}

	// 총 연간 배당금 = 각 종목들의 연배당금의 합계
	public Money calculateAnnualDividend() {
		return portfolioHoldings.stream()
			.map(portfolioHolding -> portfolioHolding.createMonthlyDividendMap(LocalDate.now()))
			.map(map -> map.values().stream()
				.reduce(Money.zero(), Money::add))
			.reduce(Money.zero(), Money::add);
	}

	// 총 연간배당율 = 모든 종목들의 연 배당금 합계 / 모든 종목들의 총 가치의 합계) * 100
	public double calculateAnnualDividendYield() {
		Money currentValuation = calculateTotalCurrentValuation();
		Money totalAnnualDividend = calculateAnnualDividend();
		return totalAnnualDividend.divide(currentValuation).toPercentage();
	}

	// 최대손실율 = ((예산 - 최대손실금액) / 예산) * 100
	public double calculateMaximumLossRate() {
		return budget.subtract(maximumLoss).divide(budget).toPercentage();
	}

	// 투자대비 연간 배당율 = 포트폴리오 총 연배당금 / 포트폴리오 투자금액 * 100
	public double calculateAnnualInvestmentDividendYield() {
		Money investment = calculateTotalInvestmentAmount();
		Money dividend = calculateAnnualDividend();
		return dividend.divide(investment).toPercentage();
	}

	public PortfolioGainHistory createPortfolioGainHistory(PortfolioGainHistory history) {
		Money totalGain = calculateTotalGain();
		Money dailyGain = calculateDailyGain(history);
		Money currentValuation = calculateTotalCurrentValuation();
		Money cash = calculateBalance();
		return PortfolioGainHistory.builder()
			.totalGain(totalGain)
			.dailyGain(dailyGain)
			.currentValuation(currentValuation)
			.cash(cash)
			.portfolio(this)
			.build();
	}

	// 포트폴리오 모든 종목들에 주식 현재가 적용
	public void applyCurrentPriceAllHoldingsBy(CurrentPriceManager manager) {
		for (PortfolioHolding portfolioHolding : portfolioHoldings) {
			portfolioHolding.applyCurrentPrice(manager);
			log.debug("portfolioHolding : {}, purchaseHistory : {}", portfolioHolding,
				portfolioHolding.getPurchaseHistory());
		}
	}

	// 목표 수익률 = ((목표 수익 금액 - 예산) / 예산) * 100
	public double calculateTargetReturnRate() {
		return targetGain.subtract(budget).divide(budget).toPercentage();
	}

	// 총 자산 = 잔고 + 평가금액 합계
	public Money calculateTotalAsset() {
		return calculateBalance().add(calculateTotalCurrentValuation());
	}

	// 목표수익금액 알림 변경
	public void changeTargetGainNotification(Boolean isActive) {
		this.targetGainIsActive = isActive;
	}

	// 최대손실금액의 알림 변경
	public void changeMaximumLossNotification(Boolean isActive) {
		this.maximumLossIsActive = isActive;
	}

	// 포트폴리오가 목표수익금액에 도달했는지 검사 (평가금액이 목표수익금액보다 같거나 큰 경우)
	public boolean reachedTargetGain() {
		Money currentValuation = calculateTotalCurrentValuation();
		log.debug("reachedTargetGain currentValuation={}, targetGain={}", currentValuation, targetGain);
		return currentValuation.compareTo(targetGain) >= 0;
	}

	// 포트폴리오가 최대손실금액에 도달했는지 검사 (예산 + 총손익이 최대손실금액보다 작은 경우)
	public boolean reachedMaximumLoss() {
		Money totalGain = calculateTotalGain();
		log.debug("reachedTargetGain totalGain={}", totalGain);
		return budget.add(totalGain).compareTo(maximumLoss) <= 0;
	}

	// 파이 차트 생성
	public List<PortfolioPieChartItem> createPieChart() {
		List<PortfolioPieChartItem> stocks = portfolioHoldings.stream()
			.map(portfolioHolding -> portfolioHolding.createPieChartItem(calculateWeightBy(portfolioHolding)))
			.collect(Collectors.toList());
		PortfolioPieChartItem cash = PortfolioPieChartItem.cash(calculateCashWeight(), calculateBalance());

		List<PortfolioPieChartItem> result = new ArrayList<>(stocks);
		result.add(cash);

		// 정렬
		// 평가금액(valuation) 기준 내림차순
		// 총손익(totalGain) 기준 내림차순
		result.sort(((Comparator<PortfolioPieChartItem>)(o1, o2) -> o2.getValuation().compareTo(o1.getValuation()))
			.thenComparing((o1, o2) -> o2.getTotalGain().compareTo(o1.getTotalGain())));
		return result;
	}

	// 현금 비중 계산, 현금 비중 = 잔고 / 총자산
	public double calculateCashWeight() {
		Money balance = calculateBalance();
		Money totalAsset = calculateTotalAsset();
		return balance.divide(totalAsset).toPercentage();
	}

	// 포트폴리오 종목 비중 계산, 종목 비중 = 종목 평가 금액 / 총자산
	private Double calculateWeightBy(PortfolioHolding holding) {
		return holding.calculateWeightBy(calculateTotalAsset());
	}

	private double calculateWeightBy(Money currentValuation) {
		Money totalAsset = calculateTotalAsset();
		return currentValuation.divide(totalAsset).toPercentage();
	}

	// 배당금 차트 생성
	public List<PortfolioDividendChartItem> createDividendChart(LocalDate currentLocalDate) {
		Map<Integer, Money> totalDividendMap = portfolioHoldings.stream()
			.flatMap(holding ->
				holding.createMonthlyDividendMap(currentLocalDate).entrySet().stream()
			)
			.collect(Collectors.groupingBy(Map.Entry::getKey,
				Collectors.reducing(Money.zero(), Map.Entry::getValue, Money::add))
			);
		return totalDividendMap.entrySet().stream()
			.map(entry -> PortfolioDividendChartItem.create(entry.getKey(), entry.getValue()))
			.collect(Collectors.toList());
	}

	public List<PortfolioSectorChartItem> createSectorChart() {
		return calculateSectorCurrentValuationMap().entrySet().stream()
			.map(mappingSectorChartItem())
			.sorted(
				((Comparator<PortfolioSectorChartItem>)(o1, o2) -> Double.compare(o2.getSectorWeight(),
					o1.getSectorWeight()))
					.thenComparing(PortfolioSectorChartItem::getSector))
			.collect(Collectors.toList());
	}

	private Map<String, List<Money>> calculateSectorCurrentValuationMap() {
		Map<String, List<Money>> sectorCurrentValuationMap = portfolioHoldings.stream()
			.collect(Collectors.groupingBy(portfolioHolding -> portfolioHolding.getStock().getSector(),
				Collectors.mapping(PortfolioHolding::calculateCurrentValuation, Collectors.toList())));
		// 섹션 차트에 현금 추가
		sectorCurrentValuationMap.put("현금", List.of(calculateBalance()));
		return sectorCurrentValuationMap;
	}

	private Function<Map.Entry<String, List<Money>>, PortfolioSectorChartItem> mappingSectorChartItem() {
		return entry -> {
			Money currentValuation = entry.getValue().stream()
				.reduce(Money.zero(), Money::add);
			double weight = calculateWeightBy(currentValuation);
			return PortfolioSectorChartItem.create(entry.getKey(), weight);
		};
	}

	public boolean isSameName(Portfolio changePortfolio) {
		return this.name.equals(changePortfolio.name);
	}

	// 매입 이력을 포트폴리오에 추가시 현금이 충분한지 판단
	public boolean isCashSufficientForPurchase(Money money) {
		return calculateTotalInvestmentAmount().add(money).compareTo(budget) > 0;
	}

	public boolean isSameTargetGainActive(boolean active) {
		return this.targetGainIsActive == active;
	}

	public boolean isSameMaxLossActive(boolean active) {
		return this.maximumLossIsActive == active;
	}

	public boolean hasTargetGainSentHistory(NotificationSentManager manager) {
		return manager.hasTargetGainSendHistory(id);
	}

	public boolean hasMaxLossSentHistory(NotificationSentManager manager) {
		return manager.hasMaxLossSendHistory(id);
	}

	public NotifyMessage getTargetGainMessage(String token) {
		String title = "포트폴리오";
		String content = String.format("%s의 목표 수익률을 달성했습니다", name);
		NotificationType type = NotificationType.PORTFOLIO_TARGET_GAIN;
		String referenceId = id.toString();
		Long memberId = member.getId();
		String link = "/portfolio/" + referenceId;
		return NotifyMessage.portfolio(
			title,
			content,
			type,
			referenceId,
			memberId,
			token,
			link,
			name
		);
	}

	public NotifyMessage getMaxLossMessage(String token) {
		String title = "포트폴리오";
		String content = String.format("%s이(가) 최대 손실율에 도달했습니다", name);
		NotificationType type = NotificationType.PORTFOLIO_MAX_LOSS;
		String referenceId = id.toString();
		Long memberId = member.getId();
		String link = "/portfolio/" + referenceId;
		return NotifyMessage.portfolio(
			title,
			content,
			type,
			referenceId,
			memberId,
			token,
			link,
			name
		);
	}

	public Boolean isTargetGainSet() {
		return !targetGain.isZero();
	}

	public Boolean isMaximumLossSet() {
		return !maximumLoss.isZero();
	}
}
