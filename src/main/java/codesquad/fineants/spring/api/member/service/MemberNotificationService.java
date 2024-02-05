package codesquad.fineants.spring.api.member.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.notification.Notification;
import codesquad.fineants.domain.notification.NotificationRepository;
import codesquad.fineants.spring.api.member.response.MemberNotification;
import codesquad.fineants.spring.api.member.response.MemberNotificationResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberNotificationService {

	private final NotificationRepository notificationRepository;

	public MemberNotificationResponse readNotifications(Long memberId) {
		List<Notification> notifications = notificationRepository.findAllByMemberId(memberId);
		return new MemberNotificationResponse(
			notifications.stream()
				.map(MemberNotification::from)
				.collect(Collectors.toList())
		);
	}
}
