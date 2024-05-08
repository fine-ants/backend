package codesquad.fineants.domain.portfolio.domain.dto.response;

import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortFolioCreateResponse {

	private Long portfolioId;

	public static PortFolioCreateResponse from(Portfolio portfolio) {
		return new PortFolioCreateResponse(portfolio.getId());
	}
}
