package codesquad.fineants.domain.holding.domain.dto.response;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class PortfolioHoldingsRealTimeResponse {
	private PortfolioDetailRealTimeItem portfolioDetails;
	private List<PortfolioHoldingRealTimeItem> portfolioHoldings;

	public static PortfolioHoldingsRealTimeResponse of(PortfolioDetailRealTimeItem portfolioDetailRealTimeItem,
		List<PortfolioHoldingRealTimeItem> portfolioHoldingDetails) {
		return new PortfolioHoldingsRealTimeResponse(portfolioDetailRealTimeItem, portfolioHoldingDetails);
	}
}
