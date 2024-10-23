package co.fineants.api.domain.portfolio.domain.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PortfolioNameResponse {
	@JsonProperty
	private final List<PortfolioNameItem> portfolios;

	@JsonCreator
	private PortfolioNameResponse(@JsonProperty("portfolios") List<PortfolioNameItem> portfolios) {
		this.portfolios = portfolios;
	}

	public static PortfolioNameResponse from(List<PortfolioNameItem> items) {
		return new PortfolioNameResponse(items);
	}

	@Override
	public String toString() {
		return String.format("PortfolioNames : %s", portfolios);
	}
}
