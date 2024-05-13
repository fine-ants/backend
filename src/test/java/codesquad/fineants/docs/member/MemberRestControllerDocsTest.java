package codesquad.fineants.docs.member;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.snippet.Attributes;

import codesquad.fineants.docs.RestDocsSupport;
import codesquad.fineants.domain.jwt.domain.Jwt;
import codesquad.fineants.domain.member.controller.MemberRestController;
import codesquad.fineants.domain.member.domain.dto.request.LoginRequest;
import codesquad.fineants.domain.member.domain.dto.request.OauthMemberLoginRequest;
import codesquad.fineants.domain.member.domain.dto.request.OauthMemberRefreshRequest;
import codesquad.fineants.domain.member.domain.dto.request.ProfileChangeServiceRequest;
import codesquad.fineants.domain.member.domain.dto.request.SignUpServiceRequest;
import codesquad.fineants.domain.member.domain.dto.response.LoginResponse;
import codesquad.fineants.domain.member.domain.dto.response.OauthMemberLoginResponse;
import codesquad.fineants.domain.member.domain.dto.response.OauthMemberRefreshResponse;
import codesquad.fineants.domain.member.domain.dto.response.OauthSaveUrlResponse;
import codesquad.fineants.domain.member.domain.dto.response.ProfileChangeResponse;
import codesquad.fineants.domain.member.domain.dto.response.ProfileResponse;
import codesquad.fineants.domain.member.domain.dto.response.SignUpServiceResponse;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.service.MemberService;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.global.util.ObjectMapperUtil;

public class MemberRestControllerDocsTest extends RestDocsSupport {

	private final MemberService memberService = Mockito.mock(MemberService.class);

	@Override
	protected Object initController() {
		return new MemberRestController(memberService);
	}

