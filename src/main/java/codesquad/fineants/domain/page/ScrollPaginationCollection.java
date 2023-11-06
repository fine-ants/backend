package codesquad.fineants.domain.page;

import java.util.List;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ScrollPaginationCollection<T> {
	private final List<T> itemsWithNextCursor;
	private final int countPerScroll;

	public static <T> ScrollPaginationCollection<T> of(List<T> itemsWithNextCursor, int countPerScroll) {
		return new ScrollPaginationCollection<>(itemsWithNextCursor, countPerScroll);
	}

	public boolean isLastScroll() {
		return this.itemsWithNextCursor.size() <= countPerScroll;
	}

	public List<T> getCurrentScrollItems() {
		if (isLastScroll()) {
			return this.itemsWithNextCursor;
		}
		return this.itemsWithNextCursor.subList(0, countPerScroll);
	}

	public T getNextCursor() {
		return itemsWithNextCursor.get(countPerScroll - 1);
	}
}
