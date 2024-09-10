package co.fineants.api.docs.member;

import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.request.RequestDocumentation;
import org.springframework.restdocs.snippet.Attributes;
import org.springframework.security.config.BeanIds;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.member.repository.MemberRoleRepository;
import co.fineants.api.domain.member.repository.RoleRepository;
import co.fineants.api.global.util.ObjectMapperUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import okhttp3.mockwebserver.MockWebServer;

@ExtendWith(RestDocumentationExtension.class)
public class AuthenticationDocsTest extends AbstractContainerBaseTest {
	public static MockWebServer mockWebServer;
	protected MockMvc mockMvc;
	@Autowired
	private WebApplicationContext webApplicationContext;
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private MemberRoleRepository memberRoleRepository;

	@BeforeAll
	static void setUp() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();
	}

	@AfterAll
	static void allTearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@BeforeEach
	void setUp(RestDocumentationContextProvider provider) throws ServletException {
		DelegatingFilterProxy delegatingFilterProxy = new DelegatingFilterProxy();
		delegatingFilterProxy.init(new MockFilterConfig(webApplicationContext.getServletContext(),
			BeanIds.SPRING_SECURITY_FILTER_CHAIN));
		this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
			.apply(MockMvcRestDocumentation.documentationConfiguration(provider))
			.addFilter(delegatingFilterProxy)
			.alwaysDo(print())
			.build();
	}

	@DisplayName("사용자가 일반 로그인한다")
	@Test
	void login() throws Exception {
		// given
		memberRepository.save(createMember());

		Map<String, Object> body = Map.of(
			"email", "dragonbead95@naver.com",
			"password", "nemo1234@"
		);
		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(cookie().exists("accessToken"))
			.andExpect(cookie().exists("refreshToken"))
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("로그인에 성공하였습니다.")))
			.andExpect(jsonPath("data").value(equalTo(null)))
			.andDo(
				document(
					"member-login",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestFields(
						fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
						fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
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

	@DisplayName("사용자는 Oauth 로그인을 요청한다")
	@Test
	void oauthLogin() throws Exception {
		// given

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/oauth2/authorization/{provider}", "naver")
				.queryParam("redirect_url", "http://localhost:8080/api/oauth/redirect"))
			.andExpect(status().is3xxRedirection())
			.andDo(
				document(
					"member_oauth-login",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					RequestDocumentation.queryParameters(
						parameterWithName("redirect_url").description("리다이렉트 URL")
					),
					pathParameters(
						parameterWithName("provider").description("플랫폼")
							.attributes(
								Attributes.key("constraints").value(
									String.join(",",
										"google", "kakao", "naver"
									)
								))
					)
				)
			);
	}

	@DisplayName("회원 로그아웃 API")
	@Test
	void logout() throws Exception {
		// given
		String url = "/api/auth/logout";
		Cookie[] cookies = createTokenCookies();

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.get(url)
				.cookie(cookies)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("로그아웃에 성공하였습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)))
			.andDo(
				document(
					"member-logout",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
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