	@DisplayName("회원 OAuth 로그인 API")
	@Test
	void login() throws Exception {
		// given
		String code = "1234";
		String redirectUrl = "http://localhost:5173/signin?provider=kakao";
		String state = "1234";
		String url = "/api/auth/{provider}/login";

		OauthMemberLoginResponse mockResponse = OauthMemberLoginResponse.builder()
			.jwt(Jwt.builder()
				.accessToken("accessToken")
				.refreshToken("refreshToken")
				.build())
			.build();
		given(memberService.login(ArgumentMatchers.any(OauthMemberLoginRequest.class)))
			.willReturn(mockResponse);
		// when
		mockMvc.perform(RestDocumentationRequestBuilders.post(url, "kakao")
				.queryParam("code", code)
				.queryParam("redirectUrl", redirectUrl)
				.queryParam("state", state)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("로그인에 성공하였습니다")))
			.andExpect(jsonPath("data.jwt.accessToken").value(equalTo("accessToken")))
			.andExpect(jsonPath("data.jwt.refreshToken").value(equalTo("refreshToken")))
			.andDo(
				document(
					"member_oauth-login",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					pathParameters(
						parameterWithName("provider").description("플랫폼 이름")
							.attributes(
								Attributes.key("constraints").value(
									String.join(",",
										"kakao",
										"google",
										"naver"
									)
								))
					),
					queryParameters(
						parameterWithName("code").description("인가 코드"),
						parameterWithName("redirectUrl").description("리다이렉트 URL"),
						parameterWithName("state").description("state")
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
						fieldWithPath("data.jwt").type(JsonFieldType.OBJECT)
							.description("Json Web Token"),
						fieldWithPath("data.jwt.accessToken").type(JsonFieldType.STRING)
							.description("액세스 토큰"),
						fieldWithPath("data.jwt.refreshToken").type(JsonFieldType.STRING)
							.description("리프레시 토큰")
					)
				)
			);
	}

	@DisplayName("회원 일반 로그인 API")
	@Test
	void loginByLocal() throws Exception {
		// given
		Member member = createMember();
		String url = "/api/auth/login";
		Map<String, Object> body = Map.of(
			"email", "dragonbead95@naver.com",
			"password", "nemo1234@"
		);

		LoginResponse mockResponse = LoginResponse.builder()
			.jwt(Jwt.builder()
				.accessToken("accessToken")
				.refreshToken("refreshToken")
				.build())
			.build();
		given(memberService.login(ArgumentMatchers.any(LoginRequest.class)))
			.willReturn(mockResponse);

		// when & then
		mockMvc.perform(post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("로그인에 성공하였습니다.")))
			.andExpect(jsonPath("data.jwt.accessToken").value(equalTo("accessToken")))
			.andExpect(jsonPath("data.jwt.refreshToken").value(equalTo("refreshToken")))
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
						fieldWithPath("data").type(JsonFieldType.OBJECT)
							.description("응답 데이터"),
						fieldWithPath("data.jwt").type(JsonFieldType.OBJECT)
							.description("Json Web Token"),
						fieldWithPath("data.jwt.accessToken").type(JsonFieldType.STRING)
							.description("액세스 토큰"),
						fieldWithPath("data.jwt.refreshToken").type(JsonFieldType.STRING)
							.description("리프레시 토큰")
					)
				)
			);
	}

	@DisplayName("회원 로그아웃 API")
	@Test
	void logout() throws Exception {
		// given
		String url = "/api/auth/logout";
		Map<String, Object> body = Map.of(
			"refreshToken", "refreshToken"
		);

		// when & then
		mockMvc.perform(post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body))
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
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
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
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
						fieldWithPath("data").type(JsonFieldType.NULL)
							.description("응답 데이터")
					)
				)
			);
	}

	@DisplayName("사용자 일반 회원가입 API")
	@Test
	void signup() throws Exception {
		// given
		given(memberService.signup(ArgumentMatchers.any(SignUpServiceRequest.class)))
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
			.andExpect(jsonPath("message").value(equalTo("회원가입이 완료되었습니다")))
			.andDo(
				document(
					"member-signup",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestParts(
						partWithName("profileImageFile")
							.optional()
							.description("프로필 파일"),
						partWithName("signupData")
							.description("회원가입 정보")
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

	@DisplayName("액세스 토큰 갱신 API")
	@Test
	void refreshAccessToken() throws Exception {
		// give
		Map<String, Object> body = Map.of(
			"refreshToken", "refreshToken"
		);

		given(memberService.refreshAccessToken(
			ArgumentMatchers.any(OauthMemberRefreshRequest.class),
			ArgumentMatchers.any(LocalDateTime.class)))
			.willReturn(OauthMemberRefreshResponse.builder()
				.accessToken("accessToken")
				.build());
		// when & then
		mockMvc.perform(post("/api/auth/refresh/token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("액세스 토큰 갱신에 성공하였습니다")))
			.andExpect(jsonPath("data.accessToken").value(equalTo("accessToken")))
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

	@DisplayName("회원 프로필 조회 API")
	@Test
	void readProfile() throws Exception {
		// given
		given(memberService.readProfile(ArgumentMatchers.any(AuthMember.class)))
			.willReturn(
				ProfileResponse.builder()
					.user(ProfileResponse.MemberProfile.builder()
						.id(1L)
						.nickname("일개미1234")
						.email("dragonbead95@naver.com")
						.profileUrl("profileUrl")
						.provider("local")
						.notificationPreferences(ProfileResponse.NotificationPreference.builder()
							.browserNotify(false)
							.targetGainNotify(true)
							.maxLossNotify(true)
							.targetPriceNotify(true)
							.build())
						.build())
					.build()
			);

		// when & then
		mockMvc.perform(get("/api/profile")
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("프로필 정보 조회에 성공하였습니다")))
			.andExpect(jsonPath("data.user.id").value(equalTo(1)))
			.andExpect(jsonPath("data.user.nickname").value(equalTo("일개미1234")))
			.andExpect(jsonPath("data.user.email").value(equalTo("dragonbead95@naver.com")))
			.andExpect(jsonPath("data.user.profileUrl").value(equalTo("profileUrl")))
			.andExpect(jsonPath("data.user.provider").value(equalTo("local")))
			.andExpect(jsonPath("data.user.notificationPreferences.browserNotify").value(equalTo(false)))
			.andExpect(jsonPath("data.user.notificationPreferences.targetGainNotify").value(equalTo(true)))
			.andExpect(jsonPath("data.user.notificationPreferences.maxLossNotify").value(equalTo(true)))
			.andExpect(jsonPath("data.user.notificationPreferences.targetPriceNotify").value(equalTo(true)))
			.andDo(
				document(
					"member-search",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
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
						fieldWithPath("data.user").type(JsonFieldType.OBJECT)
							.description("회원 정보"),
						fieldWithPath("data.user.id").type(JsonFieldType.NUMBER)
							.description("회원 등록번호"),
						fieldWithPath("data.user.nickname").type(JsonFieldType.STRING)
							.description("회원 닉네임"),
						fieldWithPath("data.user.email").type(JsonFieldType.STRING)
							.description("회원 이메일"),
						fieldWithPath("data.user.profileUrl").type(JsonFieldType.STRING)
							.description("회원 프로필 URL (NULL 허용)"),
						fieldWithPath("data.user.provider").type(JsonFieldType.STRING)
							.description("회원 가입 플랫폼"),
						fieldWithPath("data.user.notificationPreferences").type(JsonFieldType.OBJECT)
							.description("알림 설정"),
						fieldWithPath("data.user.notificationPreferences.browserNotify").type(JsonFieldType.BOOLEAN)
							.description("브라우저 알림 설정"),
						fieldWithPath("data.user.notificationPreferences.targetGainNotify").type(JsonFieldType.BOOLEAN)
							.description("목표 수익률 알림 설정"),
						fieldWithPath("data.user.notificationPreferences.maxLossNotify").type(JsonFieldType.BOOLEAN)
							.description("최대 손실율 알림 설정"),
						fieldWithPath("data.user.notificationPreferences.targetPriceNotify").type(JsonFieldType.BOOLEAN)
							.description("지정가 알림 설정")
					)
				)
			);

	}

	@DisplayName("회원 닉네임 중복 검사 API")
	@Test
	void nicknameDuplicationCheck() throws Exception {
		// given
		String nickname = "일개미1234";
		// when & then
		mockMvc.perform(
				RestDocumentationRequestBuilders.get("/api/auth/signup/duplicationcheck/nickname/{nickname}", nickname))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("닉네임이 사용가능합니다")))
			.andExpect(jsonPath("data").value(equalTo(null)))
			.andDo(
				document(
					"member_check_nickname-search",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					pathParameters(
						parameterWithName("nickname").description("닉네임")
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

	@DisplayName("회원 이메일 중복 검사 API")
	@Test
	void emailDuplicationCheck() throws Exception {
		// given
		String email = "dragonbead95@naver.com";
		// when & then
		mockMvc.perform(
				RestDocumentationRequestBuilders.get("/api/auth/signup/duplicationcheck/email/{email}", email))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("이메일이 사용가능합니다")))
			.andExpect(jsonPath("data").value(equalTo(null)))
			.andDo(
				document(
					"member_check_email-search",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					pathParameters(
						parameterWithName("email").description("이메일")
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

	@DisplayName("이메일 검증 코드 생성 API")
	@Test
	void sendVerifyCode() throws Exception {
		// given
		Map<String, Object> body = Map.of(
			"email", "kim1234@gmail.com"
		);

		// when & then
		mockMvc.perform(
				RestDocumentationRequestBuilders.post("/api/auth/signup/verifyEmail")
					.contentType(MediaType.APPLICATION_JSON)
					.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("이메일로 검증 코드를 전송하였습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)))
			.andDo(
				document(
					"member_email_validation_code-create",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestFields(
						fieldWithPath("email").type(JsonFieldType.STRING).description("이메일")
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

	@DisplayName("회원 인증 코드 검사 API")
	@Test
	void checkVerifyCode() throws Exception {
		// given
		Map<String, Object> body = Map.of(
			"email", "kim1234@gmail.com",
			"code", 123456
		);

		// when & then
		mockMvc.perform(
				RestDocumentationRequestBuilders.post("/api/auth/signup/verifyCode")
					.contentType(MediaType.APPLICATION_JSON)
					.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("일치하는 인증번호 입니다")))
			.andExpect(jsonPath("data").value(equalTo(null)))
			.andDo(
				document(
					"member_email_validation_code-verify",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestFields(
						fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
						fieldWithPath("code").type(JsonFieldType.NUMBER).description("인증 코드")
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

	@DisplayName("회원 프로필 수정 API")
	@Test
	void changeProfile() throws Exception {
		// given
		Member member = createMember();

		Map<String, Object> profileInformationMap = Map.of("nickname", "일개미12345");
		MockMultipartFile profileInformation = new MockMultipartFile(
			"profileInformation",
			"profileInformation",
			MediaType.APPLICATION_JSON_VALUE,
			ObjectMapperUtil.serialize(profileInformationMap)
				.getBytes(StandardCharsets.UTF_8));

		member.updateNickname("일개미12345");
		given(memberService.changeProfile(ArgumentMatchers.any(ProfileChangeServiceRequest.class)))
			.willReturn(ProfileChangeResponse.from(member));
		// when & then
		mockMvc.perform(multipart(POST, "/api/profile")
				.file((MockMultipartFile)createMockMultipartFile())
				.file(profileInformation)
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("프로필이 수정되었습니다")))
			.andExpect(jsonPath("data.user.id").value(equalTo(1)))
			.andExpect(jsonPath("data.user.nickname").value(equalTo("일개미12345")))
			.andExpect(jsonPath("data.user.email").value(equalTo("kim1234@gmail.com")))
			.andExpect(jsonPath("data.user.profileUrl").value(equalTo("profileUrl")))
			.andExpect(jsonPath("data.user.provider").value(equalTo("local")))
			.andDo(
				document(
					"member-update",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					requestParts(
						partWithName("profileImageFile")
							.optional()
							.description("프로필 파일"),
						partWithName("profileInformation")
							.optional()
							.description("회원 수정 정보")
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
						fieldWithPath("data.user").type(JsonFieldType.OBJECT)
							.description("회원 정보"),
						fieldWithPath("data.user.id").type(JsonFieldType.NUMBER)
							.description("회원 등록번호"),
						fieldWithPath("data.user.nickname").type(JsonFieldType.STRING)
							.description("회원 닉네임"),
						fieldWithPath("data.user.email").type(JsonFieldType.STRING)
							.description("회원 이메일"),
						fieldWithPath("data.user.profileUrl").type(JsonFieldType.STRING)
							.description("회원 프로필 URL (NULL 허용)"),
						fieldWithPath("data.user.provider").type(JsonFieldType.STRING)
							.description("회원 가입 플랫폼")
					)
				)
			);
	}

	@DisplayName("비밀번호 변경 API")
	@Test
	void changePassword() throws Exception {
		// given
		Map<String, Object> body = Map.of(
			"currentPassword", "currentPassword",
			"newPassword", "newPassword",
			"newPasswordConfirm", "newPasswordConfirm"
		);

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.put("/api/account/password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body))
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("비밀번호를 성공적으로 변경했습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)))
			.andDo(
				document(
					"member_password-update",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					requestFields(
						fieldWithPath("currentPassword").type(JsonFieldType.STRING).description("현재 비밀번호"),
						fieldWithPath("newPassword").type(JsonFieldType.STRING).description("새 비밀번호"),
						fieldWithPath("newPasswordConfirm").type(JsonFieldType.STRING).description("새 비밀번호 확인")
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

	@DisplayName("회원 계정 삭제 API")
	@Test
	void deleteAccount() throws Exception {
		// given
		Map<String, Object> body = Map.of(
			"refreshToken", "refreshToken"
		);

		// when & then
		mockMvc.perform(delete("/api/account")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body))
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("계정이 삭제되었습니다")))
			.andDo(
				document(
					"member-delete",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					requestFields(
						fieldWithPath("refreshToken").type(JsonFieldType.STRING)
							.description("리프레시 토큰")
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

	@DisplayName("인가코드 요청 URL")
	@Test
	void saveAuthorizationCodeURL() throws Exception {
		// give
		given(memberService.saveAuthorizationCodeURL(anyString()))
			.willReturn(OauthSaveUrlResponse.builder()
				.authURL("authURL")
				.build());
		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/auth/{provider}/authUrl", "kakao"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("인가 코드 URL 요청에 성공하였습니다")))
			.andExpect(jsonPath("data.authURL").value(equalTo("authURL")))
			.andDo(
				document(
					"member_authorization_code-create",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					pathParameters(
						parameterWithName("provider").description("플랫폼 이름")
							.attributes(
								Attributes.key("constraints").value(
									String.join(",",
										"kakao",
										"google",
										"naver"
									)
								))
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
						fieldWithPath("data.authURL").type(JsonFieldType.STRING)
							.description("인가 코드 요청 URL")
					)
				)
			);
	}
}
