package codesquad.fineants.docs.fcm;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.anyLong;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import codesquad.fineants.docs.RestDocsSupport;
import codesquad.fineants.domain.fcm.controller.FcmRestController;
import codesquad.fineants.domain.fcm.domain.dto.request.FcmRegisterRequest;
import codesquad.fineants.domain.fcm.domain.dto.response.FcmDeleteResponse;
import codesquad.fineants.domain.fcm.domain.dto.response.FcmRegisterResponse;
import codesquad.fineants.domain.fcm.service.FcmService;
import codesquad.fineants.global.util.ObjectMapperUtil;

class FcmRestControllerDocsTest extends RestDocsSupport {

	private final FcmService fcmService = Mockito.mock(FcmService.class);

	@Override
	protected Object initController() {
		return new FcmRestController(fcmService);
	}

	@DisplayName("FCM 토큰 추가 API")
	@Test
	void createToken() throws Exception {
		// given
		BDDMockito.given(fcmService.createToken(any(FcmRegisterRequest.class), anyLong()))
			.willReturn(FcmRegisterResponse.builder()
				.fcmTokenId(1L)
				.build());

		Map<String, String> body = Map.of("fcmToken", "fcmToken");

		// when & then
		mockMvc.perform(post("/api/fcm/tokens")
				.content(ObjectMapperUtil.serialize(body))
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(createTokenCookies()))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo("FCM 토큰을 성공적으로 등록하였습니다")))
			.andExpect(jsonPath("data.fcmTokenId").value(equalTo(1)))
			.andDo(
				document(
					"fcm-create",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestFields(
						fieldWithPath("fcmToken").type(JsonFieldType.STRING)
							.description("FCM 토큰")
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
						fieldWithPath("data.fcmTokenId").type(JsonFieldType.NUMBER)
							.description("FCM 토큰 등록번호")
					)
				)
			);
	}

	@DisplayName("FCM 토큰 삭제 API")
	@Test
	void deleteToken() throws Exception {
		// given
		Long fcmTokenId = 1L;
		BDDMockito.given(fcmService.deleteToken(anyLong()))
			.willReturn(FcmDeleteResponse.builder()
				.fcmTokenId(fcmTokenId)
				.build());

		// when & then
		mockMvc.perform(delete("/api/fcm/tokens/{fcmTokenId}", fcmTokenId)
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("FCM 토큰을 성공적으로 삭제하였습니다")))
			.andDo(
				document(
					"fcm-delete",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					pathParameters(
						parameterWithName("fcmTokenId").description("FCM 토큰 등록번호")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(nullValue())
							.description("응답 데이터")
					)
				)
			);
	}
}
