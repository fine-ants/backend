package codesquad.fineants.domain.portfolio_gain_history;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import codesquad.fineants.domain.BaseEntity;
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
	private Long totalGain;
	private Long dailyGain;
	private Long currentValuation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "portfolio_id")
	private Portfolio portfolio;

	@Builder
	public PortfolioGainHistory(Long id, Long totalGain, Long dailyGain, Long currentValuation, Portfolio portfolio) {
		this.id = id;
		this.totalGain = totalGain;
		this.dailyGain = dailyGain;
		this.currentValuation = currentValuation;
		this.portfolio = portfolio;
	}

	public static PortfolioGainHistory empty() {
		return PortfolioGainHistory.builder()
			.totalGain(0L)
			.dailyGain(0L)
			.currentValuation(0L)
			.build();
	}
}
