package co.fineants.api.domain.portfolio.domain.dto.request;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
public class PortfoliosDeleteRequest {
	private List<Long> portfolioIds;
}
