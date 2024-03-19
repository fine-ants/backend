package codesquad.fineants.spring.api.portfolio_stock.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class PortfolioSectorChartItem {
	private String sector;
	private Double sectorWeight;

	public static PortfolioSectorChartItem create(String sector, Double sectorWeight) {
		return new PortfolioSectorChartItem(sector, sectorWeight);
	}
}
