package codesquad.fineants.domain.stock.domain.dto.response;

import java.util.Set;

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
	private Set<String> addedStocks;
	private Set<String> deletedStocks;

	public static StockRefreshResponse create(Set<String> addedStocks, Set<String> deletedStocks) {
		return new StockRefreshResponse(addedStocks, deletedStocks);
	}
}
