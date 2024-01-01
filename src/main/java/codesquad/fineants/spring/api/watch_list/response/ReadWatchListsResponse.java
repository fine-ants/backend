package codesquad.fineants.spring.api.watch_list.response;

import java.util.List;

import lombok.AllArgsConstructor;

public class ReadWatchLists {
	private List watchList;

	@AllArgsConstructor
	public static class WatchList {
		private Long id;
		private String name;
	}
}
