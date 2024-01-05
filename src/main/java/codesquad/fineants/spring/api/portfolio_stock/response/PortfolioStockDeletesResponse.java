package codesquad.fineants.spring.api.portfolio_stock.response;

import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PortfolioStockDeletesResponse {
	private List<Long> portfolioHoldingIds;
}
