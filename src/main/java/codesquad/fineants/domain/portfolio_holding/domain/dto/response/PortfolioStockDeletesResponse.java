package codesquad.fineants.domain.portfolio_holding.domain.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PortfolioStockDeletesResponse {
	private List<Long> portfolioHoldingIds;
}
