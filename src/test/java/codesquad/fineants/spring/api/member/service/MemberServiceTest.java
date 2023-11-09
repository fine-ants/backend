package codesquad.fineants.spring.api.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.jwt.Jwt;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.client.OauthClientRandomGenerator;
import codesquad.fineants.spring.api.errors.errorcode.OauthErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.member.response.OauthAccessTokenResponse;
import codesquad.fineants.spring.api.member.response.OauthCreateUrlResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberLoginResponse;

@ActiveProfiles("test")
@SpringBootTest
public class MemberServiceTest {

	@Autowired
	private MemberService memberService;

	@MockBean
	private WebClientWrapper webClientWrapper;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MemberRepository memberRepository;

	@MockBean
	private OauthMemberRedisService redisService;

	@MockBean
	private OauthClientRandomGenerator oauthClientRandomGenerator;

	@AfterEach
	void tearDown() {
		memberRepository.deleteAllInBatch();
	}

	@DisplayName("사용자가 카카오 로그인합니다.")
	@Test
	void login() throws JsonProcessingException {
		// given
		String provider = "kakao";
		String code = "1234";
		String state = "1234";
		String codeVerifier = "1234";
		String nonce = "5b9a1e56120de83c4a083894c8dc8127";
		given(oauthClientRandomGenerator.generateState()).willReturn(state);
		given(oauthClientRandomGenerator.generateCodeVerifier()).willReturn(codeVerifier);
		given(oauthClientRandomGenerator.generateNonce()).willReturn(nonce);

		memberService.createAuthorizationCodeURL(provider);

		Map<String, Object> responseBody = new HashMap<>();
		responseBody.put("access_token", "accessTokenValue");
		responseBody.put("scope", "scopeValue");
		responseBody.put("token_type", "Bearer");
		responseBody.put("id_token",
			"eyJraWQiOiI5ZjI1MmRhZGQ1ZjIzM2Y5M2QyZmE1MjhkMTJmZWEiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI4ODE3MTk1NmM5OTI1N2U5ZWE4YzI0MWI0ZmQ1NDRkZiIsInN1YiI6IjMxMDE1NDMzNjUiLCJhdXRoX3RpbWUiOjE2OTk1MDg3MTEsImlzcyI6Imh0dHBzOi8va2F1dGgua2FrYW8uY29tIiwiZXhwIjoxNjk5NTMwMzExLCJpYXQiOjE2OTk1MDg3MTEsIm5vbmNlIjoiNWI5YTFlNTYxMjBkZTgzYzRhMDgzODk0YzhkYzgxMjciLCJwaWN0dXJlIjoiaHR0cDovL2sua2FrYW9jZG4ubmV0L2RuL2RwazlsMS9idHFtR2hBMmxLTC9PejB3RHVKbjFZVjJESW45MmY2RFZLL2ltZ18xMTB4MTEwLmpwZyIsImVtYWlsIjoicWtkbGZqdG0xMTlAbmF2ZXIuY29tIn0.H32Q0NE4Wcy-XWoIKXRXEXhjx3z9kpB5Wfppkm3V_8yAN8HgJzj7RT4zM7_xeHNAdf5grFHm5CpE-UpZKAWMKA1aikAMmyuIKBvcjWTIE97pHS0HGP-vPY6Kp6l-ZYj6aAlafJq93gcGba3kE-9oha8N48aHyv6G7GapZAQghjRNK2az-2YURcaNDkXXCH3Gnntnx-lZ-NKnTgEeAsD4yvU9rW39wlit7rHE4uhXGBsqaxglTK6WogxOhA89_aZsHTGZdn2BkIjRseafzBrGuk0ltDQxc1TCakrjK4bUCXB1q7yYwm5y-zbqb1iUNWd70-mwU3ko1YDUVbLBi0EuAA");
		OauthAccessTokenResponse mockAccessTokenResponse =
			objectMapper.readValue(objectMapper.writeValueAsString(responseBody), OauthAccessTokenResponse.class);
		given(webClientWrapper.post(anyString(), any(), any(), any())).willReturn(mockAccessTokenResponse);

		willDoNothing().given(redisService).saveRefreshToken(anyString(), any(Jwt.class));

		Map<String, Object> map = new HashMap<>();
		map.put("keys", "[\n"
			+ "        {\n"
			+ "            \"kid\": \"3f96980381e451efad0d2ddd30e3d3\",\n"
			+ "            \"kty\": \"RSA\",\n"
			+ "            \"alg\": \"RS256\",\n"
			+ "            \"use\": \"sig\",\n"
			+ "            \"n\": \"q8zZ0b_MNaLd6Ny8wd4cjFomilLfFIZcmhNSc1ttx_oQdJJZt5CDHB8WWwPGBUDUyY8AmfglS9Y1qA0_fxxs-ZUWdt45jSbUxghKNYgEwSutfM5sROh3srm5TiLW4YfOvKytGW1r9TQEdLe98ork8-rNRYPybRI3SKoqpci1m1QOcvUg4xEYRvbZIWku24DNMSeheytKUz6Ni4kKOVkzfGN11rUj1IrlRR-LNA9V9ZYmeoywy3k066rD5TaZHor5bM5gIzt1B4FmUuFITpXKGQZS5Hn_Ck8Bgc8kLWGAU8TzmOzLeROosqKE0eZJ4ESLMImTb2XSEZuN1wFyL0VtJw\",\n"
			+ "            \"e\": \"AQAB\"\n"
			+ "        }, {\n"
			+ "            \"kid\": \"9f252dadd5f233f93d2fa528d12fea\",\n"
			+ "            \"kty\": \"RSA\",\n"
			+ "            \"alg\": \"RS256\",\n"
			+ "            \"use\": \"sig\",\n"
			+ "            \"n\": \"qGWf6RVzV2pM8YqJ6by5exoixIlTvdXDfYj2v7E6xkoYmesAjp_1IYL7rzhpUYqIkWX0P4wOwAsg-Ud8PcMHggfwUNPOcqgSk1hAIHr63zSlG8xatQb17q9LrWny2HWkUVEU30PxxHsLcuzmfhbRx8kOrNfJEirIuqSyWF_OBHeEgBgYjydd_c8vPo7IiH-pijZn4ZouPsEg7wtdIX3-0ZcXXDbFkaDaqClfqmVCLNBhg3DKYDQOoyWXrpFKUXUFuk2FTCqWaQJ0GniO4p_ppkYIf4zhlwUYfXZEhm8cBo6H2EgukntDbTgnoha8kNunTPekxWTDhE5wGAt6YpT4Yw\",\n"
			+ "            \"e\": \"AQAB\"\n"
			+ "        }\n"
			+ "    ]");
		given(
			webClientWrapper.getPublicKeyList("https://kauth.kakao.com/.well-known/jwks.json",
				new ParameterizedTypeReference<Map<String, Object>>() {
				}))
			.willReturn(map);

		// when
		OauthMemberLoginResponse response = memberService.login(provider, code, state, LocalDateTime.now());

		// then
		assertThat(response)
			.extracting("user")
			.extracting("email")
			.isEqualTo("qkdlfjtm119@naver.com");
	}

