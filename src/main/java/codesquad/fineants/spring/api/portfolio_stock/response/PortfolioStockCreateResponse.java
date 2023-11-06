package codesquad.fineants.spring.api.portfolio_stock.response;

import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioStockCreateResponse {

	private Long portfolioStockId;

	public static PortfolioStockCreateResponse from(PortfolioHolding portFolioHolding) {
		return new PortfolioStockCreateResponse(portFolioHolding.getId());
	}
}
