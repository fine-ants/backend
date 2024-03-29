package codesquad.fineants.spring.api.stock.response;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class StockRefreshResponse {
	private List<String> addedStocks;
	private List<String> deletedStocks;

	public static StockRefreshResponse create(List<String> addedStocks, List<String> deletedStocks) {
		return new StockRefreshResponse(addedStocks, deletedStocks);
	}
}
