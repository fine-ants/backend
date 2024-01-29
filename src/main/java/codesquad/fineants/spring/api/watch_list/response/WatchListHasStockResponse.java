package codesquad.fineants.spring.api.watch_list.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WatchListHasStockResponse {
	private long id;
	private String name;
	private boolean hasStock;
}
