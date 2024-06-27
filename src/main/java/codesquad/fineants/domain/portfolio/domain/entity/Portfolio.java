package codesquad.fineants.domain.portfolio.domain.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hibernate.annotations.BatchSize;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Bank;
import codesquad.fineants.domain.common.money.Currency;
import codesquad.fineants.domain.common.money.Expression;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.MoneyConverter;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.common.money.RateDivision;
import codesquad.fineants.domain.common.notification.Notifiable;
import codesquad.fineants.domain.gainhistory.domain.entity.PortfolioGainHistory;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioDividendChartItem;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioPieChartItem;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioSectorChartItem;
import codesquad.fineants.domain.holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.kis.repository.CurrentPriceRepository;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.notification.domain.dto.response.NotifyMessage;
import codesquad.fineants.domain.notification.domain.entity.type.NotificationType;
import codesquad.fineants.domain.notification.repository.NotificationSentRepository;
import codesquad.fineants.domain.notificationpreference.domain.entity.NotificationPreference;
import codesquad.fineants.global.errors.errorcode.PortfolioErrorCode;
import codesquad.fineants.global.errors.exception.BadRequestException;
import codesquad.fineants.global.errors.exception.FineAntsException;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedEntityGraphs;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
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
public class Portfolio extends BaseEntity implements Notifiable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
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

	private Portfolio(Long id, String name, String securitiesFirm, Money budget, Money targetGain, Money maximumLoss,
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

	public static Portfolio active(String name, String securitiesFirm, Money budget, Money targetGain,
		Money maximumLoss, Member member) {
		return active(null, name, securitiesFirm, budget, targetGain, maximumLoss, member);
	}

	public static Portfolio active(Long id, String name, String securitiesFirm, Money budget, Money targetGain,
		Money maximumLoss, Member member) {
		return new Portfolio(id, name, securitiesFirm, budget, targetGain, maximumLoss, true, true, member);
	}

	public static Portfolio noActive(String name, String securitiesFirm, Money budget, Money targetGain,
		Money maximumLoss, Member member) {
		return new Portfolio(null, name, securitiesFirm, budget, targetGain, maximumLoss, false, false, member);
	}

	//== 연관관계 메소드 ==//
	public void addHolding(PortfolioHolding portFolioHolding) {
		if (!portfolioHoldings.contains(portFolioHolding)) {
			portfolioHoldings.add(portFolioHolding);
			portFolioHolding.setPortfolio(this);
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

	/**
	 * 포트폴리오 총 손익율
	 * 포트폴리오 총 손익 / 포트폴리오 총 투자 금액 * 100
	 * @return 포트폴리오 총 손익율의 백분율
	 */
	public RateDivision calculateTotalGainRate() {
		Expression amount = calculateTotalInvestmentAmount();
		return calculateTotalGain().divide(amount);
	}

	/**
	 * 포트폴리오 총 손익
	 * 모든 종목(PortfolioHolding)의 총 손익 합
	 * @return 포트폴리오 총 손익
	 */
	public Expression calculateTotalGain() {
		return portfolioHoldings.stream()
			.map(PortfolioHolding::calculateTotalGain)
			.reduce(Money.wonZero(), Expression::plus);
	}

	/**
	 * 포트폴리오 총 투자 금액
	 * 각 종목들의 총 투자 금액 합계
	 * @return 포트폴리오 총 투자 금액
	 */
	public Expression calculateTotalInvestmentAmount() {
		return portfolioHoldings.stream()
			.map(PortfolioHolding::calculateTotalInvestmentAmount)
			.reduce(Money.wonZero(), Expression::plus);
	}

	/**
	 * 포트폴리오 당일 손익
	 * 각 종목들의 평가 금액 합계 - 이전 포트폴리오 내역의 각 종목들의 평가 금액 합계
	 * 단, 이전일의 포트포릴오 내역의 각 종목들의 평가 금액이 없는 경우 총 투자금액으로 뺀다
	 * @param history 이전 포트폴리오 내역
	 * @return 포트폴리오 당일 손익
	 */
	public Expression calculateDailyGain(PortfolioGainHistory history) {
		Expression previousCurrentValuation = history.getCurrentValuation();
		Money won = Bank.getInstance().toWon(previousCurrentValuation);
		if (won.isZero()) {
			return calculateTotalCurrentValuation().minus(calculateTotalInvestmentAmount());
		}
		return calculateTotalCurrentValuation().minus(previousCurrentValuation);
	}

	// 포트폴리오 평가 금액(현재 가치) = 모든 종목들의 평가금액 합계
	public Expression calculateTotalCurrentValuation() {
		return portfolioHoldings.stream()
			.map(PortfolioHolding::calculateCurrentValuation)
			.reduce(Money.wonZero(), Expression::plus);
	}

	// 포트폴리오 당일 손익율 = (당일 포트폴리오 가치 총합 - 이전 포트폴리오 가치 총합) / 이전 포트폴리오 가치 총합
	// 단, 이전 포트폴리오가 없는 경우 ((당일 포트폴리오 가치 총합 - 당일 포트폴리오 총 투자 금액) / 당일 포트폴리오 총 투자 금액) * 100%
	public RateDivision calculateDailyGainRate(PortfolioGainHistory prevHistory) {
		Money prevCurrentValuation = prevHistory.getCurrentValuation();
		Expression currentValuation = calculateTotalCurrentValuation();
		if (prevCurrentValuation.isZero()) {
			Expression amount = calculateTotalInvestmentAmount();
			return currentValuation.minus(amount)
				.divide(amount);
		}
		return currentValuation.minus(prevCurrentValuation)
			.divide(prevCurrentValuation);
	}

	// 포트폴리오 당월 예상 배당금 = 각 종목들에 해당월의 배당금 합계
	public Expression calculateCurrentMonthDividend() {
		return portfolioHoldings.stream()
			.map(PortfolioHolding::calculateCurrentMonthDividend)
			.reduce(Money.zero(), Expression::plus);
	}

	public Count getNumberOfShares() {
		return Count.from(portfolioHoldings.size());
	}

	// 잔고 = 예산 - 총 투자 금액
	public Expression calculateBalance() {
		return budget.minus(calculateTotalInvestmentAmount());
	}

	// 총 연간 배당금 = 각 종목들의 연배당금의 합계
	public Expression calculateAnnualDividend() {
		return portfolioHoldings.stream()
			.map(portfolioHolding -> portfolioHolding.createMonthlyDividendMap(LocalDate.now()))
			.map(map -> map.values().stream()
				.reduce(Money.zero(), Expression::plus))
			.reduce(Money.zero(), Expression::plus);
	}

	// 총 연간배당율 = 모든 종목들의 연 배당금 합계 / 모든 종목들의 총 가치의 합계) * 100
	public RateDivision calculateAnnualDividendYield() {
		Expression currentValuation = calculateTotalCurrentValuation();
		Expression totalAnnualDividend = calculateAnnualDividend();
		return totalAnnualDividend.divide(currentValuation);
	}

	// 최대손실율 = ((예산 - 최대손실금액) / 예산) * 100
	public RateDivision calculateMaximumLossRate() {
		return budget.minus(maximumLoss)
			.divide(budget);
	}

	// 투자대비 연간 배당율 = 포트폴리오 총 연배당금 / 포트폴리오 투자금액 * 100
	public RateDivision calculateAnnualInvestmentDividendYield() {
		Expression amount = calculateTotalInvestmentAmount();
		Expression dividend = calculateAnnualDividend();
		return dividend.divide(amount);
	}

	public PortfolioGainHistory createPortfolioGainHistory(PortfolioGainHistory history) {
		Bank bank = Bank.getInstance();
		Money totalGain = bank.toWon(calculateTotalGain());
		Money dailyGain = bank.toWon(calculateDailyGain(history));
		Money cash = bank.toWon(calculateBalance());
		Money currentValuation = bank.toWon(calculateTotalCurrentValuation());
		return PortfolioGainHistory.create(totalGain, dailyGain, cash, currentValuation, this);
	}

	// 포트폴리오 모든 종목들에 주식 현재가 적용
	public void applyCurrentPriceAllHoldingsBy(CurrentPriceRepository manager) {
		for (PortfolioHolding portfolioHolding : portfolioHoldings) {
			portfolioHolding.applyCurrentPrice(manager);
			log.debug("portfolioHolding : {}, purchaseHistory : {}", portfolioHolding,
				portfolioHolding.getPurchaseHistory());
		}
	}

	// 목표 수익률 = ((목표 수익 금액 - 예산) / 예산) * 100
	public RateDivision calculateTargetReturnRate() {
		return targetGain.minus(budget)
			.divide(budget);
	}

	// 총 자산 = 잔고 + 평가금액 합계
	public Expression calculateTotalAsset() {
		return calculateBalance().plus(calculateTotalCurrentValuation());
	}

	// 목표수익금액 알림 변경
	public void changeTargetGainNotification(Boolean isActive) {
		validateTargetGainNotification();
		this.targetGainIsActive = isActive;
	}

	/**
	 * 목표 수익 금액에 따른 변경이 가능한 상태인지 검증
	 * - 목표 수익 금액이 0원인 경우 변경 불가능
	 */
	private void validateTargetGainNotification() {
		if (targetGain.isZero()) {
			throw new FineAntsException(PortfolioErrorCode.TARGET_GAIN_IS_ZERO_WITH_NOTIFY_UPDATE);
		}
	}

	// 최대손실금액의 알림 변경
	public void changeMaximumLossNotification(Boolean isActive) {
		validateMaxLossNotification();
		this.maximumLossIsActive = isActive;
	}

	/**
	 * 최대 손실 금액에 따른 변경이 가능한 상태인지 검증
	 * - 최대 손실 금액이 0원인 경우 변경 불가능
	 */
	private void validateMaxLossNotification() {
		if (maximumLoss.isZero()) {
			throw new FineAntsException(PortfolioErrorCode.MAX_LOSS_IS_ZERO_WITH_NOTIFY_UPDATE);
		}
	}

	// 포트폴리오가 목표수익금액에 도달했는지 검사 (평가금액이 목표수익금액보다 같거나 큰 경우)
	public boolean reachedTargetGain() {
		Bank bank = Bank.getInstance();
		Money currentValuation = bank.toWon(calculateTotalCurrentValuation());
		log.debug("reachedTargetGain currentValuation={}, targetGain={}", currentValuation, targetGain);
		return currentValuation.compareTo(bank.toWon(targetGain)) >= 0;
	}

	// 포트폴리오가 최대손실금액에 도달했는지 검사 (예산 + 총손익이 최대손실금액보다 작은 경우)
	public boolean reachedMaximumLoss() {
		Bank bank = Bank.getInstance();
		Expression totalGain = calculateTotalGain();
		log.debug("reachedTargetGain totalGain={}", totalGain);
		Money amount = bank.toWon(budget.plus(totalGain));
		return amount.compareTo(bank.toWon(maximumLoss)) <= 0;
	}

	// 파이 차트 생성
	public List<PortfolioPieChartItem> createPieChart() {
		List<PortfolioPieChartItem> stocks = portfolioHoldings.stream()
			.map(portfolioHolding -> portfolioHolding.createPieChartItem(calculateWeightBy(portfolioHolding)))
			.collect(Collectors.toList());
		Bank bank = Bank.getInstance();
		Percentage weight = calculateCashWeight().toPercentage(bank, Currency.KRW);
		PortfolioPieChartItem cash = PortfolioPieChartItem.cash(weight, bank.toWon(calculateBalance()));

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
	public RateDivision calculateCashWeight() {
		Expression balance = calculateBalance();
		Expression totalAsset = calculateTotalAsset();
		return balance.divide(totalAsset);
	}

	// 포트폴리오 종목 비중 계산, 종목 비중 = 종목 평가 금액 / 총자산
	private RateDivision calculateWeightBy(PortfolioHolding holding) {
		return holding.calculateWeightBy(calculateTotalAsset());
	}

	private RateDivision calculateWeightBy(Money currentValuation) {
		Expression totalAsset = calculateTotalAsset();
		return currentValuation.divide(totalAsset);
	}

	// 배당금 차트 생성
	public List<PortfolioDividendChartItem> createDividendChart(LocalDate currentLocalDate) {
		Map<Integer, Expression> totalDividendMap = portfolioHoldings.stream()
			.flatMap(holding ->
				holding.createMonthlyDividendMap(currentLocalDate).entrySet().stream()
			)
			.collect(Collectors.groupingBy(Map.Entry::getKey,
				Collectors.reducing(Money.zero(), Map.Entry::getValue, Expression::plus))
			);
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		return totalDividendMap.entrySet().stream()
			.map(entry -> PortfolioDividendChartItem.create(entry.getKey(), entry.getValue().reduce(bank, to)))
			.collect(Collectors.toList());
	}

	public List<PortfolioSectorChartItem> createSectorChart() {
		return calculateSectorCurrentValuationMap().entrySet().stream()
			.map(mappingSectorChartItem())
			.sorted(PortfolioSectorChartItem::compareTo)
			.collect(Collectors.toList());
	}

	private Map<String, List<Expression>> calculateSectorCurrentValuationMap() {
		Map<String, List<Expression>> sectorCurrentValuationMap = portfolioHoldings.stream()
			.collect(Collectors.groupingBy(portfolioHolding -> portfolioHolding.getStock().getSector(),
				Collectors.mapping(PortfolioHolding::calculateCurrentValuation, Collectors.toList())));
		// 섹션 차트에 현금 추가
		sectorCurrentValuationMap.put("현금", List.of(calculateBalance()));
		return sectorCurrentValuationMap;
	}

	private Function<Map.Entry<String, List<Expression>>, PortfolioSectorChartItem> mappingSectorChartItem() {
		return entry -> {
			Expression currentValuation = entry.getValue().stream()
				.reduce(Money.wonZero(), Expression::plus);
			RateDivision weight = calculateWeightBy(Bank.getInstance().toWon(currentValuation));
			Percentage weightPercentage = weight.toPercentage(Bank.getInstance(), Currency.KRW);
			return PortfolioSectorChartItem.create(entry.getKey(), weightPercentage);
		};
	}

	public boolean isSameName(Portfolio changePortfolio) {
		return this.name.equals(changePortfolio.name);
	}

	// 매입 이력을 포트폴리오에 추가시 현금이 충분한지 판단
	public boolean isCashSufficientForPurchase(Money money) {
		Expression amount = calculateTotalInvestmentAmount().plus(money);
		return Bank.getInstance().toWon(amount).compareTo(budget) > 0;
	}

	public boolean isSameTargetGainActive(boolean active) {
		return this.targetGainIsActive == active;
	}

	public boolean isSameMaxLossActive(boolean active) {
		return this.maximumLossIsActive == active;
	}

	public boolean hasTargetGainSentHistory(NotificationSentRepository manager) {
		return manager.hasTargetGainSendHistory(id);
	}

	public boolean hasMaxLossSentHistory(NotificationSentRepository manager) {
		return manager.hasMaxLossSendHistory(id);
	}

	@Override
	public NotifyMessage createTargetGainMessageWith(String token) {
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

	@Override
	public NotifyMessage createMaxLossMessageWith(String token) {
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

	public boolean isExceedBudgetByPurchasedAmount(Expression amount) {
		Bank bank = Bank.getInstance();
		Expression investAmount = calculateTotalInvestmentAmount().plus(amount);
		return budget.compareTo(bank.toWon(investAmount)) < 0;
	}

	@Override
	public Long fetchMemberId() {
		return member.getId();
	}

	@Override
	public NotificationPreference getNotificationPreference() {
		return member.getNotificationPreference();
	}

	@Override
	public NotifyMessage getTargetPriceMessage(String token) {
		throw new UnsupportedOperationException("This method is not supported for Portfolio");
	}
}
