package co.fineants.api.global.common.authorized.service;

import java.util.List;

public interface AuthorizedService<T> {
	List<T> findResourceAllBy(List<Long> ids);

	boolean isAuthorized(Object resource, Long memberId);
}
