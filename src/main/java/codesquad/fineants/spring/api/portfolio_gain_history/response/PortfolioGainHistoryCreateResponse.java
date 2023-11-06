package codesquad.fineants.spring.api.portfolio_gain_history.response;

import java.util.List;
import java.util.stream.Collectors;

import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioGainHistoryCreateResponse {
	private List<Long> ids;

	public static PortfolioGainHistoryCreateResponse from(List<PortfolioGainHistory> histories) {
		List<Long> ids = histories.stream()
			.map(PortfolioGainHistory::getId)
			.collect(Collectors.toList());
		return new PortfolioGainHistoryCreateResponse(ids);
	}
}
