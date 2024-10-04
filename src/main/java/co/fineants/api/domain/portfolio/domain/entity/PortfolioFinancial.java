package co.fineants.api.domain.portfolio.domain.entity;

import java.util.List;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.MoneyConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
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
				throwIllegalArgumentException(budget, targetGain, maximumLoss);
			}
		}
		// 목표 수익 금액이 0원이 아닌 상태에서 예산 보다 큰지 검증
		if (!targetGain.isZero() && budget.compareTo(targetGain) >= 0) {
			throwIllegalArgumentException(budget, targetGain, maximumLoss);
		}
		// 최대 손실 금액이 예산 보다 작은지 검증
		if (!maximumLoss.isZero() && budget.compareTo(maximumLoss) <= 0) {
			throwIllegalArgumentException(budget, targetGain, maximumLoss);
		}
	}

	private boolean isNegative(Money money) {
		return money.compareTo(Money.zero()) < 0;
	}

	private void throwIllegalArgumentException(Money budget, Money targetGain, Money maximumLoss) {
		String message = String.format("invalid PortfolioFinancial budget: %s, targetGain: %s, maximumLoss: %s",
			budget, targetGain, maximumLoss);
		throw new IllegalArgumentException(message);
	}

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

	@Override
	public String toString() {
		return String.format("(budget=%s, targetGain=%s, maximumLoss=%s)", budget, targetGain, maximumLoss);
	}
}
