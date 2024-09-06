package co.fineants.api.domain.member.controller;

import static co.fineants.api.domain.notification.domain.entity.type.NotificationType.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.member.domain.dto.response.MemberNotification;
import co.fineants.api.domain.member.domain.dto.response.MemberNotificationResponse;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.service.MemberNotificationPreferenceService;
import co.fineants.api.domain.member.service.MemberNotificationService;
import co.fineants.api.domain.notification.domain.entity.NotificationBody;
import co.fineants.api.global.util.ObjectMapperUtil;
import co.fineants.support.controller.ControllerTestSupport;

@WebMvcTest(controllers = MemberNotificationRestController.class)
class MemberNotificationRestControllerTest extends ControllerTestSupport {

	@MockBean
	private MemberNotificationService notificationService;

	@MockBean
	private MemberNotificationPreferenceService preferenceService;

	@Override
	protected Object initController() {
		return new MemberNotificationRestController(notificationService, preferenceService);
	}

	@DisplayName("사용자는 알림 목록 조회합니다")
	@Test
	void fetchNotifications() throws Exception {
		// given
		Member member = createMember();

		List<MemberNotification> mockNotifications = createNotifications();
		given(notificationService.searchMemberNotifications(anyLong()))
			.willReturn(MemberNotificationResponse.create(mockNotifications));

		// when & then
		mockMvc.perform(get("/api/members/{memberId}/notifications", member.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("현재 알림 목록 조회를 성공했습니다")))
			.andExpect(jsonPath("data.notifications").isArray())
			.andExpect(jsonPath("data.notifications[0].notificationId").value(equalTo(3)))
			.andExpect(jsonPath("data.notifications[1].notificationId").value(equalTo(2)))
			.andExpect(jsonPath("data.notifications[2].notificationId").value(equalTo(1)));
	}

	@DisplayName("사용자는 알림 모두 읽습니다")
	@Test
	void readAllNotifications() throws Exception {
		// given
		Member member = createMember();

		List<MemberNotification> mockNotifications = createNotifications();
		given(notificationService.fetchMemberNotifications(anyLong(), anyList()))
			.willReturn(
				List.of(
					mockNotifications.get(0).getNotificationId(),
					mockNotifications.get(1).getNotificationId()
				)
			);

		List<Long> notificationIds = mockNotifications.stream()
			.map(MemberNotification::getNotificationId)
			.toList();
		// when & then
		mockMvc.perform(patch("/api/members/{memberId}/notifications", member.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(Map.of("notificationIds", notificationIds))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("알림을 모두 읽음 처리했습니다")));
	}

	@DisplayName("사용자는 빈 리스트를 전달하여 알림을 읽을 수 없습니다")
	@Test
	void readAllNotifications_whenEmptyList_thenResponse400Error() throws Exception {
		// given
		Member member = createMember();

		List<MemberNotification> mockNotifications = createNotifications();
		given(notificationService.fetchMemberNotifications(anyLong(), anyList()))
			.willReturn(
				List.of(
					mockNotifications.get(0).getNotificationId(),
					mockNotifications.get(1).getNotificationId()
				)
			);

		List<Long> notificationIds = Collections.emptyList();
		// when & then
		mockMvc.perform(patch("/api/members/{memberId}/notifications", member.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(Map.of("notificationIds", notificationIds))))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")));
	}

	@DisplayName("사용자는 유효하지 않은 입력으로 알림을 읽을 수 없습니다")
	@Test
	void readAllNotifications_whenInvalidInput_thenResponse400Error() throws Exception {
		// given
		Member member = createMember();

		List<MemberNotification> mockNotifications = createNotifications();
		given(notificationService.fetchMemberNotifications(anyLong(), anyList()))
			.willReturn(
				List.of(
					mockNotifications.get(0).getNotificationId(),
					mockNotifications.get(1).getNotificationId()
				)
			);

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("notificationIds", null);
		// when & then
		mockMvc.perform(patch("/api/members/{memberId}/notifications", member.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(requestBodyMap)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")));
	}

	@DisplayName("사용자는 알람을 모두 삭제합니다")
	@Test
	void deleteAllNotifications() throws Exception {
		// given
		Member member = createMember();

		List<MemberNotification> mockNotifications = createNotifications();
		List<Long> notificationIds = mockNotifications.stream()
			.map(MemberNotification::getNotificationId)
			.toList();
		given(notificationService.fetchMemberNotifications(anyLong(), anyList()))
			.willReturn(notificationIds);

		// when & then
		mockMvc.perform(delete("/api/members/{memberId}/notifications",
				member.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(Map.of("notificationIds", notificationIds))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("알림 전체 삭제를 성공하였습니다")));
	}

	@DisplayName("사용자는 특정 알람을 삭제합니다")
	@Test
	void deleteNotification() throws Exception {
		// given
		Member member = createMember();

		MemberNotification mockNotification = MemberNotification.builder()
			.notificationId(3L)
			.title("포트폴리오")
			.body(NotificationBody.portfolio("포트폴리오2", PORTFOLIO_MAX_LOSS))
			.timestamp(LocalDateTime.of(2024, 1, 24, 10, 10, 10))
			.isRead(false)
			.type(PORTFOLIO_MAX_LOSS.getCategory())
			.referenceId("2")
			.build();
		given(notificationService.deleteMemberNotifications(anyLong(), anyList()))
			.willReturn(List.of(mockNotification.getNotificationId()));

		// when & then
		mockMvc.perform(delete("/api/members/{memberId}/notifications/{notificationId}",
				member.getId(),
				mockNotification.getNotificationId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("알림 삭제를 성공하였습니다")));
	}

	private List<MemberNotification> createNotifications() {
		return List.of(MemberNotification.builder()
				.notificationId(3L)
				.title("포트폴리오")
				.body(NotificationBody.portfolio("포트폴리오2", PORTFOLIO_MAX_LOSS))
				.timestamp(LocalDateTime.of(2024, 1, 24, 10, 10, 10))
				.isRead(false)
				.type(PORTFOLIO_MAX_LOSS.getCategory())
				.referenceId("2")
				.build(),
			MemberNotification.builder()
				.notificationId(2L)
				.title("포트폴리오")
				.body(NotificationBody.portfolio("포트폴리오1", PORTFOLIO_TARGET_GAIN))
				.timestamp(LocalDateTime.of(2024, 1, 23, 10, 10, 10))
				.isRead(false)
				.type(PORTFOLIO_TARGET_GAIN.getCategory())
				.referenceId("1")
				.build(),
			MemberNotification.builder()
				.notificationId(1L)
				.title("지정가")
				.body(NotificationBody.stock("삼성전자", Money.won(60000L)))
				.timestamp(LocalDateTime.of(2024, 1, 22, 10, 10, 10))
				.isRead(true)
				.type(STOCK_TARGET_PRICE.getCategory())
				.referenceId("005930")
				.build());
	}
}
