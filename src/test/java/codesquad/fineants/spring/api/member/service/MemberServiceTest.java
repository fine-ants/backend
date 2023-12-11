package codesquad.fineants.spring.api.member.service;

import static codesquad.fineants.spring.util.ObjectMapperUtil.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.client.AuthorizationCodeRandomGenerator;
import codesquad.fineants.spring.api.errors.errorcode.OauthErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.member.request.AuthorizationRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberLoginRequest;
import codesquad.fineants.spring.api.member.response.OauthMemberLoginResponse;
import codesquad.fineants.spring.api.member.response.OauthToken;

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
	private AuthorizationCodeRandomGenerator authorizationCodeRandomGenerator;

	@AfterEach
	void tearDown() {
		memberRepository.deleteAllInBatch();
	}

	@DisplayName("사용자가 카카오 로그인합니다.")
	@Test
	void loginUsingKakao() {
		// given
		String provider = "kakao";
		String code = "1234";
		String redirectUrl = "http://localhost:5173/signin?provider=kakao";
		String state = "1234";
		AuthorizationRequest authorizationRequest = createAuthorizationRequest(state);
		given(authorizationCodeRandomGenerator.generateAuthorizationRequest()).willReturn(authorizationRequest);

		memberService.createAuthorizationCodeURL(provider);

		Map<String, Object> responseBody = new HashMap<>();
		responseBody.put("access_token", "accessTokenValue");
		responseBody.put("scope", "openid");
		responseBody.put("token_type", "Bearer");
		responseBody.put("id_token",
			"eyJraWQiOiI5ZjI1MmRhZGQ1ZjIzM2Y5M2QyZmE1MjhkMTJmZWEiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiJkZmIxZTI1YTJiOTdkMDNiMGIyMjVkNDg3NGEzNDgyMyIsInN1YiI6IjMxNjA5OTI1NjMiLCJhdXRoX3RpbWUiOjE2OTk4NjY2MjIsImlzcyI6Imh0dHBzOi8va2F1dGgua2FrYW8uY29tIiwiZXhwIjoxNjk5ODg4MjIyLCJpYXQiOjE2OTk4NjY2MjIsIm5vbmNlIjoiZjQ2ZTI5NzczNzhlZDZjZGYyZjAzYjU5NjIxMDFmN2QiLCJwaWN0dXJlIjoiaHR0cDovL2sua2FrYW9jZG4ubmV0L2RuL2RwazlsMS9idHFtR2hBMmxLTC9PejB3RHVKbjFZVjJESW45MmY2RFZLL2ltZ18xMTB4MTEwLmpwZyIsImVtYWlsIjoiZmluZWFudHMuY29AZ21haWwuY29tIn0.KYRaqSup_joMxGSYoWSa5bEvQRbCR3q_kUydXaDs_otD1fmJFWW3h8UsrKaHyaDT6mMIobeuTAXlhjiY7rxqhr3K3cqj3urZThIJ-h4-5VrANC6aulzyhJB93kcIJUxa2UzI0fe_TyC4pbbGvRnFDci0Iv5CuVZoL0-oLgu5LQFpM1xmNmF_SABKuIU0rV60E_Qs7PsbnodZ_emv4PLEtk1yUD1r5oAr9swtSjLG_uh1d2GXUvcIStIJN871Kp-jolqM1Ce4bp0zULd00x0nGKbPBHmS_J11NYJbjujeg771307w65KCYIPMMqM_1BbEFQghH3jDRdzwP_5_RRKhAg");
		OauthToken mockAccessTokenResponse = deserialize(serialize(responseBody),
			OauthToken.class);
		given(webClientWrapper.post(anyString(), any(), any(), any())).willReturn(mockAccessTokenResponse);

		MockedStatic<JWT> jwtMockedStatic = mockStatic(JWT.class);
		Verification mockVerification = mock(Verification.class);
		JWTVerifier mockJWTVerifier = mock(JWTVerifier.class);
		given(JWT.require(any(Algorithm.class))).willReturn(mockVerification);
		given(mockVerification.build()).willReturn(mockJWTVerifier);
		DecodedJWT mockDecodedJWT = mock(DecodedJWT.class);
		String kid = "9f252dadd5f233f93d2fa528d12fea";
		given(JWT.decode((String)responseBody.get("id_token"))).willReturn(mockDecodedJWT);
		given(mockDecodedJWT.getKeyId()).willReturn(kid);

		OauthMemberLoginRequest loginRequest = createOauthMemberLoginServiceRequest(provider, code, redirectUrl,
			state);
		// when
		OauthMemberLoginResponse response = memberService.login(loginRequest);

		// then
		assertThat(response)
			.extracting("user")
			.extracting("email")
			.isEqualTo("fineants.co@gmail.com");

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

		OauthMemberLoginRequest loginRequest = createOauthMemberLoginServiceRequest(provider,
			code, redirectUrl, state);

		// when
		Throwable throwable = catchThrowable(() -> memberService.login(loginRequest));

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

		OauthMemberLoginRequest loginRequest = createOauthMemberLoginServiceRequest(provider,
			code, redirectUrl, state);

		// when
		Throwable throwable = catchThrowable(() -> memberService.login(loginRequest));

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

		OauthMemberLoginRequest loginRequest = createOauthMemberLoginServiceRequest(provider,
			code, redirectUrl, state);
		// when
		Throwable throwable = catchThrowable(() -> memberService.login(loginRequest));

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
		OauthToken mockAccessTokenResponse =
			objectMapper.readValue(objectMapper.writeValueAsString(responseBody), OauthToken.class);
		given(webClientWrapper.post(anyString(), any(), any(), any())).willReturn(mockAccessTokenResponse);

		Map<String, Object> userProfileResponseBody = new HashMap<>();
		userProfileResponseBody.put("response",
			Map.of("email", "qkdlfjtm119@naver.com", "profile_image", "profile_image"));
		given(webClientWrapper.get(anyString(), any(MultiValueMap.class),
			any(ParameterizedTypeReference.class))).willReturn(userProfileResponseBody);

		OauthMemberLoginRequest loginRequest = createOauthMemberLoginServiceRequest(provider,
			code, redirectUrl, state);
		// when
		OauthMemberLoginResponse response = memberService.login(loginRequest);

		// then
		assertThat(response)
			.extracting("user")
			.extracting("email")
			.isEqualTo("qkdlfjtm119@naver.com");
	}

	private AuthorizationRequest createAuthorizationRequest(String state) {
		String codeVerifier = "1234";
		String codeChallenge = "1234";
		String nonce = "f46e2977378ed6cdf2f03b5962101f7d";
		return AuthorizationRequest.of(state, codeVerifier, codeChallenge, nonce);
	}

	private OauthMemberLoginRequest createOauthMemberLoginServiceRequest(String provider, String code,
		String redirectUrl, String state) {
		return OauthMemberLoginRequest.of(provider,
			code, redirectUrl, state, LocalDate.of(2023, 11, 8).atStartOfDay());
	}
}
