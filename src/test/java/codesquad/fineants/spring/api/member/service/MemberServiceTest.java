package codesquad.fineants.spring.api.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.MultiValueMap;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.jwt.Jwt;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.client.AuthorizationCodeRandomGenerator;
import codesquad.fineants.spring.api.errors.errorcode.OauthErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.member.request.AuthorizationRequest;
import codesquad.fineants.spring.api.member.response.OauthAccessTokenResponse;
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
	private AuthorizationCodeRandomGenerator authorizationCodeRandomGenerator;

	@Mock
	private JWT mockJwt;

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
		String redirectUrl = "http://localhost:5173/signin?provider=kakao";
		String state = "1234";
		String codeVerifier = "1234";
		String codeChallenge = "1234";
		String nonce = "5b9a1e56120de83c4a083894c8dc8127";
		given(authorizationCodeRandomGenerator.generateAuthorizationRequest()).willReturn(
			AuthorizationRequest.of(state, codeVerifier, codeChallenge, nonce));

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

		MockedStatic<JWT> jwtMockedStatic = mockStatic(JWT.class);
		Verification mockVerification = mock(Verification.class);
		JWTVerifier mockJWTVerifier = mock(JWTVerifier.class);
		given(JWT.require(any(Algorithm.class))).willReturn(mockVerification);
		given(mockVerification.build()).willReturn(mockJWTVerifier);
		DecodedJWT mockDecodedJWT = mock(DecodedJWT.class);
		String kid = "9f252dadd5f233f93d2fa528d12fea";
		given(JWT.decode((String)responseBody.get("id_token"))).willReturn(mockDecodedJWT);
		given(mockDecodedJWT.getKeyId()).willReturn(kid);

		OauthMemberLoginResponse response = memberService.login(provider, code, redirectUrl, state,
			LocalDate.of(2023, 11, 8).atStartOfDay());

		assertThat(response)
			.extracting("user")
			.extracting("email")
			.isEqualTo("qkdlfjtm119@naver.com");

		jwtMockedStatic.close();
	}

	@DisplayName("존재하지 않은 provider를 제공하여 로그인 시도시 예외가 발생한다")
	@Test
	void loginWithNotExistProvider() {
		// given
		String provider = "invalidProvider";
		String code = "1234";
		String redirectUrl = "http://localhost:5173/signin?provider=kakao";
		String state = "1234";
		String codeVerifier = "1234";
		String codeChallenge = "1234";
		String nonce = "1234";
		given(authorizationCodeRandomGenerator.generateAuthorizationRequest()).willReturn(
			AuthorizationRequest.of(state, codeVerifier, codeChallenge, nonce));
		memberService.createAuthorizationCodeURL("kakao");

		// when
		Throwable throwable = catchThrowable(
			() -> memberService.login(provider, code, redirectUrl, state, LocalDateTime.now()));

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
		String redirectUrl = "http://localhost:5173/signin?provider=kakao";
		String state = "1234";
		String codeVerifier = "1234";
		String codeChallenge = "4321";
		String nonce = "1234";
		given(authorizationCodeRandomGenerator.generateAuthorizationRequest()).willReturn(
			AuthorizationRequest.of(state, codeVerifier, codeChallenge, nonce));
		memberService.createAuthorizationCodeURL(provider);
		given(webClientWrapper.post(anyString(), any(), any(), any()))
			.willThrow(new BadRequestException(OauthErrorCode.FAIL_REQUEST,
				"{\"error\":\"invalid_grant\",\"error_description\":\"authorization code not found for code=1234\",\"error_code\":\"KOE320\"}"));

		// when
		Throwable throwable = catchThrowable(
			() -> memberService.login(provider, code, redirectUrl, state, LocalDateTime.now()));

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
		String redirectUrl = "http://localhost:5173/signin?provider=kakao";
		String state = "1234";

		// when
		Throwable throwable = catchThrowable(
			() -> memberService.login(provider, code, redirectUrl, state, LocalDateTime.now()));

		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage("잘못된 State입니다");
	}

	@DisplayName("사용자가 네이버 로그인합니다.")
	@Test
	void loginWithNaver() throws JsonProcessingException {
		// given
		String provider = "naver";
		String code = "1234";
		String redirectUrl = "http://localhost:5173/signin?provider=naver";
		String state = "1234";
		String codeVerifier = "1234";
		String codeChallenge = "1234";
		String nonce = "1234";
		given(authorizationCodeRandomGenerator.generateAuthorizationRequest()).willReturn(
			AuthorizationRequest.of(state, codeVerifier, codeChallenge, nonce));
		memberService.createAuthorizationCodeURL(provider);

		Map<String, Object> responseBody = new HashMap<>();
		responseBody.put("access_token", "accessTokenValue");
		responseBody.put("scope", "scopeValue");
		responseBody.put("token_type", "Bearer");
		OauthAccessTokenResponse mockAccessTokenResponse =
			objectMapper.readValue(objectMapper.writeValueAsString(responseBody), OauthAccessTokenResponse.class);
		given(webClientWrapper.post(anyString(), any(), any(), any())).willReturn(mockAccessTokenResponse);
		willDoNothing().given(redisService).saveRefreshToken(anyString(), any(Jwt.class));

		Map<String, Object> userProfileResponseBody = new HashMap<>();
		userProfileResponseBody.put("response",
			Map.of("email", "qkdlfjtm119@naver.com", "profile_image", "profile_image"));
		given(webClientWrapper.get(anyString(), any(MultiValueMap.class),
			any(ParameterizedTypeReference.class))).willReturn(userProfileResponseBody);
		// when
		OauthMemberLoginResponse response = memberService.login(provider, code, redirectUrl, state,
			LocalDate.of(2023, 11, 8).atStartOfDay());

		// then
		assertThat(response)
			.extracting("user")
			.extracting("email")
			.isEqualTo("qkdlfjtm119@naver.com");
	}

}
