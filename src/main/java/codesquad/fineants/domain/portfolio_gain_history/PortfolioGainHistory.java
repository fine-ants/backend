package codesquad.fineants.domain.portfolio_gain_history;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.MoneyConverter;
import codesquad.fineants.domain.portfolio.Portfolio;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PortfolioGainHistory extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Convert(converter = MoneyConverter.class)
	@Column(precision = 19, nullable = false)
	private Money totalGain;
	@Convert(converter = MoneyConverter.class)
	@Column(precision = 19, nullable = false)
	private Money dailyGain;
	@Convert(converter = MoneyConverter.class)
	@Column(precision = 19, nullable = false)
	private Money cash;
	@Convert(converter = MoneyConverter.class)
	@Column(precision = 19, nullable = false)
	private Money currentValuation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "portfolio_id")
	private Portfolio portfolio;

	@Builder
	public PortfolioGainHistory(Long id, Money totalGain, Money dailyGain, Money currentValuation, Portfolio portfolio,
		Money cash) {
		this.id = id;
		this.totalGain = totalGain;
		this.dailyGain = dailyGain;
		this.currentValuation = currentValuation;
		this.portfolio = portfolio;
		this.cash = cash;
	}

	public static PortfolioGainHistory empty() {
		return PortfolioGainHistory.builder()
			.totalGain(Money.zero())
			.dailyGain(Money.zero())
			.currentValuation(Money.zero())
			.cash(Money.zero())
			.build();
	}
}
