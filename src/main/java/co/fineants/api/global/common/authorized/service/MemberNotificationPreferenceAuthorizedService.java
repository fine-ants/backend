package co.fineants.api.global.common.authorized.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import co.fineants.api.domain.notificationpreference.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberNotificationPreferenceAuthorizedService implements AuthorizedService<NotificationPreference> {
	private final NotificationPreferenceRepository repository;

	@Override
	public List<NotificationPreference> findResourceAllBy(List<Long> ids) {
		return repository.findAllByMemberIds(ids);
	}

	@Override
	public boolean isAuthorized(Object resource, Long memberId) {
		return ((NotificationPreference)resource).hasAuthorization(memberId);
	}
}
