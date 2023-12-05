package codesquad.fineants.spring.api.member.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.jwt.Jwt;
import codesquad.fineants.domain.jwt.JwtProperties;
import codesquad.fineants.domain.jwt.JwtProvider;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.oauth.client.OauthClient;
import codesquad.fineants.domain.oauth.repository.OauthClientRepository;
import codesquad.fineants.domain.oauth.support.AuthPrincipalArgumentResolver;
import codesquad.fineants.spring.api.errors.handler.GlobalExceptionHandler;
import codesquad.fineants.spring.api.member.request.AuthorizationRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberLoginServiceRequest;
import codesquad.fineants.spring.api.member.response.OauthCreateUrlResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberLoginResponse;
import codesquad.fineants.spring.api.member.service.MemberService;
import codesquad.fineants.spring.config.JpaAuditingConfiguration;
import codesquad.fineants.spring.config.OauthConfig;

@ActiveProfiles("test")
@WebMvcTest(controllers = MemberRestController.class)
@ImportAutoConfiguration(OauthConfig.class)
@Import({JwtProvider.class, JwtProperties.class})
@MockBean(JpaAuditingConfiguration.class)
class MemberRestControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private MemberRestController memberRestController;

	@Autowired
	private GlobalExceptionHandler globalExceptionHandler;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private OauthClientRepository oauthClientRepository;

	@Autowired
	private JwtProvider jwtProvider;

	@MockBean
	private MemberService memberService;

	@MockBean
	private AuthPrincipalArgumentResolver authPrincipalArgumentResolver;

	private Member member;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(memberRestController)
			.setControllerAdvice(globalExceptionHandler)
			.setCustomArgumentResolvers(authPrincipalArgumentResolver)
			.alwaysDo(print())
			.build();

		member = Member.builder()
			.id(1L)
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.provider("local")
			.password("kim1234@")
			.profileUrl("profileValue")
			.build();
	}

	@DisplayName("클라이언트는 소셜 로그인을 위한 인가 코드 URL을 요청한다")
	@Test
	void authorizationCodeURL() throws Exception {
		// given
		String url = "/api/auth/kakao/authUrl";
		OauthClient oauthClient = oauthClientRepository.findOneBy("kakao");
		String clientId = oauthClient.getClientId();
		String redirectUrl = oauthClient.getRedirectUri();
		String expectedAuthURL = String.format("https://kauth.kakao.com/oauth/authorize?"
			+ "response_type=code"
			+ "&client_id=%s"
			+ "&redirect_uri=%s"
			+ "&scope=openid"
			+ "&state=1234"
			+ "&nonce=1234"
			+ "&code_challenge=LpAzxsJ6VeWDwCNWdhDF6CypNrZlJnXYxhr4PPbkilU"
			+ "&code_challenge_method=S256", clientId, redirectUrl);

		OauthCreateUrlResponse mockResponse = new OauthCreateUrlResponse(expectedAuthURL,
			AuthorizationRequest.of("1234", "codeVerifier", "codeChallenge", "1234"));

		given(memberService.createAuthorizationCodeURL(
			anyString()
		)).willReturn(mockResponse);

		// when
		mockMvc.perform(post(url))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("인가 코드 URL 요청에 성공하였습니다")))
			.andExpect(jsonPath("data.authURL").value(equalTo(expectedAuthURL)));
	}

	@DisplayName("OAuth 서버로부터 리다이렉트 실행되어 로그인을 수행한다")
	@Test
	void login() throws Exception {
		// given
		String code = "1234";
		String redirectUrl = "http://localhost:5173/signin?provider=kakao";
		String state = "1234";
		String url = "/api/auth/kakao/login";

		Jwt jwt = jwtProvider.createJwtBasedOnMember(member, LocalDateTime.now());
		OauthMemberLoginResponse mockResponse = OauthMemberLoginResponse.of(jwt, member);
		given(memberService.login(ArgumentMatchers.any(OauthMemberLoginServiceRequest.class))).willReturn(mockResponse);
		// when
		mockMvc.perform(post(url)
				.param("code", code)
				.param("redirectUrl", redirectUrl)
				.param("state", state))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("로그인에 성공하였습니다")))
			.andExpect(jsonPath("data.jwt.accessToken").value(equalTo(jwt.getAccessToken())))
			.andExpect(jsonPath("data.jwt.refreshToken").value(equalTo(jwt.getRefreshToken())))
			.andExpect(jsonPath("data.user.id").value(equalTo(member.getId().intValue())))
			.andExpect(jsonPath("data.user.nickname").value(equalTo(member.getNickname())))
			.andExpect(jsonPath("data.user.email").value(equalTo(member.getEmail())))
			.andExpect(jsonPath("data.user.profileUrl").value(equalTo(member.getProfileUrl())));
	}
}
