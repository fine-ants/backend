package codesquad.fineants.domain.stock.domain.dto.response;

import java.util.Set;

import codesquad.fineants.domain.kis.domain.dto.response.DividendItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class StockReloadResponse {
	private Set<String> addedStocks;
	private Set<String> deletedStocks;
	private Set<DividendItem> addedDividends;

	public static StockReloadResponse create(Set<String> addedStocks, Set<String> deletedStocks,
		Set<DividendItem> addedDividends) {
		return new StockReloadResponse(addedStocks, deletedStocks, addedDividends);
	}
}
