package codesquad.fineants.domain.holding.domain.dto.response;

import codesquad.fineants.domain.holding.domain.entity.PortfolioHolding;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioStockCreateResponse {

	private Long portfolioStockId;

	public static PortfolioStockCreateResponse from(PortfolioHolding portFolioHolding) {
		return new PortfolioStockCreateResponse(portFolioHolding.getId());
	}
}
