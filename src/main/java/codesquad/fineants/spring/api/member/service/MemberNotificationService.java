package codesquad.fineants.spring.api.member.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import codesquad.fineants.domain.fcm_token.FcmRepository;
import codesquad.fineants.domain.fcm_token.FcmToken;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.notification.Notification;
import codesquad.fineants.domain.notification.NotificationRepository;
import codesquad.fineants.spring.api.common.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.common.errors.errorcode.NotificationErrorCode;
import codesquad.fineants.spring.api.common.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.member.request.MemberPortfolioNotificationSendRequest;
import codesquad.fineants.spring.api.member.request.MemberTargetPriceNotificationSendRequest;
import codesquad.fineants.spring.api.member.response.MemberNotification;
import codesquad.fineants.spring.api.member.response.MemberNotificationResponse;
import codesquad.fineants.spring.api.member.response.MemberNotificationSendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberNotificationService {

	private final NotificationRepository notificationRepository;
	private final FcmRepository fcmRepository;
	private final MemberRepository memberRepository;
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
	public MemberNotificationSendResponse sendPortfolioNotification(Long memberId,
		MemberPortfolioNotificationSendRequest request) {
		// 알림 데이터 등록
		Member member = findMember(memberId);
		Notification notification = notificationRepository.save(request.toEntity(member));

		// 알림 데이터 전송
		List<String> tokens = fcmRepository.findAllByMemberId(memberId).stream()
			.map(FcmToken::getToken)
			.collect(Collectors.toList());
		List<String> sendMessageIds = sendNotification(tokens, notification);
		if (sendMessageIds.isEmpty()) {
			notificationRepository.deleteById(notification.getId());
			log.info("알림 메시지 전송 실패로 인한 알림 메시지 삭제, id={}", notification.getId());
		}
		return MemberNotificationSendResponse.from(notification, sendMessageIds);
	}

	@Transactional
	public MemberNotificationSendResponse sendTargetPriceNotification(Long memberId,
		MemberTargetPriceNotificationSendRequest request) {
		// 알림 데이터 등록
		Member member = findMember(memberId);
		Notification notification = notificationRepository.save(request.toEntity(member));

		// 알림 데이터 전송
		List<String> tokens = fcmRepository.findAllByMemberId(memberId).stream()
			.map(FcmToken::getToken)
			.collect(Collectors.toList());
		List<String> sendMessageIds = sendNotification(tokens, notification);
		if (sendMessageIds.isEmpty()) {
			notificationRepository.deleteById(notification.getId());
			log.info("알림 메시지 전송 실패로 인한 알림 메시지 삭제, id={}", notification.getId());
		}
		return MemberNotificationSendResponse.from(notification, sendMessageIds);
	}

	private Member findMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new NotFoundResourceException(MemberErrorCode.NOT_FOUND_MEMBER));
	}

	private List<String> sendNotification(List<String> tokens, Notification notification) {
		List<String> messageIds = new ArrayList<>();
		List<String> deadTokens = new ArrayList<>();
		for (String token : tokens) {
			Message message = Message.builder()
				.setToken(token)
				.setNotification(
					com.google.firebase.messaging.Notification.builder()
						.setTitle(notification.getTitle())
						.setBody(notification.createNotificationContent())
						.build())
				.build();
			try {
				String messageId = firebaseMessaging.send(message);
				messageIds.add(messageId);
				log.info("알림 메시지 전송 결과 : messageId={}", messageId);
			} catch (FirebaseMessagingException e) {
				log.info("알림 메시지 전송 실패, token={}, message={}", token, e.getMessage());
				deadTokens.add(token);
			}
		}
		int deleteTokenCount = fcmRepository.deleteAllByTokens(deadTokens);
		log.info("토큰 삭제 결과 deleteTokenCount={}", deleteTokenCount);
		return messageIds;
	}
}
