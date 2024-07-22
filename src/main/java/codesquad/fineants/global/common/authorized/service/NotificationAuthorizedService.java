package codesquad.fineants.global.common.authorized.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.notification.domain.entity.Notification;
import codesquad.fineants.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationAuthorizedService implements AuthorizedService<Notification> {

	private final NotificationRepository repository;

	@Override
	public List<Notification> findResourceAllBy(List<Long> ids) {
		return repository.findAllById(ids);
	}

	@Override
	public boolean isAuthorized(Object resource, Long memberId) {
		return ((Notification)resource).hasAuthorization(memberId);
	}
}
