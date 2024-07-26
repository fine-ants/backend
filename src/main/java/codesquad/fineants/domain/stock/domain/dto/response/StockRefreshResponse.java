package codesquad.fineants.domain.stock.domain.dto.response;

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

	public static StockRefreshResponse create(List<String> addedStocks) {
		return new StockRefreshResponse(addedStocks);
	}
}
