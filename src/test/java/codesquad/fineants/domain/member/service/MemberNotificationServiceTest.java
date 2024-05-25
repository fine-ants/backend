package codesquad.fineants.domain.member.service;

import static codesquad.fineants.domain.notification.domain.entity.type.NotificationType.*;
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

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.fcm.repository.FcmRepository;
import codesquad.fineants.domain.member.domain.dto.response.MemberNotification;
import codesquad.fineants.domain.member.domain.dto.response.MemberNotificationResponse;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.notification.domain.entity.Notification;
import codesquad.fineants.domain.notification.domain.entity.NotificationBody;
import codesquad.fineants.domain.notification.repository.NotificationRepository;
import codesquad.fineants.global.errors.errorcode.NotificationErrorCode;
import codesquad.fineants.global.errors.exception.NotFoundResourceException;

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
					.body(NotificationBody.portfolio("포트폴리오2", PORTFOLIO_MAX_LOSS))
					.timestamp(LocalDateTime.of(2024, 1, 24, 10, 10, 10))
					.isRead(false)
					.type(PORTFOLIO_MAX_LOSS.getCategory())
					.referenceId(notifications.get(2).getReferenceId())
					.build(),
				MemberNotification.builder()
					.notificationId(notifications.get(1).getId())
					.title("포트폴리오")
					.body(NotificationBody.portfolio("포트폴리오1", PORTFOLIO_TARGET_GAIN))
					.timestamp(LocalDateTime.of(2024, 1, 23, 10, 10, 10))
					.isRead(false)
					.type(PORTFOLIO_TARGET_GAIN.getCategory())
					.referenceId(notifications.get(1).getReferenceId())
					.build(),
				MemberNotification.builder()
					.notificationId(notifications.get(0).getId())
					.title("지정가")
					.body(NotificationBody.stock("삼성전자", Money.won(60000L)))
					.timestamp(LocalDateTime.of(2024, 1, 22, 10, 10, 10))
					.isRead(true)
					.type(STOCK_TARGET_PRICE.getCategory())
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
				.hasSize(3)
				.containsAll(notificationIds),
			() -> assertThat(notificationRepository.findAllById(Objects.requireNonNull(readNotificationIds))
				.stream()
				.allMatch(Notification::getIsRead))
				.isTrue()
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

	private List<Notification> createNotifications(Member member) {
		return List.of(
			Notification.stock(
				"삼성전자일반주",
				Money.won(60000L),
				"종목 지정가",
				"005930",
				"messageId",
				1L,
				member
			),
			Notification.portfolio(
				"포트폴리오1",
				"포트폴리오",
				PORTFOLIO_TARGET_GAIN,
				"1",
				"messageId",
				member
			),
			Notification.portfolio(
				"포트폴리오2",
				"포트폴리오",
				PORTFOLIO_MAX_LOSS,
				"2",
				"messageId",
				member
			)
		);
	}
}
