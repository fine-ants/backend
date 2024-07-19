package codesquad.fineants.global.common.authorized;

import java.util.Optional;

public interface AuthorizeService<T> {
	Optional<T> findResourceById(Long id);

	boolean isAuthorized(Object resource, Long memberId);
}