	@DisplayName("존재하지 않은 provider를 제공하여 로그인 시도시 예외가 발생한다")
	@Test
	void loginWithNotExistProvider() {
		// given
		String provider = "invalidProvider";
		String code = "1234";
		String state = "1234";
		String codeVerifier = "1234";
		String nonce = "1234";
		given(oauthClientRandomGenerator.generateState()).willReturn(state);
		given(oauthClientRandomGenerator.generateCodeVerifier()).willReturn(codeVerifier);
		given(oauthClientRandomGenerator.generateNonce()).willReturn(nonce);
		memberService.createAuthorizationCodeURL("kakao");

		// when
		Throwable throwable = catchThrowable(() -> memberService.login(provider, code, state, LocalDateTime.now()));

		// then
		assertThat(throwable)
			.isInstanceOf(NotFoundResourceException.class)
			.hasMessage("provider를 찾을 수 없습니다");
	}

	@DisplayName("유효하지 않은 인가코드를 전달하여 예외가 발생한다")
	@Test
	void loginWithInvalidCode() {
		// given
		String provider = "kakao";
		String code = "1234";
		given(oauthClientRandomGenerator.generateState()).willReturn("1234");
		given(oauthClientRandomGenerator.generateCodeVerifier()).willReturn("1234");
		given(oauthClientRandomGenerator.generateNonce()).willReturn("1234");
		OauthCreateUrlResponse oauthCreateUrlResponse = memberService.createAuthorizationCodeURL(provider);
		String state = oauthCreateUrlResponse.getState();

		given(webClientWrapper.post(anyString(), any(), any(), any()))
			.willThrow(new BadRequestException(OauthErrorCode.FAIL_REQUEST,
				"{\"error\":\"invalid_grant\",\"error_description\":\"authorization code not found for code=1234\",\"error_code\":\"KOE320\"}"));

		// when
		Throwable throwable = catchThrowable(() -> memberService.login(provider, code, state, LocalDateTime.now()));

		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage(
				"{\"error\":\"invalid_grant\",\"error_description\":\"authorization code not found for code=1234\",\"error_code\":\"KOE320\"}");
	}

	@DisplayName("잘못된 state를 전달하여 로그인을 할 수 없다")
	@Test
	void loginWithInvalidState() {
		// given
		String provider = "kakao";
		String code = "1234";
		String state = "1234";

		// when
		Throwable throwable = catchThrowable(() -> memberService.login(provider, code, state, LocalDateTime.now()));

		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage("잘못된 State입니다");
	}
}
