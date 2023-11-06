package codesquad.fineants.spring.api.portfolio.response;

import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PortfolioListResponse {
	private List<PortFolioItem> portfolios;
}
