package codesquad.fineants.domain.portfolio_gain_history.domain.entity;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.MoneyConverter;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
	@Column(name = "id")
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

	public PortfolioGainHistory(Money totalGain, Money dailyGain, Money cash, Money currentValuation,
		Portfolio portfolio) {
		this(null, totalGain, dailyGain, cash, currentValuation, portfolio);
	}

	@Builder
	public PortfolioGainHistory(Long id, Money totalGain, Money dailyGain, Money cash,
		Money currentValuation, Portfolio portfolio) {
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
