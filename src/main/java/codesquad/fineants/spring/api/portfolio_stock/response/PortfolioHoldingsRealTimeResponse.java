package codesquad.fineants.spring.api.portfolio_stock.response;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioHoldingsRealTimeResponse {
	private PortfolioDetailRealTimeItem portfolioDetails;
	private List<PortfolioHoldingRealTimeItem> portfolioHoldings;

	public static PortfolioHoldingsRealTimeResponse of(PortfolioDetailRealTimeItem portfolioDetailRealTimeItem,
		List<PortfolioHoldingRealTimeItem> portfolioHoldingDetails) {
		return new PortfolioHoldingsRealTimeResponse(portfolioDetailRealTimeItem, portfolioHoldingDetails);
	}
}
