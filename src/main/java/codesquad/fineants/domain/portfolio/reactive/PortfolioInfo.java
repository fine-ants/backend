package codesquad.fineants.domain.portfolio.reactive;

import java.util.List;

import codesquad.fineants.domain.holding.domain.dto.response.PortfolioDetailRealTimeItem;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioHoldingRealTimeItem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class PortfolioInfo {
	private final PortfolioDetailRealTimeItem portfolioDetails;
	private final List<PortfolioHoldingRealTimeItem> portfolioHoldings;

	public static PortfolioInfo fetch(PortfolioDetailRealTimeItem portfolio,
		List<PortfolioHoldingRealTimeItem> holdings) {
		return new PortfolioInfo(portfolio, holdings);
	}
}
