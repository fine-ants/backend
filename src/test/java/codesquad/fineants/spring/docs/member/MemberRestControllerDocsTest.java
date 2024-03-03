package codesquad.fineants.spring.docs.member;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.snippet.Attributes;

import codesquad.fineants.domain.jwt.Jwt;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.notification.type.NotificationType;
import codesquad.fineants.spring.api.member.controller.MemberRestController;
import codesquad.fineants.spring.api.member.request.OauthMemberLoginRequest;
import codesquad.fineants.spring.api.member.response.OauthMemberLoginResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberResponse;
import codesquad.fineants.spring.api.member.service.MemberService;
import codesquad.fineants.spring.docs.RestDocsSupport;

public class MemberRestControllerDocsTest extends RestDocsSupport {

	private final MemberService memberService = Mockito.mock(MemberService.class);

	@Override
	protected Object initController() {
		return new MemberRestController(memberService);
	}

	@DisplayName("OAuth 서버로부터 리다이렉트 실행되어 로그인을 수행한다")
	@Test
	void login() throws Exception {
		// given
		Member member = createMember();
		String code = "1234";
		String redirectUrl = "http://localhost:5173/signin?provider=kakao";
		String state = "1234";
		String url = "/api/auth/kakao/login";

		// Jwt jwt = jwtProvider.createJwtBasedOnMember(member, LocalDateTime.now());
		OauthMemberLoginResponse mockResponse = OauthMemberLoginResponse.builder()
			.jwt(Jwt.builder()
				.accessToken("accessToken")
				.refreshToken("refreshToken")
				.build())
			.user(OauthMemberResponse.from(member))
			.build();
		given(memberService.login(ArgumentMatchers.any(OauthMemberLoginRequest.class)))
			.willReturn(mockResponse);
		// when
		mockMvc.perform(post(url)
				.param("code", code)
				.param("redirectUrl", redirectUrl)
				.param("state", state))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("로그인에 성공하였습니다")))
			.andExpect(jsonPath("data.jwt.accessToken").value(equalTo("accessToken")))
			.andExpect(jsonPath("data.jwt.refreshToken").value(equalTo("refreshToken")))
			.andExpect(jsonPath("data.user.id").value(equalTo(member.getId().intValue())))
			.andExpect(jsonPath("data.user.nickname").value(equalTo(member.getNickname())))
			.andExpect(jsonPath("data.user.email").value(equalTo(member.getEmail())))
			.andExpect(jsonPath("data.user.profileUrl").value(equalTo(member.getProfileUrl())))
			.andDo(
				document(
					"member-login",
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
}
