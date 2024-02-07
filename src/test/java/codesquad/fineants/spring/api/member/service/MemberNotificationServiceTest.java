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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.notification.Notification;
import codesquad.fineants.domain.notification.NotificationRepository;
import codesquad.fineants.spring.api.member.response.MemberNotification;
import codesquad.fineants.spring.api.member.response.MemberNotificationResponse;

@ActiveProfiles("test")
@SpringBootTest
class MemberNotificationServiceTest {

	@Autowired
	private MemberNotificationService notificationService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@AfterEach
	void tearDown() {
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
					.content("포트폴리오2의 최대 손실율을 초과했습니다")
					.timestamp(LocalDateTime.of(2024, 1, 24, 10, 10, 10))
					.isRead(false)
					.type("portfolio")
					.referenceId(notifications.get(2).getReferenceId())
					.build(),
				MemberNotification.builder()
					.notificationId(notifications.get(1).getId())
					.title("포트폴리오")
					.content("포트폴리오1의 목표 수익률을 달성했습니다")
					.timestamp(LocalDateTime.of(2024, 1, 23, 10, 10, 10))
					.isRead(false)
					.type("portfolio")
					.referenceId(notifications.get(1).getReferenceId())
					.build(),
				MemberNotification.builder()
					.notificationId(notifications.get(0).getId())
					.title("지정가")
					.content("삼성전자가 지정가 KRW60000에 도달했습니다")
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
		assertThat(notificationRepository.findAllByMemberIdAndIds(member.getId(), notificationIds)
			.stream()
			.allMatch(Notification::getIsDeleted)).isTrue();
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

}
