package codesquad.fineants.spring.member;

import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import codesquad.fineants.spring.api.member.controller.MemberRestController;
import codesquad.fineants.spring.api.member.service.MemberService;
import codesquad.fineants.spring.docs.RestDocsSupport;
import codesquad.fineants.spring.util.ObjectMapperUtil;

public class MemberRestControllerDocsTest extends RestDocsSupport {

	private final MemberService service = Mockito.mock(MemberService.class);

	@Override
	protected Object initController() {
		return new MemberRestController(service);
	}

	@DisplayName("사용자 계정 삭제 API")
	@Test
	void deleteAccount() throws Exception {
		// given
		Map<String, Object> body = Map.of(
			"refreshToken", "refreshToken"
		);

		// when & then
		mockMvc.perform(delete("/api/account")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("계정이 삭제되었습니다")))
			.andDo(
				document(
					"member-delete",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestFields(
						fieldWithPath("refreshToken").type(JsonFieldType.STRING)
							.description("리프레시 토큰")
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
