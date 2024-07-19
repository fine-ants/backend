package codesquad.fineants.global.common.authorized;

import java.util.Collections;
import java.util.List;

public interface AuthorizeService<T> {
	default List<T> findResourceAllBy(List<Long> ids) {
		return Collections.emptyList();
	}

	default boolean isAuthorized(Object resource, Long memberId) {
		return false;
	}
}
