package codesquad.fineants.docs.member;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;

import codesquad.fineants.docs.RestDocsSupport;
import codesquad.fineants.domain.member.controller.MemberNotificationRestController;
import codesquad.fineants.domain.member.domain.dto.response.MemberNotification;
import codesquad.fineants.domain.member.domain.dto.response.MemberNotificationResponse;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.service.MemberNotificationPreferenceService;
import codesquad.fineants.domain.member.service.MemberNotificationService;
import codesquad.fineants.domain.notification.domain.entity.PortfolioNotification;
import codesquad.fineants.domain.notification.domain.entity.StockTargetPriceNotification;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock_target_price.domain.entity.TargetPriceNotification;
import codesquad.fineants.global.util.ObjectMapperUtil;

public class MemberNotificationRestControllerDocsTest extends RestDocsSupport {

	private final MemberNotificationService service = Mockito.mock(MemberNotificationService.class);
	private final MemberNotificationPreferenceService preferenceService = Mockito.mock(
		MemberNotificationPreferenceService.class);

	@Override
	protected Object initController() {
		return new MemberNotificationRestController(service, preferenceService);
	}

	@DisplayName("회원 알림 목록 조회 API")
	@Test
	void fetchNotifications() throws Exception {
		// given
		Member member = createMember();
		Portfolio portfolio = createPortfolio(member);
		Stock stock = createSamsungStock();

		PortfolioNotification notification = createPortfolioTargetGainNotification(portfolio,
			member);
		MemberNotification memberNotification = MemberNotification.from(notification);

		TargetPriceNotification targetPriceNotification = createTargetPriceNotification(
			createStockTargetPrice(member, stock));
		StockTargetPriceNotification stockTargetPriceNotification = createStockTargetPriceNotification(
			targetPriceNotification, member);
		MemberNotification memberNotification2 = MemberNotification.from(stockTargetPriceNotification);

		given(service.fetchNotifications(member.getId()))
			.willReturn(MemberNotificationResponse.create(List.of(
				memberNotification,
				memberNotification2
			)));

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/members/{memberId}/notifications", member.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("현재 알림 목록 조회를 성공했습니다")))
			.andExpect(jsonPath("data.notifications[0].notificationId")
				.value(equalTo(memberNotification.getNotificationId().intValue())))
			.andExpect(jsonPath("data.notifications[0].title")
				.value(equalTo(memberNotification.getTitle())))
			.andExpect(jsonPath("data.notifications[0].body.name")
				.value(equalTo(memberNotification.getBody().getName())))
			.andExpect(jsonPath("data.notifications[0].body.target")
				.value(equalTo(memberNotification.getBody().getTarget())))
			.andExpect(jsonPath("data.notifications[0].timestamp").isNotEmpty())
			.andExpect(jsonPath("data.notifications[0].isRead")
				.value(equalTo(memberNotification.getIsRead())))
			.andExpect(jsonPath("data.notifications[0].type")
				.value(equalTo(memberNotification.getType())))
			.andExpect(jsonPath("data.notifications[0].referenceId")
				.value(equalTo(memberNotification.getReferenceId())))
			.andExpect(jsonPath("data.notifications[1].notificationId")
				.value(equalTo(memberNotification2.getNotificationId().intValue())))
			.andExpect(jsonPath("data.notifications[1].title")
				.value(equalTo(memberNotification2.getTitle())))
			.andExpect(jsonPath("data.notifications[1].body.name")
				.value(equalTo(memberNotification2.getBody().getName())))
			.andExpect(jsonPath("data.notifications[1].body.target")
				.value(equalTo(memberNotification2.getBody().getTarget())))
			.andExpect(jsonPath("data.notifications[1].timestamp").isNotEmpty())
			.andExpect(jsonPath("data.notifications[1].isRead")
				.value(equalTo(memberNotification2.getIsRead())))
			.andExpect(jsonPath("data.notifications[1].type")
				.value(equalTo(memberNotification2.getType())))
			.andExpect(jsonPath("data.notifications[1].referenceId")
				.value(equalTo(memberNotification2.getReferenceId())))
			.andDo(
				document(
					"member_notification-search",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					pathParameters(
						parameterWithName("memberId").description("회원 등록 번호")
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
						fieldWithPath("data.notifications").type(JsonFieldType.ARRAY)
							.description("알림 리스트"),
						fieldWithPath("data.notifications[].notificationId").type(JsonFieldType.NUMBER)
							.description("알림 등록 번호"),
						fieldWithPath("data.notifications[].title").type(JsonFieldType.STRING)
							.description("알림 제목"),
						fieldWithPath("data.notifications[].body").type(JsonFieldType.OBJECT)
							.description("알림 내용"),
						fieldWithPath("data.notifications[].body.name").type(JsonFieldType.STRING)
							.description("이름"),
						fieldWithPath("data.notifications[].body.target").type(JsonFieldType.STRING)
							.description("타겟"),
						fieldWithPath("data.notifications[].timestamp").type(JsonFieldType.STRING)
							.description("알림 생성 일자"),
						fieldWithPath("data.notifications[].isRead").type(JsonFieldType.BOOLEAN)
							.description("알림 읽음 여부"),
						fieldWithPath("data.notifications[].type").type(JsonFieldType.STRING)
							.description("알림 종류"),
						fieldWithPath("data.notifications[].referenceId").type(JsonFieldType.STRING)
							.description("알림 참조 아이디")
					)
				)
			);

	}

