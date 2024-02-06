package codesquad.fineants.spring.api.member.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.anyLong;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.MethodParameter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalArgumentResolver;
import codesquad.fineants.spring.api.errors.handler.GlobalExceptionHandler;
import codesquad.fineants.spring.api.member.response.MemberNotification;
import codesquad.fineants.spring.api.member.response.MemberNotificationResponse;
import codesquad.fineants.spring.api.member.service.MemberNotificationService;
import codesquad.fineants.spring.config.JpaAuditingConfiguration;

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
	void readNotifications() throws Exception {
		// given
		Member member = createMember();

		List<MemberNotification> mockNotifications = createNotifications();
		given(notificationService.readNotifications(anyLong()))
			.willReturn(new MemberNotificationResponse(mockNotifications));

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
				.content("포트폴리오2의 최대 손실율을 초과했습니다")
				.timestamp(LocalDateTime.of(2024, 1, 24, 10, 10, 10))
				.isRead(false)
				.type("portfolio")
				.referenceId("2")
				.build(),
			MemberNotification.builder()
				.notificationId(2L)
				.title("포트폴리오")
				.content("포트폴리오1의 목표 수익률을 달성했습니다")
				.timestamp(LocalDateTime.of(2024, 1, 23, 10, 10, 10))
				.isRead(false)
				.type("portfolio")
				.referenceId("1")
				.build(),
			MemberNotification.builder()
				.notificationId(1L)
				.title("지정가")
				.content("삼성전자가 지정가 KRW60000에 도달했습니다")
				.timestamp(LocalDateTime.of(2024, 1, 22, 10, 10, 10))
				.isRead(true)
				.type("stock")
				.referenceId("005930")
				.build());
	}
}
