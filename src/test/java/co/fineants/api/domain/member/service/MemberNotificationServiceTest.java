package co.fineants.api.domain.member.service;

import static co.fineants.api.domain.notification.domain.entity.type.NotificationType.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.member.domain.dto.response.MemberNotification;
import co.fineants.api.domain.member.domain.dto.response.MemberNotificationResponse;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.domain.entity.NotificationBody;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.global.errors.errorcode.MemberErrorCode;
import co.fineants.api.global.errors.errorcode.NotificationErrorCode;
import co.fineants.api.global.errors.exception.FineAntsException;
import co.fineants.api.global.errors.exception.NotFoundResourceException;

class MemberNotificationServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberNotificationService notificationService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@DisplayName("사용자는 회원 알림 목록을 조회합니다")
	@Test
	void searchMemberNotifications() {
		// given
		Member member = memberRepository.save(createMember());
		List<Notification> notifications = notificationRepository.saveAll(createNotifications(member));

		setAuthentication(member);
		// when
		MemberNotificationResponse response = notificationService.searchMemberNotifications(member.getId());

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

	@DisplayName("사용자는 다른 사용자의 알림 메시지들을 조회할 수 없습니다.")
	@Test
	void searchMemberNotifications_whenOtherMemberFetch_thenThrowException() {
		// given
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		notificationRepository.saveAll(createNotifications(member));

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(() -> notificationService.searchMemberNotifications(member.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage(MemberErrorCode.FORBIDDEN_MEMBER.getMessage());
	}

	@DisplayName("사용자는 알림 모두 읽습니다")
	@Test
	void fetchMemberNotifications() {
		// given
		Member member = memberRepository.save(createMember());
		List<Notification> notifications = notificationRepository.saveAll(createNotifications(member));
		List<Long> notificationIds = notifications.stream()
			.map(Notification::getId)
			.toList();

		setAuthentication(member);
		// when
		List<Long> readNotificationIds = notificationService.fetchMemberNotifications(member.getId(), notificationIds);

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
	void fetchMemberNotifications_whenNotExistNotificationIds_thenThrow404Error() {
		// given
		Member member = memberRepository.save(createMember());
		List<Notification> notifications = notificationRepository.saveAll(createNotifications(member));
		List<Long> notificationIds = notifications.stream()
			.map(Notification::getId)
			.collect(Collectors.toList());

		Long notExistNotificationId = 9999L;
		notificationIds.add(notExistNotificationId);

		setAuthentication(member);
		// when
		Throwable throwable = catchThrowable(
			() -> notificationService.fetchMemberNotifications(member.getId(), notificationIds));

		// then
		assertThat(throwable)
			.isInstanceOf(NotFoundResourceException.class)
			.hasMessage(NotificationErrorCode.NOT_FOUND_NOTIFICATION.getMessage());
	}

	@DisplayName("사용자는 다른 사용자의 알림을 읽음 처리할 수 없다")
	@Test
	void fetchMemberNotifications_whenOtherMemberRequest_thenThrowException() {
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));

		List<Notification> notifications = notificationRepository.saveAll(createNotifications(member));
		List<Long> notificationIds = notifications.stream()
			.map(Notification::getId)
			.toList();

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(
			() -> notificationService.fetchMemberNotifications(hacker.getId(), notificationIds));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage(MemberErrorCode.FORBIDDEN_MEMBER.getMessage());
	}

	@DisplayName("사용자는 알림을 전체 삭제합니다")
	@Test
	void deleteAllNotifications() {
		// given
		Member member = memberRepository.save(createMember());
		List<Notification> notifications = notificationRepository.saveAll(createNotifications(member));
		List<Long> notificationIds = notifications.stream()
			.map(Notification::getId)
			.toList();

		setAuthentication(member);
		// when
		List<Long> deletedAllNotifications = notificationService.deleteMemberNotifications(member.getId(),
			notificationIds);

		// then
		assertThat(deletedAllNotifications).hasSize(3);
		assertThat(notificationRepository.findAllByMemberIdAndIds(member.getId(), notificationIds)).isEmpty();
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

		setAuthentication(member);
		// when
		Throwable throwable = catchThrowable(() -> notificationService.deleteMemberNotifications(member.getId(),
			notificationIds));

		// then
		assertThat(throwable)
			.isInstanceOf(NotFoundResourceException.class)
			.hasMessage(NotificationErrorCode.NOT_FOUND_NOTIFICATION.getMessage());
	}

	@DisplayName("사용자는 다른 사용자의 알림 메시지를 제거할 수 없습니다")
	@Test
	void deleteAllNotifications_whenOtherMemberDelete_thenThrowException() {
		// given
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		List<Notification> notifications = notificationRepository.saveAll(createNotifications(member));
		List<Long> notificationIds = notifications.stream()
			.map(Notification::getId)
			.toList();

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(
			() -> notificationService.deleteMemberNotifications(member.getId(), notificationIds));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage(MemberErrorCode.FORBIDDEN_MEMBER.getMessage());
	}

	private List<Notification> createNotifications(Member member) {
		return List.of(
			Notification.stockTargetPriceNotification(
				"종목 지정가", "005930", "/stock/005930", member, List.of("messageId"), "삼성전자일반주",
				Money.won(60000L),
				1L
			),
			Notification.portfolio(
				"포트폴리오1",
				"포트폴리오",
				PORTFOLIO_TARGET_GAIN,
				"1",
				"/portfolio/1",
				1L,
				member,
				List.of("messageId")
			),
			Notification.portfolio(
				"포트폴리오2",
				"포트폴리오",
				PORTFOLIO_MAX_LOSS,
				"2",
				"/portfolio/1",
				2L,
				member,
				List.of("messageId")
			)
		);
	}
}
