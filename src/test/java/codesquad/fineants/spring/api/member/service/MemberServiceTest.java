package codesquad.fineants.spring.api.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.client.AuthorizationCodeRandomGenerator;
import codesquad.fineants.domain.oauth.client.OauthClient;
import codesquad.fineants.domain.oauth.repository.OauthClientRepository;
import codesquad.fineants.spring.api.errors.errorcode.OauthErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.errors.exception.FineAntsException;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.member.request.AuthorizationRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberLoginRequest;
import codesquad.fineants.spring.api.member.response.OauthMemberLoginResponse;
import codesquad.fineants.spring.api.member.response.OauthUserProfile;
import codesquad.fineants.spring.util.ObjectMapperUtil;

@ActiveProfiles("test")
@SpringBootTest
public class MemberServiceTest {

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberRepository memberRepository;

	@MockBean
	private AuthorizationCodeRandomGenerator authorizationCodeRandomGenerator;

	@MockBean
	private OauthClientRepository oauthClientRepository;

	@MockBean
	private OauthClient oauthClient;

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
		given(authorizationCodeRandomGenerator.generateAuthorizationRequest()).willReturn(
			createAuthorizationRequest(state));
		given(oauthClientRepository.findOneBy(anyString())).willReturn(oauthClient);
		given(oauthClient.fetchProfile(any(OauthMemberLoginRequest.class), any(AuthorizationRequest.class)))
			.willReturn(new OauthUserProfile("fineants.co@gmail.com", "profileImage", "kakao"));
		memberService.createAuthorizationCodeURL(provider);

		OauthMemberLoginRequest loginRequest = createOauthMemberLoginServiceRequest(provider, code, redirectUrl,
			state);
		// when
		OauthMemberLoginResponse response = memberService.login(loginRequest);

		// then
		assertThat(response)
			.extracting("user")
			.extracting("email")
			.isEqualTo("fineants.co@gmail.com");
	}

	@DisplayName("존재하지 않은 provider를 제공하여 로그인 시도시 예외가 발생한다")
	@Test
	void loginWithNotExistProvider() {
		// given
		String provider = "invalidProvider";
		String code = "1234";
		String redirectUrl = "http://localhost:5173/signin?provider=kakao";
		String state = "1234";
		given(oauthClientRepository.findOneBy(anyString())).willThrow(
			new NotFoundResourceException(OauthErrorCode.NOT_FOUND_PROVIDER));

		OauthMemberLoginRequest loginRequest = createOauthMemberLoginServiceRequest(provider,
			code, redirectUrl, state);

		// when
		Throwable throwable = catchThrowable(() -> memberService.login(loginRequest));

		// then
		assertAll(
			() -> assertThat(throwable)
				.isInstanceOf(FineAntsException.class)
				.extracting("errorCode")
				.isInstanceOf(OauthErrorCode.class)
				.extracting("httpStatus", "message")
				.containsExactlyInAnyOrder(HttpStatus.UNAUTHORIZED, "로그인 정보가 일치하지 않습니다"),

			() -> assertThat(throwable)
				.isInstanceOf(FineAntsException.class)
				.extracting("message")
				.isEqualTo(
					"FineAnts 예외, NotFoundResourceException(errorCode=Oauth 에러 코드, OauthErrorCode(name=NOT_FOUND_PROVIDER, httpStatus=404 NOT_FOUND, message=provider를 찾을 수 없습니다), message=provider를 찾을 수 없습니다)")
		);
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
		given(oauthClientRepository.findOneBy(anyString())).willReturn(oauthClient);

		Map<String, String> errorBody = new HashMap<>();
		errorBody.put("error", "invalid_grant");
		errorBody.put("error_description", "authorization code not found for code=1234");
		errorBody.put("error_code", "KOE320");
		given(oauthClient.fetchProfile(any(OauthMemberLoginRequest.class), any(AuthorizationRequest.class)))
			.willThrow(new BadRequestException(OauthErrorCode.FAIL_REQUEST, ObjectMapperUtil.serialize(errorBody)));
		memberService.createAuthorizationCodeURL(provider);

		OauthMemberLoginRequest loginRequest = createOauthMemberLoginServiceRequest(provider, code, redirectUrl, state);

		// when
		Throwable throwable = catchThrowable(() -> memberService.login(loginRequest));

		// then
		assertAll(
			() -> assertThat(throwable)
				.isInstanceOf(FineAntsException.class)
				.extracting("errorCode")
				.isInstanceOf(OauthErrorCode.class)
				.extracting("httpStatus", "message")
				.containsExactlyInAnyOrder(HttpStatus.UNAUTHORIZED, "로그인 정보가 일치하지 않습니다"),

			() -> assertThat(throwable)
				.isInstanceOf(FineAntsException.class)
				.extracting("message")
				.isEqualTo(
					"FineAnts 예외, BadRequestException(errorCode=Oauth 에러 코드, OauthErrorCode(name=FAIL_REQUEST, httpStatus=400 BAD_REQUEST, message=요청에 실패하였습니다), message={\"error_description\":\"authorization code not found for code=1234\",\"error_code\":\"KOE320\",\"error\":\"invalid_grant\"})")
		);
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
		assertAll(
			() -> assertThat(throwable)
				.isInstanceOf(FineAntsException.class)
				.extracting("errorCode")
				.isInstanceOf(OauthErrorCode.class)
				.extracting("httpStatus", "message")
				.containsExactlyInAnyOrder(HttpStatus.UNAUTHORIZED, "로그인 정보가 일치하지 않습니다"),

			() -> assertThat(throwable)
				.isInstanceOf(FineAntsException.class)
				.extracting("message")
				.isEqualTo(
					"FineAnts 예외, BadRequestException(errorCode=Oauth 에러 코드, OauthErrorCode(name=WRONG_STATE, httpStatus=400 BAD_REQUEST, message=잘못된 State입니다), message=잘못된 State입니다)")
		);
	}

	@DisplayName("사용자가 네이버 로그인합니다.")
	@Test
	void loginWithNaver() {
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
		given(oauthClientRepository.findOneBy(anyString())).willReturn(oauthClient);
		given(oauthClient.fetchProfile(any(OauthMemberLoginRequest.class), any(AuthorizationRequest.class)))
			.willReturn(createOauthUserProfile("fineants.co@naver.com", "naver"));
		memberService.createAuthorizationCodeURL(provider);

		OauthMemberLoginRequest loginRequest = createOauthMemberLoginServiceRequest(provider, code, redirectUrl, state);
		// when
		OauthMemberLoginResponse response = memberService.login(loginRequest);

		// then
		assertThat(response)
			.extracting("user")
			.extracting("email")
			.isEqualTo("fineants.co@naver.com");
	}

	private AuthorizationRequest createAuthorizationRequest(String state) {
		String codeVerifier = "1234";
		String codeChallenge = "1234";
		String nonce = "f46e2977378ed6cdf2f03b5962101f7d";
		return AuthorizationRequest.of(state, codeVerifier, codeChallenge, nonce);
	}

	private OauthUserProfile createOauthUserProfile(String email, String provider) {
		return new OauthUserProfile("fineants.co@naver.com", "profileImage", "naver");
	}

	private OauthMemberLoginRequest createOauthMemberLoginServiceRequest(String provider, String code,
		String redirectUrl, String state) {
		return OauthMemberLoginRequest.of(provider,
			code, redirectUrl, state, LocalDate.of(2023, 11, 8).atStartOfDay());
	}
}
