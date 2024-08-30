package co.fineants.api.domain.portfolio.domain.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PortfolioListResponse {
	private List<PortFolioItem> portfolios;
}
