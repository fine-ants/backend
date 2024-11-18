package co.fineants.api.domain.holding.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class PortfolioStockCreateResponse {

	@JsonProperty
	private final Long portfolioHoldingId;

	@JsonCreator
	private PortfolioStockCreateResponse(@JsonProperty("portfolioHoldingId") Long portfolioHoldingId) {
		this.portfolioHoldingId = portfolioHoldingId;
	}

	public static PortfolioStockCreateResponse from(PortfolioHolding portFolioHolding) {
		return new PortfolioStockCreateResponse(portFolioHolding.getId());
	}

	@Override
	public String toString() {
		return String.format("포트폴리오 종목 생성 응답 결과(portfolioHoldingId=%d)", portfolioHoldingId);
	}
}
