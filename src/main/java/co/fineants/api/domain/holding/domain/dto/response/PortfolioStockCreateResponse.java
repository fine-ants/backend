package co.fineants.api.domain.holding.domain.dto.response;

import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioStockCreateResponse {

	private Long portfolioHoldingId;

	public static PortfolioStockCreateResponse from(PortfolioHolding portFolioHolding) {
		return new PortfolioStockCreateResponse(portFolioHolding.getId());
	}
}
