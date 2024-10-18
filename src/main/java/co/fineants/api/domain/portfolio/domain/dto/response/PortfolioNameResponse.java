package co.fineants.api.domain.portfolio.domain.dto.response;

import java.util.List;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioNameResponse {

	private final List<PortfolioNameItem> portfolios;

	public static PortfolioNameResponse from(List<PortfolioNameItem> items) {
		return new PortfolioNameResponse(items);
	}

	@Override
	public String toString() {
		return String.format("PortfolioNames : %s", portfolios);
	}
}
