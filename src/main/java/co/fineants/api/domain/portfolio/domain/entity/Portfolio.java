package co.fineants.api.domain.portfolio.domain.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.annotations.BatchSize;

import co.fineants.api.domain.BaseEntity;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.common.money.RateDivision;
import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioDividendChartItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioPieChartItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioSectorChartItem;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessage;
import co.fineants.api.domain.notification.domain.entity.type.NotificationType;
import co.fineants.api.domain.notification.repository.NotificationSentRepository;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.global.common.time.DefaultLocalDateTimeService;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.global.errors.errorcode.PortfolioErrorCode;
import co.fineants.api.global.errors.exception.FineAntsException;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NamedEntityGraph(name = "Portfolio.withAll", attributeNodes = {
	@NamedAttributeNode("member"),
	@NamedAttributeNode(value = "portfolioHoldings", subgraph = "portfolioHoldings")
}, subgraphs = {
	@NamedSubgraph(name = "portfolioHoldings", attributeNodes = {
		@NamedAttributeNode("stock")
	})})
@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "portfolio", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"name", "member_id"})
})
@EqualsAndHashCode(of = {"detail", "member"}, callSuper = false)
public class Portfolio extends BaseEntity implements Notifiable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	@Embedded
	private PortfolioDetail detail;
	@Embedded
	private PortfolioFinancial financial;
	@Embedded
	private PortfolioNotificationPreference preference;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@BatchSize(size = 1000)
	@OneToMany(mappedBy = "portfolio")
	private final List<PortfolioHolding> portfolioHoldings = new ArrayList<>();

	@Transient
	private LocalDateTimeService localDateTimeService = new DefaultLocalDateTimeService();

	private Portfolio(Long id, PortfolioDetail detail, PortfolioFinancial financial,
		PortfolioNotificationPreference preference) {
		this.id = id;
		this.detail = detail;
		this.financial = financial;
		this.preference = preference;
	}

	/**
	 * 포트폴리오 알림 설정을 전부 활성화한 객체를 생성하여 반환한다.
	 *
	 * @param id 포트폴리오 식별번호
	 * @param detail 포트폴리오 상세 정보
	 * @param financial 포트폴리오 금융 정보
	 * @param member 포트폴리오를 소유한 회원 객체
	 * @return 포트폴리오 객체
	 */
	public static Portfolio allActive(Long id, PortfolioDetail detail, PortfolioFinancial financial, Member member) {
		PortfolioNotificationPreference preference = PortfolioNotificationPreference.allActive();
		Portfolio portfolio = new Portfolio(id, detail, financial, preference);
		portfolio.setMember(member);
		return portfolio;
	}

	/**
	 * 알림 설정이 전부 비활성화된 포트폴리오 객체를 생성하여 반환한다.
	 *
	 * @param detail 포트폴리오 상세 정보 객체
	 * @param financial 포트폴리오 금융 정보 객체
	 * @param member 포트폴리오를 소유한 회원 객체
	 * @return 생성한 포트폴리오 객체
	 */
	public static Portfolio allInActive(PortfolioDetail detail, PortfolioFinancial financial, Member member) {
		PortfolioNotificationPreference preference = PortfolioNotificationPreference.allInactive();
		Portfolio portfolio = new Portfolio(null, detail, financial, preference);
		portfolio.setMember(member);
		return portfolio;
	}

	//== 연관 관계 메소드 ==//
	public void addHolding(PortfolioHolding holding) {
		if (portfolioHoldings.contains(holding)) {
			return;
		}
		portfolioHoldings.add(holding);
		if (holding.getPortfolio() != this) {
			holding.setPortfolio(this);
		}
	}

	public void removeHolding(PortfolioHolding holding) {
		this.portfolioHoldings.remove(holding);
		holding.setPortfolio(null);
	}

	public void setMember(Member member) {
		this.member = member;
	}
	//== 연관 관계 편의 메소드 종료 ==//

	public void change(Portfolio changePortfolio) {
		this.detail.change(changePortfolio.detail);
		this.financial.change(changePortfolio.financial);
	}

	public boolean hasAuthorization(Long memberId) {
		return member.hasAuthorization(memberId);
	}

	public Count getNumberOfShares() {
		return Count.from(portfolioHoldings.size());
	}

	// 포트폴리오 모든 종목들에 주식 현재가 적용
	public void applyCurrentPriceAllHoldingsBy(CurrentPriceRedisRepository manager) {
		for (PortfolioHolding portfolioHolding : portfolioHoldings) {
			portfolioHolding.applyCurrentPrice(manager);
			log.debug("portfolioHolding : {}, purchaseHistory : {}", portfolioHolding,
				portfolioHolding.getPurchaseHistory());
		}
	}

	// 목표수익금액 알림 변경
	public void changeTargetGainNotification(Boolean isActive) {
		validateTargetGainNotification();
		this.preference.changeTargetGain(isActive);
	}

	/**
	 * 목표 수익 금액에 따른 변경이 가능한 상태인지 검증
	 * - 목표 수익 금액이 0원인 경우 변경 불가능
	 */
	private void validateTargetGainNotification() {
		if (this.financial.getTargetGain().isZero()) {
			throw new FineAntsException(PortfolioErrorCode.TARGET_GAIN_IS_ZERO_WITH_NOTIFY_UPDATE);
		}
	}

	// 최대손실금액의 알림 변경
	public void changeMaximumLossNotification(Boolean isActive) {
		validateMaxLossNotification();
		this.preference.changeMaximumLoss(isActive);
	}

	/**
	 * 최대 손실 금액에 따른 변경이 가능한 상태인지 검증
	 * - 최대 손실 금액이 0원인 경우 변경 불가능
	 */
	private void validateMaxLossNotification() {
		if (this.financial.getMaximumLoss().isZero()) {
			throw new FineAntsException(PortfolioErrorCode.MAX_LOSS_IS_ZERO_WITH_NOTIFY_UPDATE);
		}
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
			.toList();
	}

	public boolean equalName(Portfolio changePortfolio) {
		return detail.equalName(changePortfolio.detail);
	}

	// 매입 이력을 포트폴리오에 추가시 현금이 충분한지 판단
	public boolean isCashSufficientForPurchase(Expression purchaseAmount, PortfolioCalculator calculator) {
		return calculator.calBalanceBy(this).compareTo(purchaseAmount) >= 0;
	}

	public boolean isSameTargetGainActive(boolean active) {
		return this.preference.isSameTargetGain(active);
	}

	public boolean isSameMaxLossActive(boolean active) {
		return this.preference.isSameMaxLoss(active);
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
		String content = detail.getTargetGainReachMessage();
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
			detail.getName()
		);
	}

	@Override
	public NotifyMessage createMaxLossMessageWith(String token) {
		String title = "포트폴리오";
		String content = detail.getMaximumLossReachMessage();
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
			detail.getName()
		);
	}

	public Boolean isTargetGainSet() {
		return !this.financial.getTargetGain().isZero();
	}

	public Boolean isMaximumLossSet() {
		return !this.financial.getMaximumLoss().isZero();
	}

	public boolean isExceedBudgetByPurchasedAmount(Expression amount, Expression totalInvestment) {
		Bank bank = Bank.getInstance();
		Expression investAmount = totalInvestment.plus(amount);
		return this.financial.getBudget().compareTo(bank.toWon(investAmount)) < 0;
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

	public List<PortfolioHolding> getPortfolioHoldings() {
		return Collections.unmodifiableList(portfolioHoldings);
	}

	public void setLocalDateTimeService(LocalDateTimeService localDateTimeService) {
		this.localDateTimeService = localDateTimeService;
	}

	public String getSecuritiesFirm() {
		return detail.getSecuritiesFirm();
	}

	public String getName() {
		return detail.getName();
	}

	public Money getBudget() {
		return this.financial.getBudget();
	}

	public Money getTargetGain() {
		return this.financial.getTargetGain();
	}

	public Money getMaximumLoss() {
		return this.financial.getMaximumLoss();
	}

	public Boolean getTargetGainIsActive() {
		return preference.getTargetGainIsActive();
	}

	public Boolean getMaximumLossIsActive() {
		return preference.getMaximumLossIsActive();
	}

	@Override
	public String toString() {
		return String.format("Portfolio(id=%d, detail=%s, memberNickname=%s)", id, detail, member.getNickname());
	}

	public Expression calTotalGain(PortfolioCalculator calculator) {
		return calculator.calTotalGain(Collections.unmodifiableList(portfolioHoldings));
	}

	public Expression calTotalInvestment(PortfolioCalculator calculator) {
		return calculator.calTotalInvestment(Collections.unmodifiableList(portfolioHoldings));
	}

	public Expression calTotalGainRate(PortfolioCalculator calculator) {
		return calculator.calTotalGainRate(Collections.unmodifiableList(portfolioHoldings));
	}

	public Expression calBalance(Expression totalInvestment) {
		return this.financial.calBalance(totalInvestment);
	}

	public Expression calTotalCurrentValuation(PortfolioCalculator calculator) {
		return calculator.calTotalCurrentValuation(Collections.unmodifiableList(portfolioHoldings));
	}

	public Expression calTotalAsset(PortfolioCalculator calculator) {
		Expression balance = calculator.calBalanceBy(this);
		Expression totalCurrentValuation = calculator.calTotalCurrentValuationBy(this);
		return calculator.calTotalAsset(balance, totalCurrentValuation);
	}

	public Expression calCurrentMonthDividend(PortfolioCalculator calculator) {
		return calculator.calCurrentMonthDividend(Collections.unmodifiableList(portfolioHoldings));
	}

	public Expression calAnnualDividend(LocalDateTimeService dateTimeService, PortfolioCalculator calculator) {
		return calculator.calAnnualDividend(dateTimeService, Collections.unmodifiableList(portfolioHoldings));
	}

	public Expression calAnnualInvestmentDividendYield(LocalDateTimeService localDateTimeService,
		PortfolioCalculator calculator) {
		Expression annualDividend = calculator.calAnnualDividendBy(localDateTimeService, this);
		Expression totalInvestment = calculator.calTotalInvestmentBy(this);
		return calculator.calAnnualInvestmentDividendYield(annualDividend, totalInvestment);
	}

	public RateDivision calculateMaximumLossRate() {
		return this.financial.calMaximumLossRate();
	}

	public RateDivision calculateTargetReturnRate() {
		return this.financial.calTargetGainRate();
	}

	public List<PortfolioSectorChartItem> createSectorChart(PortfolioCalculator calculator) {
		Expression balance = calculator.calBalanceBy(this);
		Expression totalAsset = calculator.calTotalAssetBy(this);

		Map<String, List<Expression>> sector = portfolioHoldings.stream()
			.collect(Collectors.groupingBy(portfolioHolding -> portfolioHolding.getStock().getSector(),
				Collectors.mapping(PortfolioHolding::calculateCurrentValuation, Collectors.toList())));
		sector.put("현금", List.of(balance));

		return sector.entrySet().stream()
			.map(entry -> {
				Expression currentValuation = entry.getValue().stream()
					.reduce(Money.wonZero(), Expression::plus);

				Percentage weightPercentage = calculator.calCurrentValuationWeight(currentValuation, totalAsset)
					.toPercentage(Bank.getInstance(), Currency.KRW);
				return PortfolioSectorChartItem.create(entry.getKey(), weightPercentage);
			})
			.sorted(PortfolioSectorChartItem::compareTo)
			.toList();
	}

	/**
	 * 포트폴리오가 목표수익금액에 도달했는지 여부 검사.
	 * @param calculator 포트폴리오 계산기 객체
	 * @return true: 평가금액이 목표수익금액보다 같거나 큰 경우, false: 평가금액이 목표수익금액보다 작은 경우
	 */
	public boolean reachedTargetGain(PortfolioCalculator calculator) {
		Expression totalCurrentValuation = calculator.calTotalCurrentValuationBy(this);
		return this.financial.reachedTargetGain(totalCurrentValuation);
	}

	/**
	 * 포트폴리오가 최대손실금액에 도달했는지 여부 검사.
	 *
	 * @param calculator 포트폴리오 계산기 객체
	 * @return true: 총 손익이 최대손실금액보다 같거나 작은 경우, false: 총 손익이 최대손실금액보다 큰 경우
	 */
	public boolean reachedMaximumLoss(PortfolioCalculator calculator) {
		Expression totalGain = calculator.calTotalGainBy(this);
		return this.financial.reachedMaximumLoss(totalGain);
	}

	public List<PortfolioPieChartItem> calCurrentValuationWeights(PortfolioCalculator calculator) {
		Expression totalAsset = calculator.calTotalAssetBy(this);
		return portfolioHoldings.stream()
			.map(holding -> {
				RateDivision weight = calculator.calCurrentValuationWeightBy(holding, totalAsset);
				return holding.createPieChartItem(weight);
			}).toList();
	}
}
