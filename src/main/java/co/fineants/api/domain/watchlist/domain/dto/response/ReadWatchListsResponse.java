package co.fineants.api.domain.watchlist.domain.dto.response;

import co.fineants.api.domain.watchlist.domain.entity.WatchList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReadWatchListsResponse {
	private Long id;
	private String name;

	public static ReadWatchListsResponse create(Long id, String name) {
		return new ReadWatchListsResponse(id, name);
	}

	public static ReadWatchListsResponse from(WatchList watchList) {
		return new ReadWatchListsResponse(watchList.getId(), watchList.getName());
	}
}
