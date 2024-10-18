package co.fineants.api.domain.portfolio.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.hibernate.annotations.BatchSize;

import co.fineants.api.domain.BaseEntity;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.RateDivision;
import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioPieChartItem;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessage;
import co.fineants.api.domain.notification.domain.entity.type.NotificationType;
import co.fineants.api.domain.notification.repository.NotificationSentRepository;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.global.common.time.DefaultLocalDateTimeService;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.global.errors.errorcode.PortfolioErrorCode;
import co.fineants.api.global.errors.exception.portfolio.IllegalPortfolioFinancialStateException;
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
import lombok.Setter;
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
	@Setter
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

	//== Notifiable Interface 시작 ==//
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
			detail.name()
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
			detail.name()
		);
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
	//== Notifiable Interface 종료 ==//

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
	//== 연관 관계 메소드 ==//

	public void change(Portfolio changePortfolio) {
		this.detail.change(changePortfolio.detail);
		this.financial.change(changePortfolio.financial);
	}

	public boolean hasAuthorization(Long memberId) {
		return member.hasAuthorization(memberId);
	}

	public Count numberOfShares() {
		return Count.from(portfolioHoldings.size());
	}

	/**
	 * 포트폴리오의 목표수익금액 알림 설정을 변경.
	 * <p>
	 * 포트폴리오의 목표수익금액이 0원이 경우에는 활성화 알림이 변경되지 않는다.
	 * </p>
	 * @param active 변경하고자 하는 알림 설정, true: 알림 활성화, false: 알림 비활성화
	 * @throws IllegalPortfolioFinancialStateException 목표수익금액이 0원인 경우 예외 발생
	 */
	public void changeTargetGainNotification(Boolean active) {
		if (this.financial.isTargetGainZero()) {
			throw new IllegalPortfolioFinancialStateException(this.financial,
				PortfolioErrorCode.TARGET_GAIN_IS_ZERO_WITH_NOTIFY_UPDATE);
		}
		this.preference.changeTargetGain(active);
	}

	/**
	 * 포트폴리오의 최대손실금액의 활성화 알림을 변경.
	 *<p>
	 * 포트폴리오의 최대손실금액이 0원인 경우 활성화 알림이 변경되지 않는다
	 * </p>
	 * @param active 변경하고자 하는 활성화 알림 여부, true: 알림 활성화, false: 알림 비활성화
	 * @throws IllegalPortfolioFinancialStateException 최대손실금액이 0원인 경우 예외 발생
	 */
	public void changeMaximumLossNotification(Boolean active) {
		if (this.financial.isMaximumLossZero()) {
			throw new IllegalPortfolioFinancialStateException(this.financial,
				PortfolioErrorCode.MAX_LOSS_IS_ZERO_WITH_NOTIFY_UPDATE);
		}
		this.preference.changeMaximumLoss(active);
	}

	/**
	 * 포트폴리오의 잔고가 주문금액보다 충분한지 여부 검사.
	 *
	 * @param purchaseAmount 매입 이력 추가 위한 주문금액
	 * @param calculator 포트폴리오 계산기 객체
	 * @return true: 포트폴리오에 주문금액보다 같거나 큰 잔고를 가지고 있음, false: 구매금액보다 작은 잔고를 가지고 있음
	 */
	public boolean isCashSufficientForPurchase(Expression purchaseAmount, PortfolioCalculator calculator) {
		return calculator.calBalanceBy(this).compareTo(purchaseAmount) >= 0;
	}

	public List<PortfolioHolding> getPortfolioHoldings() {
		return Collections.unmodifiableList(portfolioHoldings);
	}

	//== PortfolioDetail 위임 메소드 시작 ==//
	public boolean equalName(Portfolio portfolio) {
		return detail.equalName(portfolio.detail);
	}

	public String securitiesFirm() {
		return detail.securitiesFirm();
	}

	public String name() {
		return detail.name();
	}
	//== PortfolioDetail 위임 메소드 종료 ==//

	//== PortfolioFinancial 위임 메소드 시작 ==//
	public Boolean isTargetGainSet() {
		return !this.financial.isTargetGainZero();
	}

	public Boolean isMaximumLossSet() {
		return !this.financial.isMaximumLossZero();
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
	//== PortfolioFinancial 위임 메소드 종료 ==//

	//== PortfolioNotificationPreference 위임 메서드 시작 ==//
	public boolean isSameTargetGainActive(boolean active) {
		return this.preference.isSameTargetGain(active);
	}

	public boolean isSameMaxLossActive(boolean active) {
		return this.preference.isSameMaxLoss(active);
	}

	public Boolean targetGainIsActive() {
		return preference.targetGainIsActive();
	}

	public Boolean maximumLossIsActive() {
		return preference.maximumLossIsActive();
	}
	//== PortfolioNotificationPreference 위임 메서드 종료 ==//

	public boolean hasTargetGainSentHistory(NotificationSentRepository manager) {
		return manager.hasTargetGainSendHistory(id);
	}

	//== Portfolio 계산 메서드 시작 ==//

	/**
	 * 포트폴리오의 총 손익을 계산 후 반환.
	 *
	 * @param calculator 포트폴리오 계산기 객체
	 * @return 포트폴리오 총 손익
	 * @throws IllegalStateException 포트폴리오 종목(PortfolioHolding)에 따른 현재가가 저장소에 없으면 예외 발생
	 */
	public Expression calTotalGain(PortfolioCalculator calculator) {
		return calculator.calTotalGainBy(Collections.unmodifiableList(portfolioHoldings));
	}

	public boolean hasMaxLossSentHistory(NotificationSentRepository manager) {
		return manager.hasMaxLossSendHistory(id);
	}

	/**
	 * 포트폴리오 총 투자 금액 계산 후 반환.
	 *
	 * @param calculator 계산기 객체
	 * @return 포트폴리오의 총 투자 금액
	 */
	public Expression calTotalInvestment(PortfolioCalculator calculator) {
		return calculator.calTotalInvestmentOfHolding(Collections.unmodifiableList(portfolioHoldings));
	}

	/**
	 * 포트폴리오의 총 손익율을 계산 후 반환.
	 *
	 * @param calculator 포트폴리오 계산기 객체
	 * @return 포트폴리오 총 손익율
	 * @throws java.util.NoSuchElementException 포트폴리오 종목(PortfolioHolding)에 따른 현재가가 저장소에 없으면 예외 발생
	 */
	public Expression calTotalGainRate(PortfolioCalculator calculator) {
		return calculator.calTotalGainRate(Collections.unmodifiableList(portfolioHoldings));
	}

	public Expression calBalance(PortfolioCalculator calculator) {
		Expression totalInvestment = calculator.calTotalInvestmentBy(this);
		return this.financial.calBalance(calculator, totalInvestment);
	}

	/**
	 * 포트폴리오 총 평가금액 계산 후 반환.
	 *
	 * @param calculator 포트폴리오 계산기 객체
	 * @return 포트폴리오의 총 평가금액
	 * @throws NoSuchElementException 포트폴리오 종목(PortfolioHolding)에 따른 현재가가 저장소에 없으면 예외 발생
	 */
	public Expression calTotalCurrentValuation(PortfolioCalculator calculator) {
		return calculator.calTotalCurrentValuation(Collections.unmodifiableList(portfolioHoldings));
	}

	public Expression calTotalAsset(PortfolioCalculator calculator) {
		Expression balance = calculator.calBalanceBy(this);
		Expression totalCurrentValuation = calculator.calTotalCurrentValuationBy(this);
		return calculator.calTotalAsset(balance, totalCurrentValuation);
	}

	public Expression calCurrentMonthDividend(PortfolioCalculator calculator) {
		return calculator.calCurrentMonthDividendBy(Collections.unmodifiableList(portfolioHoldings));
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

	public RateDivision calMaximumLossRate(PortfolioCalculator calculator) {
		return this.financial.calMaximumLossRate(calculator);
	}

	public RateDivision calTargetGainRate(PortfolioCalculator calculator) {
		return this.financial.calTargetGainRate(calculator);
	}

	public Map<String, List<Expression>> createSectorChart(PortfolioCalculator calculator) {
		Expression balance = calculator.calBalanceBy(this);
		return calculator.calSectorChart(Collections.unmodifiableList(portfolioHoldings), balance);
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
			.map(holding -> calculator.calPortfolioPieChartItemBy(holding, totalAsset))
			.toList();
	}

	public Map<Month, Expression> calTotalDividend(PortfolioCalculator calculator, LocalDate currentLocalDate) {
		return calculator.calTotalDividend(Collections.unmodifiableList(portfolioHoldings), currentLocalDate);
	}
	//== Portfolio 계산 메서드 시작 ==//

	public void setCreateAt(LocalDateTime createAt) {
		super.setCreateAt(createAt);
	}

	@Override
	public String toString() {
		return String.format("Portfolio(id=%d, detail=%s, memberNickname=%s)", id, detail, member.getNickname());
	}
}
