package codesquad.fineants.domain.watchlist.domain.dto.request;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateWatchStockRequest {
	private List<String> tickerSymbols;
}
