package co.fineants.api.domain.portfolio.domain.entity;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.MoneyConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
		validateBudget(budget, targetGain, maximumLoss);
		this.budget = budget;
		this.targetGain = targetGain;
		this.maximumLoss = maximumLoss;
	}

	private void validateBudget(Money budget, Money targetGain, Money maximumLoss) {
		if (budget.isZero()) {
			return;
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

	private void throwIllegalArgumentException(Money budget, Money targetGain, Money maximumLoss) {
		String message = String.format("invalid PortfolioFinancial budget: %s, targetGain: %s, maximumLoss: %s",
			budget, targetGain, maximumLoss);
		throw new IllegalArgumentException(message);
	}

	public static PortfolioFinancial of(Money budget, Money targetGain, Money maximumLoss) {
		return new PortfolioFinancial(budget, targetGain, maximumLoss);
	}

	@Override
	public String toString() {
		return String.format("(budget=%s, targetGain=%s, maximumLoss=%s)", budget, targetGain, maximumLoss);
	}
}
