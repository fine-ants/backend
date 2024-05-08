package codesquad.fineants.domain.watch_list.domain.dto.request;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteWatchStocksRequest {
	private List<String> tickerSymbols;
}
