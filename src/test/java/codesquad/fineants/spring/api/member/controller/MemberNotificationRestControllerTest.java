package codesquad.fineants.spring.api.member.controller;

import static codesquad.fineants.domain.notification.type.NotificationType.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.anyList;
import static org.mockito.BDDMockito.anyLong;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.notification.NotificationBody;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalArgumentResolver;
import codesquad.fineants.spring.api.common.errors.handler.GlobalExceptionHandler;
import codesquad.fineants.spring.api.member.response.MemberNotification;
import codesquad.fineants.spring.api.member.response.MemberNotificationResponse;
import codesquad.fineants.spring.api.member.service.MemberNotificationPreferenceService;
import codesquad.fineants.spring.api.member.service.MemberNotificationService;
import codesquad.fineants.spring.config.JpaAuditingConfiguration;
import codesquad.fineants.spring.util.ObjectMapperUtil;

@ActiveProfiles("test")
@WebMvcTest(controllers = MemberNotificationRestController.class)
@MockBean(JpaAuditingConfiguration.class)
class MemberNotificationRestControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private MemberNotificationRestController memberNotificationRestController;

	@Autowired
	private GlobalExceptionHandler globalExceptionHandler;

	@MockBean
	private AuthPrincipalArgumentResolver authPrincipalArgumentResolver;

	@MockBean
	private MemberNotificationService notificationService;

	@MockBean
	private MemberNotificationPreferenceService preferenceService;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(memberNotificationRestController)
			.setControllerAdvice(globalExceptionHandler)
			.setCustomArgumentResolvers(authPrincipalArgumentResolver)
			.alwaysDo(print())
			.build();

		given(authPrincipalArgumentResolver.supportsParameter(ArgumentMatchers.any(MethodParameter.class)))
			.willReturn(true);
		given(authPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any()))
			.willReturn(AuthMember.from(createMember()));
	}

	@DisplayName("사용자는 알림 목록 조회합니다")
	@Test
	void fetchNotifications() throws Exception {
		// given
		Member member = createMember();

		List<MemberNotification> mockNotifications = createNotifications();
		given(notificationService.fetchNotifications(anyLong()))
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
		given(notificationService.readAllNotifications(anyLong(), anyList()))
			.willReturn(
				List.of(
					mockNotifications.get(0).getNotificationId(),
					mockNotifications.get(1).getNotificationId()
				)
			);

		List<Long> notificationIds = mockNotifications.stream()
			.map(MemberNotification::getNotificationId)
			.collect(Collectors.toList());
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
		given(notificationService.readAllNotifications(anyLong(), anyList()))
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
		given(notificationService.readAllNotifications(anyLong(), anyList()))
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

	@DisplayName("사용자는 특정 알림을 읽습니다")
	@Test
	void readNotification() throws Exception {
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
		given(notificationService.readAllNotifications(anyLong(), anyList()))
			.willReturn(List.of(mockNotification.getNotificationId()));

		// when & then
		mockMvc.perform(patch("/api/members/{memberId}/notifications/{notificationId}",
				member.getId(),
				mockNotification.getNotificationId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("알림을 모두 읽음 처리했습니다")));
	}

	@DisplayName("사용자는 알람을 모두 삭제합니다")
	@Test
	void deleteAllNotifications() throws Exception {
		// given
		Member member = createMember();

		List<MemberNotification> mockNotifications = createNotifications();
		List<Long> notificationIds = mockNotifications.stream()
			.map(MemberNotification::getNotificationId)
			.collect(Collectors.toList());
		given(notificationService.readAllNotifications(anyLong(), anyList()))
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
		given(notificationService.deleteAllNotifications(anyLong(), anyList()))
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

	private Member createMember() {
		return Member.builder()
			.id(1L)
			.nickname("일개미1234")
			.email("dragonbead95@naver.com")
			.provider("local")
			.password("password")
			.profileUrl("profileUrl")
			.build();
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
				.body(NotificationBody.stock("삼성전자", 60000L))
				.timestamp(LocalDateTime.of(2024, 1, 22, 10, 10, 10))
				.isRead(true)
				.type(STOCK_TARGET_PRICE.getCategory())
				.referenceId("005930")
				.build());
	}
}
