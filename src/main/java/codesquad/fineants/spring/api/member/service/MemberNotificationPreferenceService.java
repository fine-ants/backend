package codesquad.fineants.spring.api.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.notification_preference.NotificationPreference;
import codesquad.fineants.domain.notification_preference.NotificationPreferenceRepository;
import codesquad.fineants.spring.api.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.NotificationPreferenceErrorCode;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.fcm.response.FcmDeleteResponse;
import codesquad.fineants.spring.api.fcm.service.FcmService;
import codesquad.fineants.spring.api.member.request.MemberNotificationPreferenceRequest;
import codesquad.fineants.spring.api.member.response.MemberNotificationPreferenceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberNotificationPreferenceService {

	private final NotificationPreferenceRepository notificationPreferenceRepository;
	private final MemberRepository memberRepository;
	private final FcmService fcmService;

	@Transactional
	public MemberNotificationPreferenceResponse registerDefaultNotificationPreference(Member member) {
		NotificationPreference preference = notificationPreferenceRepository.findByMemberId(member.getId())
			.orElseGet(() -> NotificationPreference.defaultSetting(member));
		NotificationPreference saveNotificationPreference = notificationPreferenceRepository.save(preference);
		return MemberNotificationPreferenceResponse.from(saveNotificationPreference);
	}

	@Transactional
	public MemberNotificationPreferenceResponse updateNotificationPreference(
		Long memberId,
		MemberNotificationPreferenceRequest request) {
		notificationPreferenceRepository.findByMemberId(memberId)
			.ifPresentOrElse(notificationPreference -> notificationPreference.changePreference(request.toEntity()),
				() -> {
					Member member = memberRepository.findById(memberId)
						.orElseThrow(() -> new NotFoundResourceException(MemberErrorCode.NOT_FOUND_MEMBER));
					notificationPreferenceRepository.save(NotificationPreference.defaultSetting(member));
				});
		NotificationPreference preference = notificationPreferenceRepository.findByMemberId(memberId)
			.orElseThrow(() ->
				new NotFoundResourceException(NotificationPreferenceErrorCode.NOT_FOUND_NOTIFICATION_PREFERENCE));

		// 회원 계정의 전체 알림 설정이 모두 비활성화인 경우 FCM 토큰 삭제
		if (preference.isAllInActive() && request.getFcmTokenId() != null) {
            FcmDeleteResponse response = fcmService.deleteToken(request.getFcmTokenId(), memberId);
			log.info("회원 알림 설정 전체 비활성화로 인한 결과 : {}", response);
		}
		return MemberNotificationPreferenceResponse.from(preference);
	}
}