	@DisplayName("회원 알림 설정 수정 API")
	@Test
	void updateNotificationPreference() throws Exception {
		// given
		Member member = createMember();

		Map<String, Object> body = Map.of(
			"browserNotify", true,
			"targetGainNotify", true,
			"maxLossNotify", true,
			"targetPriceNotify", true,
			"fcmTokenId", 19L
		);

		// when & then
		mockMvc.perform(
				RestDocumentationRequestBuilders.put("/api/members/{memberId}/notification/settings", member.getId())
					.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
					.contentType(MediaType.APPLICATION_JSON)
					.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("알림 설정을 변경했습니다")))
			.andDo(
				document(
					"member_notification_settings-update",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					pathParameters(
						parameterWithName("memberId").description("회원 등록 번호")
					),
					requestFields(
						fieldWithPath("browserNotify").type(JsonFieldType.BOOLEAN).description("브라우저 계정 알림 활성화 여부"),
						fieldWithPath("targetGainNotify").type(JsonFieldType.BOOLEAN).description("목표 수익률 알림 활성화 여부"),
						fieldWithPath("maxLossNotify").type(JsonFieldType.BOOLEAN).description("최대 손실율 알림 활성화 여부"),
						fieldWithPath("targetPriceNotify").type(JsonFieldType.BOOLEAN).description("종목 지정가 알림 활성화 여부"),
						fieldWithPath("fcmTokenId").type(JsonFieldType.NUMBER).description("FCM 토큰 등록 번호").optional()
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

	@DisplayName("회원 알림 전체 삭제 API")
	@Test
	void deleteAllNotifications() throws Exception {
		// given
		Member member = createMember();
		Map<String, Object> body = Map.of(
			"notificationIds", List.of(1, 2)
		);

		// when & then
		mockMvc.perform(
				RestDocumentationRequestBuilders.delete("/api/members/{memberId}/notifications", member.getId())
					.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
					.contentType(MediaType.APPLICATION_JSON)
					.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("알림 전체 삭제를 성공하였습니다")))
			.andDo(
				document(
					"member_notification-all-delete",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					pathParameters(
						parameterWithName("memberId").description("회원 등록 번호")
					),
					requestFields(
						fieldWithPath("notificationIds").type(JsonFieldType.ARRAY).description("알림 등록 번호 리스트")
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

	@DisplayName("회원 알림 단일 삭제 API")
	@Test
	void deleteNotification() throws Exception {
		// given
		Member member = createMember();
		Long notificationId = 1L;

		// when & then
		mockMvc.perform(
				RestDocumentationRequestBuilders.delete("/api/members/{memberId}/notifications/{notificationId}",
						member.getId(), notificationId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("알림 삭제를 성공하였습니다")))
			.andDo(
				document(
					"member_notification-one-delete",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					pathParameters(
						parameterWithName("memberId").description("회원 등록 번호"),
						parameterWithName("notificationId").description("알림 등록 번호")
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

	@DisplayName("회원 알림 모두 읽음 API")
	@Test
	void readAllNotifications() throws Exception {
		// given
		Member member = createMember();

		Map<String, Object> body = Map.of(
			"notificationIds", List.of(1, 2)
		);

		// when & then
		mockMvc.perform(
				RestDocumentationRequestBuilders.patch("/api/members/{memberId}/notifications/", member.getId())
					.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
					.contentType(MediaType.APPLICATION_JSON)
					.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("알림을 모두 읽음 처리했습니다")))
			.andDo(
				document(
					"member_notification-all-read",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					pathParameters(
						parameterWithName("memberId").description("회원 등록 번호")
					),
					requestFields(
						fieldWithPath("notificationIds").type(JsonFieldType.ARRAY).description("알림 등록 번호 리스트")
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
