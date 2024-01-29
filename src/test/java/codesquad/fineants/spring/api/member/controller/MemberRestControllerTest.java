package codesquad.fineants.spring.api.member.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import codesquad.fineants.domain.jwt.Jwt;
import codesquad.fineants.domain.jwt.JwtProperties;
import codesquad.fineants.domain.jwt.JwtProvider;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.oauth.client.OauthClient;
import codesquad.fineants.domain.oauth.repository.OauthClientRepository;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalArgumentResolver;
import codesquad.fineants.spring.api.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.errors.handler.GlobalExceptionHandler;
import codesquad.fineants.spring.api.member.request.AuthorizationRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberLoginRequest;
import codesquad.fineants.spring.api.member.response.OauthMemberLoginResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberResponse;
import codesquad.fineants.spring.api.member.response.OauthSaveUrlResponse;
import codesquad.fineants.spring.api.member.response.ProfileChangeResponse;
import codesquad.fineants.spring.api.member.service.MemberService;
import codesquad.fineants.spring.api.member.service.WebClientWrapper;
import codesquad.fineants.spring.api.member.service.request.ProfileChangeServiceRequest;
import codesquad.fineants.spring.api.member.service.request.SignUpServiceRequest;
import codesquad.fineants.spring.api.member.service.response.SignUpServiceResponse;
import codesquad.fineants.spring.config.JpaAuditingConfiguration;
import codesquad.fineants.spring.config.OauthConfig;
import codesquad.fineants.spring.util.ObjectMapperUtil;

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
	private OauthClientRepository oauthClientRepository;

	@Autowired
	private JwtProvider jwtProvider;

	@MockBean
	private MemberService memberService;

	@MockBean
	private AuthPrincipalArgumentResolver authPrincipalArgumentResolver;

	@MockBean
	private WebClientWrapper webClient;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(memberRestController)
			.setControllerAdvice(globalExceptionHandler)
			.setCustomArgumentResolvers(authPrincipalArgumentResolver)
			.alwaysDo(print())
			.build();

		given(authPrincipalArgumentResolver.supportsParameter(ArgumentMatchers.any(MethodParameter.class)))
			.willReturn(true);
		given(authPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any()))
			.willReturn(AuthMember.from(createMember()));
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

		OauthSaveUrlResponse mockResponse = new OauthSaveUrlResponse(expectedAuthURL,
			AuthorizationRequest.of("1234", "codeVerifier", "codeChallenge", "1234"));

		given(memberService.saveAuthorizationCodeURL(
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
		Member member = createMember();
		String code = "1234";
		String redirectUrl = "http://localhost:5173/signin?provider=kakao";
		String state = "1234";
		String url = "/api/auth/kakao/login";

		Jwt jwt = jwtProvider.createJwtBasedOnMember(member, LocalDateTime.now());
		OauthMemberLoginResponse mockResponse = OauthMemberLoginResponse.of(jwt, OauthMemberResponse.from(member));
		given(memberService.login(ArgumentMatchers.any(OauthMemberLoginRequest.class))).willReturn(mockResponse);
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

	@DisplayName("사용자는 회원의 프로필 및 닉네임을 변경한다")
	@Test
	void changeProfile() throws Exception {
		// given
		Member member = createMember("일개미12345", "changeProfileUrl");
		given(memberService.changeProfile(ArgumentMatchers.any(ProfileChangeServiceRequest.class)))
			.willReturn(ProfileChangeResponse.from(member));

		Map<String, Object> profileInformationMap = Map.of("nickname", "일개미12345");
		String json = ObjectMapperUtil.serialize(profileInformationMap);
		MockMultipartFile profileInformation = new MockMultipartFile(
			"profileInformation",
			"profileInformation",
			MediaType.APPLICATION_JSON_VALUE,
			json.getBytes(StandardCharsets.UTF_8));

		// when & then
		mockMvc.perform(multipart(PUT, "/api/profile")
				.file((MockMultipartFile)createMockMultipartFile())
				.file(profileInformation))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("프로필이 수정되었습니다")))
			.andExpect(jsonPath("data.user.id").value(equalTo(1)))
			.andExpect(jsonPath("data.user.nickname").value(equalTo("일개미12345")))
			.andExpect(jsonPath("data.user.email").value(equalTo("dragonbead95@naver.com")))
			.andExpect(jsonPath("data.user.profileUrl").value(equalTo("changeProfileUrl")));
	}

	@DisplayName("사용자는 아무 정보도 전달하지 않고 회원 프로필 변경 요청시 에러를 응답한다")
	@Test
	void changeProfile_whenNotExistInput_thenResponse400Error() throws Exception {
		// given
		given(memberService.changeProfile(ArgumentMatchers.any(ProfileChangeServiceRequest.class)))
			.willThrow(new BadRequestException(MemberErrorCode.NO_PROFILE_CHANGES));

		// when & then
		mockMvc.perform(multipart(PUT, "/api/profile"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("변경할 회원 정보가 없습니다")));
	}

	@DisplayName("사용자는 일반 회원가입을 한다")
	@Test
	void signup() throws Exception {
		// given
		given(memberService.signup(any(SignUpServiceRequest.class)))
			.willReturn(SignUpServiceResponse.from(createMember()));

		Map<String, Object> profileInformationMap = Map.of(
			"nickname", "일개미1234",
			"email", "dragonbead95@naver.com",
			"password", "nemo1234@",
			"passwordConfirm", "nemo1234@");
		String json = ObjectMapperUtil.serialize(profileInformationMap);
		MockMultipartFile signupData = new MockMultipartFile(
			"signupData",
			"signupData",
			MediaType.APPLICATION_JSON_VALUE,
			json.getBytes(StandardCharsets.UTF_8));

		// when & then
		mockMvc.perform(multipart(POST, "/api/auth/signup")
				.file((MockMultipartFile)createMockMultipartFile())
				.file(signupData))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo("회원가입이 완료되었습니다")));
	}

	@DisplayName("사용자는 프로필을 건너뛰고 회원가입 할 수 있다")
	@Test
	void signup_whenSkipProfileImageFile_then200OK() throws Exception {
		// given
		given(memberService.signup(any(SignUpServiceRequest.class)))
			.willReturn(SignUpServiceResponse.from(createMember()));

		Map<String, Object> profileInformationMap = Map.of(
			"nickname", "일개미1234",
			"email", "dragonbead95@naver.com",
			"password", "nemo1234@",
			"passwordConfirm", "nemo1234@");
		String json = ObjectMapperUtil.serialize(profileInformationMap);
		MockMultipartFile signupData = new MockMultipartFile(
			"signupData",
			"signupData",
			MediaType.APPLICATION_JSON_VALUE,
			json.getBytes(StandardCharsets.UTF_8));

		// when & then
		mockMvc.perform(multipart(POST, "/api/auth/signup")
				.file(signupData))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo("회원가입이 완료되었습니다")));
	}

	@DisplayName("사용자는 유효하지 않은 회원가입 데이터로 요청시 400 에러를 응답받는다")
	@MethodSource(value = "invalidSignupData")
	@ParameterizedTest
	void signup_whenInvalidSignupData_thenResponse400Error(String nickname, String email, String password,
		String passwordConfirm) throws Exception {
		// given
		Map<String, Object> profileInformationMap = Map.of(
			"nickname", nickname,
			"email", email,
			"password", password,
			"passwordConfirm", passwordConfirm);
		String json = ObjectMapperUtil.serialize(profileInformationMap);
		MockMultipartFile signupData = new MockMultipartFile(
			"signupData",
			"signupData",
			MediaType.APPLICATION_JSON_VALUE,
			json.getBytes(StandardCharsets.UTF_8));

		// when & then
		mockMvc.perform(multipart(POST, "/api/auth/signup")
				.file((MockMultipartFile)createMockMultipartFile())
				.file(signupData))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")));
	}

	@DisplayName("사용자는 중복된 닉네임으로는 회원가입 할 수 없다")
	@Test
	void signup_whenDuplicatedNickname_thenResponse400Error() throws Exception {
		// given
		given(memberService.signup(any(SignUpServiceRequest.class)))
			.willThrow(new BadRequestException(MemberErrorCode.REDUNDANT_NICKNAME));

		Map<String, Object> profileInformationMap = Map.of(
			"nickname", "일개미1234",
			"email", "dragonbead95@naver.com",
			"password", "nemo1234@",
			"passwordConfirm", "nemo1234@");
		String json = ObjectMapperUtil.serialize(profileInformationMap);
		MockMultipartFile signupData = new MockMultipartFile(
			"signupData",
			"signupData",
			MediaType.APPLICATION_JSON_VALUE,
			json.getBytes(StandardCharsets.UTF_8));

		// when & then
		mockMvc.perform(multipart(POST, "/api/auth/signup")
				.file((MockMultipartFile)createMockMultipartFile())
				.file(signupData))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("닉네임이 중복되었습니다")));
	}

	@DisplayName("사용자는 중복된 이메일로는 회원가입 할 수 없다")
	@Test
	void signup_whenDuplicatedEmail_thenResponse400Error() throws Exception {
		// given
		given(memberService.signup(any(SignUpServiceRequest.class)))
			.willThrow(new BadRequestException(MemberErrorCode.REDUNDANT_EMAIL));

		Map<String, Object> profileInformationMap = Map.of(
			"nickname", "일개미1234",
			"email", "dragonbead95@naver.com",
			"password", "nemo1234@",
			"passwordConfirm", "nemo1234@");
		String json = ObjectMapperUtil.serialize(profileInformationMap);
		MockMultipartFile signupData = new MockMultipartFile(
			"signupData",
			"signupData",
			MediaType.APPLICATION_JSON_VALUE,
			json.getBytes(StandardCharsets.UTF_8));

		// when & then
		mockMvc.perform(multipart(POST, "/api/auth/signup")
				.file((MockMultipartFile)createMockMultipartFile())
				.file(signupData))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("이메일이 중복되었습니다")));
	}

	@DisplayName("사용자는 비밀번호가 불일치하여 회원가입 할 수 없다")
	@Test
	void signup_whenNotMatchPasswordAndPasswordConfirm_thenResponse400Error() throws Exception {
		// given
		given(memberService.signup(any(SignUpServiceRequest.class)))
			.willThrow(new BadRequestException(MemberErrorCode.PASSWORD_CHECK_FAIL));

		Map<String, Object> profileInformationMap = Map.of(
			"nickname", "일개미1234",
			"email", "dragonbead95@naver.com",
			"password", "nemo1234@",
			"passwordConfirm", "nemo1234@");
		String json = ObjectMapperUtil.serialize(profileInformationMap);
		MockMultipartFile signupData = new MockMultipartFile(
			"signupData",
			"signupData",
			MediaType.APPLICATION_JSON_VALUE,
			json.getBytes(StandardCharsets.UTF_8));

		// when & then
		mockMvc.perform(multipart(POST, "/api/auth/signup")
				.file((MockMultipartFile)createMockMultipartFile())
				.file(signupData))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("비밀번호가 일치하지 않습니다")));
	}

	@DisplayName("사용자는 signupData 필드 없이 회원가입 할 수 없다")
	@Test
	void signup_whenNotExistSignupDataField_thenResponse400Error() throws Exception {
		// given

		// when & then
		mockMvc.perform(multipart(POST, "/api/auth/signup")
				.file((MockMultipartFile)createMockMultipartFile()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("Required request part 'signupData' is not present")));
	}

	@DisplayName("사용자는 회원가입 과정중 닉네임이 중복되었는지 검사할 수 있다")
	@Test
	void nicknameDuplicationCheck() throws Exception {
		// given
		String nickname = "일개미1234";
		// when & then
		mockMvc.perform(get("/api/auth/signup/duplicationcheck/nickname/{nickname}", nickname))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("닉네임이 사용가능합니다")));
	}

	@DisplayName("사용자는 회원가입 과정중 닉네임이 중복되어 400 응답을 받는다")
	@Test
	void nicknameDuplicationCheck_whenDuplicatedNickname_thenResponse400Error() throws Exception {
		// given
		doThrow(new BadRequestException(MemberErrorCode.REDUNDANT_NICKNAME))
			.when(memberService)
			.checkNickname(anyString());
		String nickname = "일개미1234";

		// when & then
		mockMvc.perform(get("/api/auth/signup/duplicationcheck/nickname/{nickname}", nickname))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("닉네임이 중복되었습니다")));
	}

	@DisplayName("사용자는 로컬 이메일이 중복되었는지 검사한다")
	@Test
	void emailDuplicationCheck() throws Exception {
		// given
		String email = "dragonbead95@naver.com";

		// when & then
		mockMvc.perform(get("/api/auth/signup/duplicationcheck/email/{email}", email))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("이메일이 사용가능합니다")));
	}

	@DisplayName("사용자는 로컬 이메일이 중복되어 400 에러를 응답받는다")
	@Test
	void emailDuplicationCheck_whenDuplicatedEmail_thenResponse400Error() throws Exception {
		// given
		String email = "dragonbead95@naver.com";
		doThrow(new BadRequestException(MemberErrorCode.REDUNDANT_EMAIL))
			.when(memberService)
			.checkEmail(anyString());

		// when & then
		mockMvc.perform(get("/api/auth/signup/duplicationcheck/email/{email}", email))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("이메일이 중복되었습니다")));
	}

	@DisplayName("사용자는 이메일을 전달하고 이메일로 검증 코드를 받는다")
	@Test
	void sendVerifyCode() throws Exception {
		// given
		String body = ObjectMapperUtil.serialize(Map.of("email", "dragonbead95@naver.com"));

		// when & then
		mockMvc.perform(post("/api/auth/signup/verifyEmail")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("이메일로 검증 코드를 전송하였습니다")));
	}

	@DisplayName("사용자는 유효하지 않은 형식의 이메일을 가지고 검증 코드를 받을 수 없다")
	@Test
	void sendVerifyCode_whenInvalidEmail_thenResponse400Error() throws Exception {
		// given
		String body = ObjectMapperUtil.serialize(Map.of("email", ""));

		// when & then
		mockMvc.perform(post("/api/auth/signup/verifyEmail")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")));
	}

	private Member createMember() {
		return createMember("일개미1234");
	}

	private Member createMember(String nickname) {
		return createMember(nickname, "profileUrl");
	}

	private Member createMember(String nickname, String profileUrl) {
		return Member.builder()
			.id(1L)
			.nickname(nickname)
			.email("dragonbead95@naver.com")
			.provider("local")
			.password("password")
			.profileUrl(profileUrl)
			.build();
	}

	public MultipartFile createMockMultipartFile() {
		ClassPathResource classPathResource = new ClassPathResource("profile.jpeg");
		Path path = null;
		try {
			path = Paths.get(classPathResource.getURI());
			byte[] profile = Files.readAllBytes(path);
			return new MockMultipartFile("profileImageFile", "profile.jpeg", "image/jpeg",
				profile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Stream<Arguments> invalidSignupData() {
		return Stream.of(
			Arguments.of("", "", "", ""),
			Arguments.of("a", "a", "a", "a")
		);
	}
}
