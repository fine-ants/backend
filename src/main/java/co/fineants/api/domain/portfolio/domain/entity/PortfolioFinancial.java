package co.fineants.api.domain.portfolio.domain.entity;

import java.util.List;

import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.MoneyConverter;
import co.fineants.api.domain.common.money.RateDivision;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.global.errors.errorcode.PortfolioErrorCode;
import co.fineants.api.global.errors.exception.portfolio.IllegalPortfolioFinancialArgumentException;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
public class PortfolioFinancial {
	@Convert(converter = MoneyConverter.class)
	@Column(precision = 19, nullable = false)
	private Money budget;
	@Convert(converter = MoneyConverter.class)
	@Column(precision = 19, nullable = false)
	private Money targetGain;
	@Convert(converter = MoneyConverter.class)
	@Column(precision = 19, nullable = false)
	private Money maximumLoss;

	private PortfolioFinancial(Money budget, Money targetGain, Money maximumLoss) {
		validateArguments(budget, targetGain, maximumLoss);
		this.budget = budget;
		this.targetGain = targetGain;
		this.maximumLoss = maximumLoss;
	}

	private void validateArguments(Money budget, Money targetGain, Money maximumLoss) {
		if (budget.isZero()) {
			return;
		}
		// 음수가 아닌지 검증
		for (Money money : List.of(budget, targetGain, maximumLoss)) {
			if (isNegative(money)) {
				throw new IllegalPortfolioFinancialArgumentException(budget, targetGain, maximumLoss,
					PortfolioErrorCode.INVALID_PORTFOLIO_FINANCIAL_INFO);
			}
		}
		// 목표 수익 금액이 0원이 아닌 상태에서 예산 보다 큰지 검증
		if (!targetGain.isZero() && budget.compareTo(targetGain) >= 0) {
			throw new IllegalPortfolioFinancialArgumentException(budget, targetGain, maximumLoss,
				PortfolioErrorCode.TARGET_GAIN_LOSS_IS_EQUAL_LESS_THAN_BUDGET);
		}
		// 최대 손실 금액이 예산 보다 작은지 검증
		if (!maximumLoss.isZero() && budget.compareTo(maximumLoss) <= 0) {
			throw new IllegalPortfolioFinancialArgumentException(budget, targetGain, maximumLoss,
				PortfolioErrorCode.MAXIMUM_LOSS_IS_EQUAL_GREATER_THAN_BUDGET);
		}
	}

	private boolean isNegative(Money money) {
		return money.compareTo(Money.zero()) < 0;
	}

	/**
	 * PortfolioFinancial 객체를 생성하여 반환한다.
	 *
	 * @param budget 예산
	 * @param targetGain 목표수익금액
	 * @param maximumLoss 최대손실금액
	 * @return PortfolioFinancial 객체
	 * @throws IllegalPortfolioFinancialArgumentException 예산, 목표수익금액, 최대손실금액 조합이 유효하지 않는 경우 예외 발생, 입력 정보가 음수인 경우 에외 발생
	 */
	public static PortfolioFinancial of(Money budget, Money targetGain, Money maximumLoss) {
		return new PortfolioFinancial(budget, targetGain, maximumLoss);
	}

	/**
	 * 포트폴리오 금융 정보인 예산, 목표수익금액, 최대손실금액을 변경한다.
	 *
	 * @param financial 변경하고자 하는 포트폴리오 금융 정보
	 */
	public void change(PortfolioFinancial financial) {
		this.budget = financial.budget;
		this.targetGain = financial.targetGain;
		this.maximumLoss = financial.maximumLoss;
	}

	public Expression calBalance(Expression totalInvestment) {
		return budget.minus(totalInvestment);
	}

	public RateDivision calMaximumLossRate() {
		return budget.minus(maximumLoss).divide(budget);
	}

	public RateDivision calMaximumLossRate(PortfolioCalculator calculator) {
		return calculator.calMaximumLossRate(budget, maximumLoss);
	}

	public RateDivision calTargetGainRate() {
		return targetGain.minus(budget).divide(budget);
	}

	@Override
	public String toString() {
		return String.format("(budget=%s, targetGain=%s, maximumLoss=%s)", budget, targetGain, maximumLoss);
	}

	public boolean reachedTargetGain(Expression totalCurrentValuation) {
		return targetGain.compareTo(totalCurrentValuation) <= 0;
	}

	public boolean reachedMaximumLoss(Expression totalGain) {
		return maximumLoss.compareTo(totalGain) >= 0;
	}

	public boolean isTargetGainZero() {
		return targetGain.isZero();
	}

	public boolean isMaximumLossZero() {
		return maximumLoss.isZero();
	}
}
