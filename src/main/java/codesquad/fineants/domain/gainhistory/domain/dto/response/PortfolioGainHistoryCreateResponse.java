package codesquad.fineants.domain.gainhistory.domain.dto.response;

import java.util.List;

import codesquad.fineants.domain.gainhistory.domain.entity.PortfolioGainHistory;
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
			.toList();
		return new PortfolioGainHistoryCreateResponse(ids);
	}
}
