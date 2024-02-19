package codesquad.fineants.spring.api.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import codesquad.fineants.domain.fcm_token.FcmRepository;
import codesquad.fineants.domain.fcm_token.FcmToken;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.notification.Notification;
import codesquad.fineants.domain.notification.NotificationRepository;
import codesquad.fineants.spring.AbstractContainerBaseTest;
import codesquad.fineants.spring.api.errors.errorcode.NotificationErrorCode;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.member.request.MemberNotificationSendRequest;
import codesquad.fineants.spring.api.member.response.MemberNotification;
import codesquad.fineants.spring.api.member.response.MemberNotificationResponse;
import codesquad.fineants.spring.api.member.response.MemberNotificationSendResponse;

class MemberNotificationServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberNotificationService notificationService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private FcmRepository fcmRepository;

	@AfterEach
	void tearDown() {
		fcmRepository.deleteAllInBatch();
		notificationRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
	}

	@DisplayName("사용자는 회원 알림 목록을 조회합니다")
	@Test
	void fetchNotifications() {
		// given
		Member member = memberRepository.save(createMember());
		List<Notification> notifications = notificationRepository.saveAll(createNotifications(member));

		// when
		MemberNotificationResponse response = notificationService.fetchNotifications(member.getId());

		// then
		assertThat(response)
			.extracting("notifications")
			.asList()
			.hasSize(3)
			.containsExactly(
				MemberNotification.builder()
					.notificationId(notifications.get(2).getId())
					.title("포트폴리오")
					.body("포트폴리오2의 최대 손실율을 초과했습니다")
					.timestamp(LocalDateTime.of(2024, 1, 24, 10, 10, 10))
					.isRead(false)
					.type("portfolio")
					.referenceId(notifications.get(2).getReferenceId())
					.build(),
				MemberNotification.builder()
					.notificationId(notifications.get(1).getId())
					.title("포트폴리오")
					.body("포트폴리오1의 목표 수익률을 달성했습니다")
					.timestamp(LocalDateTime.of(2024, 1, 23, 10, 10, 10))
					.isRead(false)
					.type("portfolio")
					.referenceId(notifications.get(1).getReferenceId())
					.build(),
				MemberNotification.builder()
					.notificationId(notifications.get(0).getId())
					.title("지정가")
					.body("삼성전자가 지정가 KRW60000에 도달했습니다")
					.timestamp(LocalDateTime.of(2024, 1, 22, 10, 10, 10))
					.isRead(true)
					.type("stock")
					.referenceId(notifications.get(0).getReferenceId())
					.build()
			);

	}

	@DisplayName("사용자는 알림 모두 읽습니다")
	@Test
	void readAllNotifications() {
		// given
		Member member = memberRepository.save(createMember());
		List<Notification> notifications = notificationRepository.saveAll(createNotifications(member));
		List<Long> notificationIds = notifications.stream()
			.map(Notification::getId)
			.collect(Collectors.toList());

		// when
		List<Long> readNotificationIds = notificationService.readAllNotifications(member.getId(), notificationIds);

		// then
		assertAll(
			() -> assertThat(readNotificationIds)
				.hasSize(2)
				.containsExactly(notificationIds.get(1), notificationIds.get(2)),
			() -> assertThat(notificationRepository.findAllById(Objects.requireNonNull(readNotificationIds))
				.stream()
				.allMatch(Notification::getIsRead)).isTrue()
		);
	}

	@DisplayName("사용자는 존재하지 않는 알람을 읽음 처리할 수 없다")
	@Test
	void readAllNotifications_whenNotExistNotificationIds_thenThrow404Error() {
		// given
		Member member = memberRepository.save(createMember());
		List<Notification> notifications = notificationRepository.saveAll(createNotifications(member));
		List<Long> notificationIds = notifications.stream()
			.map(Notification::getId)
			.collect(Collectors.toList());

		Long notExistNotificationId = 9999L;
		notificationIds.add(notExistNotificationId);

		// when
		Throwable throwable = catchThrowable(
			() -> notificationService.readAllNotifications(member.getId(), notificationIds));

		// then
		assertThat(throwable)
			.isInstanceOf(NotFoundResourceException.class)
			.hasMessage(NotificationErrorCode.NOT_FOUND_NOTIFICATION.getMessage());
	}

	@DisplayName("사용자는 알림을 전체 삭제합니다")
	@Test
	void deleteAllNotifications() {
		// given
		Member member = memberRepository.save(createMember());
		List<Notification> notifications = notificationRepository.saveAll(createNotifications(member));
		List<Long> notificationIds = notifications.stream()
			.map(Notification::getId)
			.collect(Collectors.toList());

		// when
		List<Long> deletedAllNotifications = notificationService.deleteAllNotifications(member.getId(),
			notificationIds);

		// then
		assertThat(deletedAllNotifications).hasSize(3);
		assertThat(notificationRepository.findAllByMemberIdAndIds(member.getId(), notificationIds).size())
			.isZero();
	}

	@DisplayName("사용자는 존재하지 않은 알람들을 삭제할 수 없습니다")
	@Test
	void deleteAllNotifications_whenNotExistNotificationId_then404Error() {
		// given
		Member member = memberRepository.save(createMember());
		List<Notification> notifications = notificationRepository.saveAll(createNotifications(member));
		List<Long> notificationIds = notifications.stream()
			.map(Notification::getId)
			.collect(Collectors.toList());
		notificationIds.add(9999L);

		// when
		Throwable throwable = catchThrowable(() -> notificationService.deleteAllNotifications(member.getId(),
			notificationIds));

		// then
		assertThat(throwable)
			.isInstanceOf(NotFoundResourceException.class)
			.hasMessage(NotificationErrorCode.NOT_FOUND_NOTIFICATION.getMessage());
	}

	@DisplayName("사용자는 알림 발송합니다")
	@Test
	void sendNotification() {
		// given
		Member member = memberRepository.save(createMember());
		fcmRepository.save(createFcmToken(member));
		MemberNotificationSendRequest request = MemberNotificationSendRequest.builder()
			.title("포트폴리오")
			.body("포트폴리오1의 최대 손실율을 초과했습니다")
			.type("portfolio")
			.referenceId("1")
			.build();

		// when
		MemberNotificationSendResponse response = notificationService.sendNotificationToUsers(member.getId(), request);

		// then
		assertAll(
			() -> assertThat(response)
				.extracting("title", "content", "isRead", "type", "referenceId")
				.containsExactly("포트폴리오", "포트폴리오1의 최대 손실율을 초과했습니다", false, "portfolio", "1"),
			() -> assertThat(response)
				.extracting("sendMessageIds")
				.asList()
				.hasSize(1)
		);
	}

	@DisplayName("사용자는 알림 발송시 유효하지 않은 토큰이 들어있는 경우 제거합니다")
	@Test
	void sendNotification_whenInvalidTokenOfTokens_thenDeleteInvalidToken() {
		// given
		Member member = memberRepository.save(createMember());
		fcmRepository.save(createFcmToken(member));
		FcmToken saveFcmToken = fcmRepository.save(createFcmToken(member, "fcmToken"));
		MemberNotificationSendRequest request = MemberNotificationSendRequest.builder()
			.title("포트폴리오")
			.body("포트폴리오1의 최대 손실율을 초과했습니다")
			.type("portfolio")
			.referenceId("1")
			.build();

		// when
		MemberNotificationSendResponse response = notificationService.sendNotificationToUsers(member.getId(), request);

		// then
		assertAll(
			() -> assertThat(response)
				.extracting("title", "content", "isRead", "type", "referenceId")
				.containsExactly("포트폴리오", "포트폴리오1의 최대 손실율을 초과했습니다", false, "portfolio", "1"),
			() -> assertThat(response)
				.extracting("sendMessageIds")
				.asList()
				.hasSize(1),
			() -> assertThat(fcmRepository.findById(saveFcmToken.getId()).isEmpty()).isTrue()
		);
	}

	@DisplayName("사용자는 토큰을 등록하지 않은 상태로 알림 발송시 전송되지 않는다")
	@Test
	void sendNotification_whenEmptyToken_thenDoNotSendNotification() {
		// given
		Member member = memberRepository.save(createMember());
		MemberNotificationSendRequest request = MemberNotificationSendRequest.builder()
			.title("포트폴리오")
			.body("포트폴리오1의 최대 손실율을 초과했습니다")
			.type("portfolio")
			.referenceId("1")
			.build();

		// when
		MemberNotificationSendResponse response = notificationService.sendNotificationToUsers(member.getId(), request);

		// then
		assertAll(
			() -> assertThat(response)
				.extracting("title", "content", "isRead", "type", "referenceId")
				.containsExactly("포트폴리오", "포트폴리오1의 최대 손실율을 초과했습니다", false, "portfolio", "1"),
			() -> assertThat(response)
				.extracting("sendMessageIds")
				.asList()
				.hasSize(0),
			() -> assertThat(notificationRepository.findAllByMemberId(member.getId()))
				.hasSize(0)
		);
	}

	private Member createMember() {
		return Member.builder()
			.nickname("일개미1234")
			.email("dragonbead95@naver.com")
			.password("kim1234@")
			.provider("local")
			.build();
	}

	private List<Notification> createNotifications(Member member) {
		Notification notification1 = createNotification(
			"지정가",
			"삼성전자가 지정가 KRW60000에 도달했습니다",
			true,
			LocalDateTime.of(2024, 1, 22, 10, 10, 10),
			"stock",
			"005930",
			member
		);
		Notification notification2 = createNotification(
			"포트폴리오",
			"포트폴리오1의 목표 수익률을 달성했습니다",
			false,
			LocalDateTime.of(2024, 1, 23, 10, 10, 10),
			"portfolio",
			"1",
			member
		);
		Notification notification3 = createNotification(
			"포트폴리오",
			"포트폴리오2의 최대 손실율을 초과했습니다",
			false,
			LocalDateTime.of(2024, 1, 24, 10, 10, 10),
			"portfolio",
			"2",
			member
		);
		return List.of(notification1, notification2, notification3);
	}

	private Notification createNotification(String title, String content, boolean isRead, LocalDateTime createAt,
		String type, String referenceId, Member member) {
		return Notification.builder()
			.title(title)
			.content(content)
			.isRead(isRead)
			.createAt(createAt)
			.type(type)
			.referenceId(referenceId)
			.member(member)
			.build();
	}

	private FcmToken createFcmToken(Member member) {
		return createFcmToken(member,
			"fahY76rRwq8HGy0m1lwckx:APA91bEovbLJyqdSRq8MWDbsIN8sbk90JiNHbIBs6rDoiOKeC-aa5P1QydiRa6okGrIZELrxx_cYieWUN44iX-AD6jma-cYRUR7e3bTMXwkqZFLRZh5s7-bcksGniB7Y2DkoONHtSjos");
	}

	private FcmToken createFcmToken(Member member, String fcmToken) {
		return FcmToken.builder()
			.token(fcmToken)
			.latestActivationTime(LocalDateTime.now())
			.member(member)
			.build();
	}
}
