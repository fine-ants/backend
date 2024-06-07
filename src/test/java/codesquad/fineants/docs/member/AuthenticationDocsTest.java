package codesquad.fineants.docs.member;

import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletResponse;
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

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.domain.entity.MemberRole;
import codesquad.fineants.domain.member.domain.entity.Role;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.member.repository.MemberRoleRepository;
import codesquad.fineants.domain.member.repository.RoleRepository;
import codesquad.fineants.global.security.factory.TokenFactory;
import codesquad.fineants.global.security.oauth.dto.Token;
import codesquad.fineants.global.util.ObjectMapperUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;

@ExtendWith(RestDocumentationExtension.class)
public class AuthenticationDocsTest extends AbstractContainerBaseTest {
	protected MockMvc mockMvc;
	@Autowired
	private WebApplicationContext webApplicationContext;
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private MemberRoleRepository memberRoleRepository;

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

	@AfterEach
	void tearDown() {
		memberRoleRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
		roleRepository.deleteAllInBatch();
	}

	@DisplayName("사용자가 일반 로그인한다")
	@Test
	void login() throws Exception {
		// given
		Role userRole = roleRepository.save(Role.create("ROLE_USER", "유저"));
		Member member = memberRepository.save(createMember());
		MemberRole memberRole = MemberRole.create(member, userRole);
		member.addMemberRole(memberRole);
		memberRepository.save(member);

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
			.andExpect(cookie().httpOnly("accessToken", true))
			.andExpect(cookie().secure("accessToken", true))
			.andExpect(cookie().exists("refreshToken"))
			.andExpect(cookie().httpOnly("refreshToken", true))
			.andExpect(cookie().secure("refreshToken", true))
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
		TokenFactory tokenFactory = new TokenFactory(true);
		Cookie accessTokenCookie = new Cookie("accessToken", "accessToken");
		Cookie refreshTokenCookie = new Cookie("refreshToken", "refreshToken");
		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.get(url)
				.cookie(accessTokenCookie, refreshTokenCookie)
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

	@DisplayName("액세스 토큰 갱신 API")
	@Test
	void refreshAccessToken() throws Exception {
		// give
		Token token = processingLogin();

		Map<String, Object> body = Map.of(
			"refreshToken", token.getRefreshToken()
		);

		// when & then
		mockMvc.perform(post("/api/auth/refresh/token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("액세스 토큰 갱신에 성공하였습니다")))
			.andExpect(jsonPath("data.accessToken").isNotEmpty())
			.andDo(
				document(
					"member_access_token-refresh",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestFields(
						fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰")
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
						fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
							.description("액세스 토큰")
					)
				)
			);
	}

	private Token processingLogin() throws Exception {
		Role userRole = roleRepository.save(Role.create("ROLE_USER", "유저"));
		Member member = memberRepository.save(createMember());
		MemberRole memberRole = MemberRole.create(member, userRole);
		member.addMemberRole(memberRole);
		memberRepository.save(member);

		Map<String, Object> body = Map.of(
			"email", "dragonbead95@naver.com",
			"password", "nemo1234@"
		);
		MockHttpServletResponse response = mockMvc.perform(RestDocumentationRequestBuilders.post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse();
		String accessToken = response.getCookie("accessToken").getValue();
		String refreshToken = response.getCookie("refreshToken").getValue();
		return Token.create(accessToken, refreshToken);
	}
}
