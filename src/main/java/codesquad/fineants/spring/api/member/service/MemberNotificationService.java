package codesquad.fineants.spring.api.member.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import codesquad.fineants.domain.fcm_token.FcmRepository;
import codesquad.fineants.domain.fcm_token.FcmToken;
import codesquad.fineants.domain.notification.Notification;
import codesquad.fineants.domain.notification.NotificationRepository;
import codesquad.fineants.spring.api.errors.errorcode.NotificationErrorCode;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.member.request.MemberNotificationCreateRequest;
import codesquad.fineants.spring.api.member.response.MemberNotification;
import codesquad.fineants.spring.api.member.response.MemberNotificationCreateResponse;
import codesquad.fineants.spring.api.member.response.MemberNotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberNotificationService {

	private final NotificationRepository notificationRepository;
	private final FcmRepository fcmRepository;
	private final FirebaseMessaging firebaseMessaging;

	public MemberNotificationResponse fetchNotifications(Long memberId) {
		List<Notification> notifications = notificationRepository.findAllByMemberId(memberId);
		return new MemberNotificationResponse(
			notifications.stream()
				.map(MemberNotification::from)
				.collect(Collectors.toList())
		);
	}

	// 입력 받은 알림들 중에서 안 읽은 알람들을 읽음 처리하고 읽은 알림의 등록번호 리스트를 반환
	@Transactional
	public List<Long> readAllNotifications(Long memberId, List<Long> notificationIds) {
		verifyExistNotifications(memberId, notificationIds);

		// 읽지 않은 알림 조회
		List<Notification> notifications = notificationRepository.findAllByMemberIdAndIds(memberId, notificationIds)
			.stream()
			.filter(notification -> !notification.getIsRead())
			.collect(Collectors.toList());
		log.info("읽지 않은 알림 목록 개수 : {}개", notifications.size());

		// 알림 읽기 처리
		notifications.forEach(Notification::readNotification);

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
	public List<Long> deleteAllNotifications(Long memberId, List<Long> notificationIds) {
		verifyExistNotifications(memberId, notificationIds);

		// 알림 삭제 처리
		notificationRepository.deleteAllById(notificationIds);

		// 삭제한 알림들의 등록번호를 반환
		return notificationIds;
	}

	@Transactional
	public MemberNotificationCreateResponse createNotification(Long memberId, MemberNotificationCreateRequest request) {
		// 알림 데이터 등록
		Notification notification = notificationRepository.save(request.toEntity());

		// 알림 데이터 전송
		List<String> tokens = fcmRepository.findAllByMemberId(memberId).stream()
			.map(FcmToken::getToken)
			.collect(Collectors.toList());
		Optional<BatchResponse> batchResponse = sendNotification(tokens, notification);
		batchResponse.ifPresentOrElse(
			response -> log.info("메시지 전송 결과 : {}", response),
			() -> log.info("메시지 전송 결과 없음"));

		return MemberNotificationCreateResponse.from(notification);
	}

	private Optional<BatchResponse> sendNotification(List<String> tokens, Notification notification) {
		List<Message> messages = tokens.stream()
			.map(token -> Message.builder()
				.setToken(token)
				.setNotification(
					com.google.firebase.messaging.Notification.builder()
						.setTitle(notification.getTitle())
						.setBody(notification.getContent())
						.build())
				.build())
			.collect(Collectors.toList());

		try {
			return Optional.ofNullable(firebaseMessaging.sendAll(messages));
		} catch (FirebaseMessagingException e) {
			log.info(e.getMessage(), e);
		}
		return Optional.empty();
	}
}
