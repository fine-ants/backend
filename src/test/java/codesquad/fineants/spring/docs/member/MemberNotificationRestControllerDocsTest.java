package codesquad.fineants.spring.docs.member;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import codesquad.fineants.domain.notification.type.NotificationType;
import codesquad.fineants.spring.api.member.controller.MemberNotificationRestController;
import codesquad.fineants.spring.api.member.request.MemberNotificationSendRequest;
import codesquad.fineants.spring.api.member.response.MemberNotificationSendResponse;
import codesquad.fineants.spring.api.member.service.MemberNotificationPreferenceService;
import codesquad.fineants.spring.api.member.service.MemberNotificationService;
import codesquad.fineants.spring.docs.RestDocsSupport;
import codesquad.fineants.spring.util.ObjectMapperUtil;

public class MemberNotificationRestControllerDocsTest extends RestDocsSupport {

	private final MemberNotificationService service = Mockito.mock(MemberNotificationService.class);
	private final MemberNotificationPreferenceService preferenceService = Mockito.mock(
		MemberNotificationPreferenceService.class);

	@Override
	protected Object initController() {
		return new MemberNotificationRestController(service, preferenceService);
	}

	@DisplayName("회원 알림 메시지 발송 API")
	@Test
	void sendNotification() throws Exception {
		// given
		String title = "포트폴리오";
		NotificationType type = NotificationType.PORTFOLIO_MAX_LOSS;
		String referenceId = "1";
		String messageId = "projects/fineants-404407/messages/4754d355-5d5d-4f14-a642-75fecdb91fa5";
		given(service.sendNotification(anyLong(),
			ArgumentMatchers.any(MemberNotificationSendRequest.class)))
			.willReturn(MemberNotificationSendResponse.builder()
				.notificationId(1L)
				.title(title)
				.content("포트폴리오 최대 손실율에 도달")
				.timestamp(LocalDateTime.now())
				.isRead(false)
				.type(type.name())
				.referenceId(referenceId)
				.sendMessageIds(List.of(messageId))
				.build());

		Map<String, Object> body = Map.of(
			"title", "포트폴리오",
			"name", "포트폴리오2",
			"target", "최대 손실율",
			"type", "portfolio",
			"referenceId", "2",
			"messageId", "messageId"
		);
		// when
		mockMvc.perform(post("/api/members/{memberId}/notifications", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body))
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("알림 메시지 생성 및 전송이 완료되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)))
			.andDo(
				document(
					"member_notification-notify",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					pathParameters(
						parameterWithName("memberId").description("회원 등록번호")
					),
					requestFields(
						fieldWithPath("title").type(JsonFieldType.STRING).description("알림 제목"),
						fieldWithPath("name").type(JsonFieldType.STRING).description("알림 이름"),
						fieldWithPath("target").type(JsonFieldType.STRING).description("알림 타겟"),
						fieldWithPath("type").type(JsonFieldType.STRING).description("알림 타입"),
						fieldWithPath("referenceId").type(JsonFieldType.STRING).description("알림 참조 아이디"),
						fieldWithPath("messageId").type(JsonFieldType.STRING).description("메시지 아이디")
							.optional()
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.NULL)
							.description("응답 데이터")
					)
				)
			);
	}
}
