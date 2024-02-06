package codesquad.fineants.spring.api.member.service;

import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
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
	void readNotifications() {
		// given
		Member member = memberRepository.save(createMember());
		Notification notification1 = notificationRepository.save(Notification.builder()
			.title("지정가")
			.content("삼성전자가 지정가 KRW60000에 도달했습니다")
			.isRead(true)
			.createAt(LocalDateTime.of(2024, 1, 22, 10, 10, 10))
			.type("stock")
			.referenceId("005930")
			.member(member)
			.build());
		Notification notification2 = notificationRepository.save(Notification.builder()
			.title("포트폴리오")
			.content("포트폴리오1의 목표 수익률을 달성했습니다")
			.isRead(false)
			.createAt(LocalDateTime.of(2024, 1, 23, 10, 10, 10))
			.type("portfolio")
			.referenceId("1")
			.member(member)
			.build());
		Notification notification3 = notificationRepository.save(Notification.builder()
			.title("포트폴리오")
			.content("포트폴리오2의 최대 손실율을 초과했습니다")
			.isRead(false)
			.createAt(LocalDateTime.of(2024, 1, 24, 10, 10, 10))
			.type("portfolio")
			.referenceId("2")
			.member(member)
			.build());

		// when
		MemberNotificationResponse response = notificationService.readNotifications(member.getId());

		// then
		Assertions.assertThat(response)
			.extracting("notifications")
			.asList()
			.hasSize(3)
			.containsExactly(
				MemberNotification.builder()
					.notificationId(notification3.getId())
					.title("포트폴리오")
					.content("포트폴리오2의 최대 손실율을 초과했습니다")
					.timestamp(LocalDateTime.of(2024, 1, 24, 10, 10, 10))
					.isRead(false)
					.type("portfolio")
					.referenceId(notification3.getReferenceId())
					.build(),
				MemberNotification.builder()
					.notificationId(notification2.getId())
					.title("포트폴리오")
					.content("포트폴리오1의 목표 수익률을 달성했습니다")
					.timestamp(LocalDateTime.of(2024, 1, 23, 10, 10, 10))
					.isRead(false)
					.type("portfolio")
					.referenceId(notification2.getReferenceId())
					.build(),
				MemberNotification.builder()
					.notificationId(notification1.getId())
					.title("지정가")
					.content("삼성전자가 지정가 KRW60000에 도달했습니다")
					.timestamp(LocalDateTime.of(2024, 1, 22, 10, 10, 10))
					.isRead(true)
					.type("stock")
					.referenceId(notification1.getReferenceId())
					.build()
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

}
