package codesquad.fineants.spring.api.watch_list.response;

import java.util.List;
import java.util.stream.Collectors;

import codesquad.fineants.domain.watch_list.WatchList;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReadWatchListsResponse {
	private List<WatchListResponse> watchLists;

	@Getter
	@AllArgsConstructor
	public static class WatchListResponse {
		private Long id;
		private String name;
	}

	public static ReadWatchListsResponse from(List<WatchList> watchLists) {
		List<WatchListResponse> responses = watchLists.stream()
			.map(watchList -> new WatchListResponse(watchList.getId(), watchList.getName()))
			.collect(Collectors.toList());
		return new ReadWatchListsResponse(responses);
	}
}
