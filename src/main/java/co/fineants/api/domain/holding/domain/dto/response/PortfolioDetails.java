package co.fineants.api.domain.holding.domain.dto.response;

import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class PortfolioDetails {
	private Long id;
	private String securitiesFirm;
	private String name;

	public static PortfolioDetails from(Portfolio portfolio) {
		return new PortfolioDetails(portfolio.getId(), portfolio.getSecuritiesFirm(), portfolio.getName());
	}
}
