package codesquad.fineants.spring.docs.notification;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.snippet.Attributes;

import codesquad.fineants.domain.notification.type.NotificationType;
import codesquad.fineants.spring.api.notification.controller.NotificationRestController;
import codesquad.fineants.spring.api.notification.request.NotificationCreateRequest;
import codesquad.fineants.spring.api.notification.response.NotificationCreateResponse;
import codesquad.fineants.spring.api.notification.response.NotifyPortfolioMessageItem;
import codesquad.fineants.spring.api.notification.response.NotifyPortfolioMessagesResponse;
import codesquad.fineants.spring.api.notification.service.NotificationService;
import codesquad.fineants.spring.docs.RestDocsSupport;
import codesquad.fineants.spring.util.ObjectMapperUtil;

class NotificationRestControllerDocsTest extends RestDocsSupport {

	private final NotificationService service = Mockito.mock(NotificationService.class);

	@Override
	protected Object initController() {
		return new NotificationRestController(service);
	}

	@DisplayName("사용자는 알림을 생성한다")
	@Test
	void createNotification() throws Exception {
		// given
		Map<String, Object> body = Map.of(
			"portfolioName", "포트폴리오1",
			"title", "포트폴리오",
			"type", "PORTFOLIO_TARGET_GAIN",
			"referenceId", "1"
		);

		Long notificationId = 1L;
		String title = "포트폴리오";
		boolean isRead = false;
		NotificationType type = NotificationType.PORTFOLIO_TARGET_GAIN;
		String referenceId = "1";
		given(service.createNotification(
			ArgumentMatchers.any(NotificationCreateRequest.class),
			anyLong()))
			.willReturn(NotificationCreateResponse.builder()
				.notificationId(notificationId)
				.title(title)
				.isRead(isRead)
				.type(type)
				.referenceId(referenceId)
				.build());
		// when
		mockMvc.perform(post("/api/notifications")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo("알림이 생성되었습니다")))
			.andExpect(jsonPath("data.notificationId").value(equalTo(notificationId.intValue())))
			.andExpect(jsonPath("data.title").value(equalTo(title)))
			.andExpect(jsonPath("data.isRead").value(equalTo(false)))
			.andExpect(jsonPath("data.type").value(equalTo(type.name())))
			.andExpect(jsonPath("data.referenceId").value(equalTo(referenceId)))
			.andDo(
				document(
					"notification-create",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestFields(
						fieldWithPath("portfolioName").type(JsonFieldType.STRING)
							.description("포트폴리오 이름"),
						fieldWithPath("title").type(JsonFieldType.STRING)
							.description("알림 제목"),
						fieldWithPath("type").type(JsonFieldType.STRING)
							.attributes(
								Attributes.key("constraints").value(
									String.join(",",
										NotificationType.PORTFOLIO_TARGET_GAIN.name(),
										NotificationType.PORTFOLIO_MAX_LOSS.name(),
										NotificationType.STOCK_TARGET_PRICE.name()
									)
								)
							)
							.description("알림 타입"),
						fieldWithPath("referenceId").type(JsonFieldType.STRING)
							.description("참조 등록번호")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.OBJECT)
							.description("응답 데이터"),
						fieldWithPath("data.notificationId").type(JsonFieldType.NUMBER)
							.description("알림 등록번호"),
						fieldWithPath("data.title").type(JsonFieldType.STRING)
							.description("알림 제목"),
						fieldWithPath("data.isRead").type(JsonFieldType.BOOLEAN)
							.description("읽음 여부"),
						fieldWithPath("data.type").type(JsonFieldType.STRING)
							.description("알림 타입"),
						fieldWithPath("data.referenceId").type(JsonFieldType.STRING)
							.description("참조 등록번호")
					)
				)
			);
	}

	@DisplayName("사용자는 한 포트폴리오의 목표 수익률 도달 알림을 전송받습니다")
	@Test
	void notifyPortfolioTargetGainMessages() throws Exception {
		// given
		Long notificationId = 1L;
		String title = "포트폴리오";
		boolean isRead = false;
		NotificationType type = NotificationType.PORTFOLIO_TARGET_GAIN;
		String referenceId = "1";
		String messageId = "projects/fineants-404407/messages/4754d355-5d5d-4f14-a642-75fecdb91fa5";
		given(service.notifyPortfolioTargetGainMessages(
			anyLong(),
			anyLong()))
			.willReturn(NotifyPortfolioMessagesResponse.builder()
				.notifications(List.of(
					NotifyPortfolioMessageItem.builder()
						.notificationId(notificationId)
						.title(title)
						.isRead(isRead)
						.type(type)
						.referenceId(referenceId)
						.messageId(messageId)
						.build()
				))
				.build());

		Long portfolioId = 1L;
		// when
		mockMvc.perform(post("/api/notifications/portfolios/{portfolioId}/notify/target-gain", portfolioId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 목표 수익률 알림 메시지가 전송되었습니다")))
			.andExpect(jsonPath("data.notifications[0].notificationId").value(equalTo(notificationId.intValue())))
			.andExpect(jsonPath("data.notifications[0].title").value(equalTo(title)))
			.andExpect(jsonPath("data.notifications[0].isRead").value(equalTo(false)))
			.andExpect(jsonPath("data.notifications[0].type").value(equalTo(type.name())))
			.andExpect(jsonPath("data.notifications[0].referenceId").value(equalTo(referenceId)))
			.andExpect(jsonPath("data.notifications[0].messageId").value(equalTo(messageId)))
			.andDo(
				document(
					"notification_portfolio_target_gain-notify",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					pathParameters(
						parameterWithName("portfolioId").description("포트폴리오 등록번호")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.OBJECT)
							.description("응답 데이터"),
						fieldWithPath("data.notifications[].notificationId").type(JsonFieldType.NUMBER)
							.description("알림 등록번호"),
						fieldWithPath("data.notifications[].title").type(JsonFieldType.STRING)
							.description("알림 제목"),
						fieldWithPath("data.notifications[].isRead").type(JsonFieldType.BOOLEAN)
							.description("읽음 여부"),
						fieldWithPath("data.notifications[].type").type(JsonFieldType.STRING)
							.description("알림 타입"),
						fieldWithPath("data.notifications[].referenceId").type(JsonFieldType.STRING)
							.description("참조 등록번호"),
						fieldWithPath("data.notifications[].messageId").type(JsonFieldType.STRING)
							.description("알림 메시지 등록번호")
					)
				)
			);
	}

	@DisplayName("사용자는 한 포트폴리오의 최대 손실율 도달 알림을 전송받습니다")
	@Test
	void notifyPortfolioMaxLossMessages() throws Exception {
		// given
		Long notificationId = 1L;
		String title = "포트폴리오";
		boolean isRead = false;
		NotificationType type = NotificationType.PORTFOLIO_MAX_LOSS;
		String referenceId = "1";
		String messageId = "projects/fineants-404407/messages/4754d355-5d5d-4f14-a642-75fecdb91fa5";
		given(service.notifyPortfolioMaxLossMessages(
			anyLong(),
			anyLong()))
			.willReturn(NotifyPortfolioMessagesResponse.builder()
				.notifications(List.of(
					NotifyPortfolioMessageItem.builder()
						.notificationId(notificationId)
						.title(title)
						.isRead(isRead)
						.type(type)
						.referenceId(referenceId)
						.messageId(messageId)
						.build()
				))
				.build());

		Long portfolioId = 1L;
		// when
		mockMvc.perform(post("/api/notifications/portfolios/{portfolioId}/notify/max-loss", portfolioId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 최대 손실율 알림 메시지가 전송되었습니다")))
			.andExpect(jsonPath("data.notifications[0].notificationId").value(equalTo(notificationId.intValue())))
			.andExpect(jsonPath("data.notifications[0].title").value(equalTo(title)))
			.andExpect(jsonPath("data.notifications[0].isRead").value(equalTo(false)))
			.andExpect(jsonPath("data.notifications[0].type").value(equalTo(type.name())))
			.andExpect(jsonPath("data.notifications[0].referenceId").value(equalTo(referenceId)))
			.andExpect(jsonPath("data.notifications[0].messageId").value(equalTo(messageId)))
			.andDo(
				document(
					"notification_portfolio_max_loss-notify",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					pathParameters(
						parameterWithName("portfolioId").description("포트폴리오 등록번호")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.OBJECT)
							.description("응답 데이터"),
						fieldWithPath("data.notifications[].notificationId").type(JsonFieldType.NUMBER)
							.description("알림 등록번호"),
						fieldWithPath("data.notifications[].title").type(JsonFieldType.STRING)
							.description("알림 제목"),
						fieldWithPath("data.notifications[].isRead").type(JsonFieldType.BOOLEAN)
							.description("읽음 여부"),
						fieldWithPath("data.notifications[].type").type(JsonFieldType.STRING)
							.description("알림 타입"),
						fieldWithPath("data.notifications[].referenceId").type(JsonFieldType.STRING)
							.description("참조 등록번호"),
						fieldWithPath("data.notifications[].messageId").type(JsonFieldType.STRING)
							.description("알림 메시지 등록번호")
					)
				)
			);
	}
}
