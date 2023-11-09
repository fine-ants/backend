package codesquad.fineants.spring.api.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
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

	@BeforeEach
	void tearDown() {
		memberRepository.deleteAllInBatch();
	}

	@DisplayName("사용자가 카카오 로그인합니다.")
	@Test
	void login() throws JsonProcessingException {
		// given
		String provider = "kakao";
		String code = "1234";
		OauthCreateUrlResponse oauthCreateUrlResponse = memberService.createAuthorizationCodeURL(provider);
		String state = oauthCreateUrlResponse.getState();

		Map<String, Object> responseBody = new HashMap<>();
		responseBody.put("access_token", "accessTokenValue");
		responseBody.put("scope", "scopeValue");
		responseBody.put("token_type", "Bearer");
		OauthAccessTokenResponse mockAccessTokenResponse =
			objectMapper.readValue(objectMapper.writeValueAsString(responseBody), OauthAccessTokenResponse.class);
		given(webClientWrapper.post(anyString(), any(), any(), any())).willReturn(mockAccessTokenResponse);

		Map<String, Object> userProfileMap = new HashMap<>();
		userProfileMap.put("kakao_account",
			Map.of("email", "kim1234@gmail.com",
				"profile", Map.of("profile_image_url", "profileImage")));
		given(webClientWrapper.get(anyString(), any(), eq(new ParameterizedTypeReference<Map<String, Object>>() {
		}))).willReturn(userProfileMap);
		willDoNothing().given(redisService).saveRefreshToken(anyString(), any(Jwt.class));

		// when
		OauthMemberLoginResponse response = memberService.login(provider, code, state, LocalDateTime.now());

		// then
		assertThat(response)
			.extracting("user")
			.extracting("email")
			.isEqualTo("kim1234@gmail.com");
	}

	@DisplayName("존재하지 않은 provider를 제공하여 로그인 시도시 예외가 발생한다")
	@Test
	void loginWithNotExistProvider() {
		// given
		String provider = "invalidProvider";
		String code = "1234";
		OauthCreateUrlResponse oauthCreateUrlResponse = memberService.createAuthorizationCodeURL("kakao");
		String state = oauthCreateUrlResponse.getState();

		// when
		Throwable throwable = catchThrowable(() -> memberService.login(provider, code, state, LocalDateTime.now()));

		// then
		assertThat(throwable)
			.isInstanceOf(NotFoundResourceException.class)
			.hasMessage("provider를 찾을 수 없습니다.");
	}

	@DisplayName("유효하지 않은 인가코드를 전달하여 예외가 발생한다")
	@Test
	void loginWithInvalidCode() {
		// given
		String provider = "kakao";
		String code = "1234";

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
			.hasMessage("잘못된 State입니다.");
	}
}
