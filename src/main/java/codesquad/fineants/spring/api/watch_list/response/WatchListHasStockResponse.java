package codesquad.fineants.spring.api.watch_list.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
@ToString
public class WatchListHasStockResponse {
	private long id;
	private String name;
	private boolean hasStock;

	public static WatchListHasStockResponse create(long id, String name, boolean hasStock) {
		return WatchListHasStockResponse.builder()
			.id(id)
			.name(name)
			.hasStock(hasStock)
			.build();
	}
}
