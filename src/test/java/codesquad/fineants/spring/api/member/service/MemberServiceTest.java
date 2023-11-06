package codesquad.fineants.spring.api.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.client.OauthClient;
import codesquad.fineants.domain.oauth.repository.OauthClientRepository;
import codesquad.fineants.spring.api.member.response.OauthAccessTokenResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberLoginResponse;
import codesquad.fineants.spring.api.member.response.OauthUserProfileResponse;

@ActiveProfiles("test")
@SpringBootTest
public class MemberServiceTest {

	@MockBean
	private OauthClientRepository oauthClientRepository;

	@Mock
	private OauthClient oauthClient;

	@Autowired
	private MemberService memberService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MemberRepository memberRepository;

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
		String redirectUrl = null;

		Map<String, Object> responseBody = new HashMap<>();
		responseBody.put("access_token", "accessTokenValue");
		responseBody.put("scope", "scopeValue");
		responseBody.put("token_type", "Bearer");
		OauthAccessTokenResponse mockAccessTokenResponse =
			objectMapper.readValue(objectMapper.writeValueAsString(responseBody), OauthAccessTokenResponse.class);

		Map<String, Object> userProfileResponseBody = new HashMap<>();
		userProfileResponseBody.put("email", "kim1234@gmail.com");
		userProfileResponseBody.put("profileImage", "profileImageValue");
		OauthUserProfileResponse mockUserProfileResponse = objectMapper.readValue(
			objectMapper.writeValueAsString(userProfileResponseBody), OauthUserProfileResponse.class);

		given(oauthClientRepository.findOneBy(ArgumentMatchers.anyString())).willReturn(oauthClient);
		given(oauthClient.exchangeAccessTokenByAuthorizationCode(
			ArgumentMatchers.anyString(),
			ArgumentMatchers.isNull()))
			.willReturn(mockAccessTokenResponse);
		given(oauthClient.getUserProfileByAccessToken(
			ArgumentMatchers.any(OauthAccessTokenResponse.class)))
			.willReturn(mockUserProfileResponse);
		// when
		OauthMemberLoginResponse response = memberService.login(provider, code, LocalDateTime.now(), redirectUrl);

		// then
		assertAll(
			() -> assertThat(response)
				.extracting("user")
				.extracting("email")
				.isEqualTo("kim1234@gmail.com")
		);

	}

}
