package codesquad.fineants.spring.api.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.notification_preference.NotificationPreference;
import codesquad.fineants.domain.notification_preference.NotificationPreferenceRepository;
import codesquad.fineants.spring.api.errors.errorcode.NotificationPreferenceErrorCode;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.member.request.MemberNotificationPreferenceRequest;
import codesquad.fineants.spring.api.member.response.MemberNotificationPreferenceResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberNotificationPreferenceService {

	private final NotificationPreferenceRepository notificationPreferenceRepository;

	@Transactional
	public MemberNotificationPreferenceResponse updateNotificationPreference(
		Long memberId,
		MemberNotificationPreferenceRequest request) {
		NotificationPreference notificationPreference = findNotificationPreference(memberId);
		notificationPreference.changePreference(request.toEntity());
		return MemberNotificationPreferenceResponse.from(notificationPreference);
	}

	private NotificationPreference findNotificationPreference(Long memberId) {
		return notificationPreferenceRepository.findByMemberId(memberId)
			.orElseThrow(() ->
				new NotFoundResourceException(NotificationPreferenceErrorCode.NOT_FOUND_NOTIFICATION_PREFERENCE));
	}
}
