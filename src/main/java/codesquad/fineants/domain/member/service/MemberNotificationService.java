package codesquad.fineants.domain.member.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.member.domain.dto.response.MemberNotification;
import codesquad.fineants.domain.member.domain.dto.response.MemberNotificationResponse;
import codesquad.fineants.domain.notification.domain.entity.Notification;
import codesquad.fineants.domain.notification.repository.NotificationRepository;
import codesquad.fineants.global.errors.errorcode.MemberErrorCode;
import codesquad.fineants.global.errors.errorcode.NotificationErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;
import codesquad.fineants.global.errors.exception.NotFoundResourceException;
import codesquad.fineants.global.security.oauth.dto.MemberAuthentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberNotificationService {

	private final NotificationRepository notificationRepository;

	@Secured("ROLE_USER")
	public MemberNotificationResponse fetchNotifications(Long memberId) {
		validateMemberAuthorization(memberId);
		List<Notification> notifications = notificationRepository.findAllByMemberId(memberId);
		return MemberNotificationResponse.create(
			notifications.stream()
				.map(MemberNotification::from)
				.collect(Collectors.toList())
		);
	}

	private void validateMemberAuthorization(Long memberId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Object principal = authentication.getPrincipal();
		MemberAuthentication memberAuthentication = (MemberAuthentication)principal;
		if (!memberId.equals(memberAuthentication.getId())) {
			throw new FineAntsException(MemberErrorCode.FORBIDDEN_MEMBER);
		}
	}

	// 입력 받은 알림들 중에서 안 읽은 알람들을 읽음 처리하고 읽은 알림의 등록번호 리스트를 반환
	@Transactional
	@Secured("ROLE_USER")
	public List<Long> readAllNotifications(Long memberId, List<Long> notificationIds) {
		validateMemberAuthorization(memberId);
		verifyExistNotifications(memberId, notificationIds);

		// 읽지 않은 알림 조회
		List<Notification> notifications = notificationRepository.findAllByMemberIdAndIds(memberId, notificationIds)
			.stream()
			.filter(notification -> !notification.getIsRead())
			.toList();
		log.info("읽지 않은 알림 목록 개수 : {}개", notifications.size());

		// 알림 읽기 처리
		notifications.forEach(Notification::read);

		// 읽은 알림들의 등록번호 반환
		return notifications.stream()
			.map(Notification::getId)
			.collect(Collectors.toList());
	}

	private void verifyExistNotifications(Long memberId, List<Long> notificationIds) {
		List<Notification> findNotifications = notificationRepository.findAllByMemberIdAndIds(memberId,
			notificationIds);
		if (notificationIds.size() != findNotifications.size()) {
			throw new NotFoundResourceException(NotificationErrorCode.NOT_FOUND_NOTIFICATION);
		}
	}

	@Transactional
	@Secured("ROLE_USER")
	public List<Long> deleteAllNotifications(Long memberId, List<Long> notificationIds) {
		validateMemberAuthorization(memberId);
		verifyExistNotifications(memberId, notificationIds);

		// 알림 삭제 처리
		notificationRepository.deleteAllById(notificationIds);

		// 삭제한 알림들의 등록번호를 반환
		return notificationIds;
	}
}
