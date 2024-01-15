package codesquad.fineants.spring.api.watch_list.response;

import java.util.List;
import java.util.stream.Collectors;

import codesquad.fineants.domain.watch_list.WatchList;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReadWatchListsResponse {
	private Long id;
	private String name;

	public static List<ReadWatchListsResponse> from(List<WatchList> watchLists) {
		return watchLists.stream()
			.map(watchList -> new ReadWatchListsResponse(watchList.getId(), watchList.getName()))
			.collect(Collectors.toList());
	}
}
