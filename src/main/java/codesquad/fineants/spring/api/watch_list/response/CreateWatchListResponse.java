package codesquad.fineants.spring.api.watch_list.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class CreateWatchListResponse {
	private Long watchlistId;

	public static CreateWatchListResponse create(Long watchlistId) {
		return new CreateWatchListResponse(watchlistId);
	}
}
