package co.fineants.api.domain.gainhistory.domain.entity;

import java.time.format.DateTimeFormatter;

import co.fineants.api.domain.BaseEntity;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.MoneyConverter;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
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

	private static final DateTimeFormatter LINE_CHART_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private PortfolioGainHistory(Money totalGain, Money dailyGain, Money cash, Money currentValuation,
		Portfolio portfolio) {
		this(null, totalGain, dailyGain, cash, currentValuation, portfolio);
	}

	private PortfolioGainHistory(Long id, Money totalGain, Money dailyGain, Money cash, Money currentValuation,
		Portfolio portfolio) {
		this.id = id;
		this.totalGain = totalGain;
		this.dailyGain = dailyGain;
		this.cash = cash;
		this.currentValuation = currentValuation;
		this.portfolio = portfolio;
	}

	public static PortfolioGainHistory empty(Portfolio portfolio) {
		return new PortfolioGainHistory(Money.zero(), Money.zero(), Money.zero(), Money.zero(), portfolio);
	}

	public static PortfolioGainHistory create(Money totalGain, Money dailyGain, Money cash, Money currentValuation,
		Portfolio portfolio) {
		return new PortfolioGainHistory(totalGain, dailyGain, cash, currentValuation, portfolio);
	}

	public String getLineChartKey() {
		return LINE_CHART_KEY_FORMATTER.format(super.getCreateAt());
	}

	/**
	 * Return cash + currentValuation
	 *
	 * @return cash + currentValuation
	 */
	public Expression calculateTotalPortfolioValue() {
		return cash.plus(currentValuation);
	}
}
