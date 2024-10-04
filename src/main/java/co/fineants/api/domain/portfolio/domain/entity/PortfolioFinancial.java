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
		this.budget = budget;
		this.targetGain = targetGain;
		this.maximumLoss = maximumLoss;
	}

	public static PortfolioFinancial of(Money budget, Money targetGain, Money maximumLoss) {
		return new PortfolioFinancial(budget, targetGain, maximumLoss);
	}

	@Override
	public String toString() {
		return String.format("(budget=%s, targetGain=%s, maximumLoss=%s)", budget, targetGain, maximumLoss);
	}
}
