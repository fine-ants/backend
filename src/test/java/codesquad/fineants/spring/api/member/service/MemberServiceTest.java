package codesquad.fineants.spring.api.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.notification_preference.NotificationPreferenceRepository;
import codesquad.fineants.domain.oauth.client.AuthorizationCodeRandomGenerator;
import codesquad.fineants.domain.oauth.client.OauthClient;
import codesquad.fineants.domain.oauth.repository.OauthClientRepository;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.spring.AbstractContainerBaseTest;
import codesquad.fineants.spring.api.S3.service.AmazonS3Service;
import codesquad.fineants.spring.api.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.OauthErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.errors.exception.FineAntsException;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.member.request.AuthorizationRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberLoginRequest;
import codesquad.fineants.spring.api.member.request.ProfileChangeRequest;
import codesquad.fineants.spring.api.member.request.SignUpRequest;
import codesquad.fineants.spring.api.member.request.VerifyCodeRequest;
import codesquad.fineants.spring.api.member.request.VerifyEmailRequest;
import codesquad.fineants.spring.api.member.response.OauthMemberLoginResponse;
import codesquad.fineants.spring.api.member.response.OauthUserProfile;
import codesquad.fineants.spring.api.member.response.ProfileChangeResponse;
import codesquad.fineants.spring.api.member.response.ProfileResponse;
import codesquad.fineants.spring.api.member.service.request.ProfileChangeServiceRequest;
import codesquad.fineants.spring.api.member.service.request.SignUpServiceRequest;
import codesquad.fineants.spring.api.member.service.response.SignUpServiceResponse;
import codesquad.fineants.spring.api.portfolio_notification.MailService;
import codesquad.fineants.spring.util.ObjectMapperUtil;

public class MemberServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private NotificationPreferenceRepository preferenceRepository;

	@MockBean
	private AuthorizationCodeRandomGenerator authorizationCodeRandomGenerator;

	@MockBean
	private OauthClientRepository oauthClientRepository;

	@MockBean
	private OauthClient oauthClient;

	@MockBean
	private AmazonS3Service amazonS3Service;

	@MockBean
	private MailService mailService;

	@MockBean
	private OauthMemberRedisService redisService;

	@MockBean
	private VerifyCodeGenerator verifyCodeGenerator;

	@AfterEach
	void tearDown() {
		preferenceRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
	}

	@DisplayName("존재하지 않은 provider를 제공하여 로그인 시도시 예외가 발생한다")
	@Test
	void loginWithNotExistProvider() {
		// given
		String provider = "invalidProvider";
		given(oauthClientRepository.findOneBy(anyString())).willThrow(
			new NotFoundResourceException(OauthErrorCode.NOT_FOUND_PROVIDER));

		OauthMemberLoginRequest loginRequest = createOauthMemberLoginServiceRequest(provider);

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
		memberService.saveAuthorizationCodeURL(provider);

		OauthMemberLoginRequest loginRequest = createOauthMemberLoginServiceRequest(provider);

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
		OauthMemberLoginRequest loginRequest = createOauthMemberLoginServiceRequest(provider);

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

	@DisplayName("사용자가 소셜 로그인을 합니다")
	@CsvSource(value = {"naver,fineants.co@naver.com", "kakao,fineants.co@gmail.com", "google,fineants.co@gmail.com"})
	@ParameterizedTest
	void loginWithNaver(String provider, String email) {
		// given
		String state = "1234";
		given(authorizationCodeRandomGenerator.generateAuthorizationRequest()).willReturn(
			createAuthorizationRequest(state));
		given(oauthClientRepository.findOneBy(anyString())).willReturn(oauthClient);
		given(oauthClient.fetchProfile(any(OauthMemberLoginRequest.class), any(AuthorizationRequest.class)))
			.willReturn(createOauthUserProfile(email, provider));
		memberService.saveAuthorizationCodeURL(provider);

		OauthMemberLoginRequest loginRequest = createOauthMemberLoginServiceRequest(provider);

		// when
		OauthMemberLoginResponse response = memberService.login(loginRequest);

		// then
		assertAll(
			() -> assertThat(response)
				.extracting("user")
				.extracting("email")
				.isEqualTo(email),
			() -> assertThat(memberRepository.findMemberByEmailAndProvider(email, provider).orElseThrow())
				.isNotNull()
		);
	}

	@DisplayName("사용자는 회원의 프로필을 변경한다")
	@MethodSource(value = "changeProfileMethodSource")
	@ParameterizedTest
	void changeProfile(MultipartFile profileImageFile, ProfileChangeRequest request) {
		// given
		Member member = memberRepository.save(createMember());
		ProfileChangeServiceRequest serviceRequest = new ProfileChangeServiceRequest(profileImageFile, request,
			AuthMember.from(member));

		// when
		ProfileChangeResponse response = memberService.changeProfile(serviceRequest);

		// then
		Member findMember = memberRepository.findById(member.getId()).orElseThrow();
		ProfileChangeResponse expected = ProfileChangeResponse.from(findMember);
		assertThat(response).isEqualTo(expected);
	}

	@DisplayName("사용자가 변경할 정보 없이 프로필을 변경하는 경우 예외가 발생한다")
	@Test
	void changeProfile_whenNoChangeInformation_thenResponse400Error() {
		// given
		Member member = memberRepository.save(createMember());
		MultipartFile profileImageFile = null;
		ProfileChangeRequest request = null;
		ProfileChangeServiceRequest serviceRequest = new ProfileChangeServiceRequest(profileImageFile, request,
			AuthMember.from(member));

		// when
		Throwable throwable = catchThrowable(() -> memberService.changeProfile(serviceRequest));

		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage("변경할 회원 정보가 없습니다");
	}

	@DisplayName("사용자가 중복된 닉네임으로 프로필을 변경하려고 하면 400 에러를 응답합니다")
	@Test
	void changeProfile_whenDuplicatedNickname_thenResponse400Error() {
		// given
		Member member = memberRepository.save(createMember());
		ProfileChangeServiceRequest serviceRequest = new ProfileChangeServiceRequest(
			createMockMultipartFile(),
			createProfileChangeRequest(member.getNickname()),
			AuthMember.from(member));

		// when
		Throwable throwable = catchThrowable(() -> memberService.changeProfile(serviceRequest));

		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage("닉네임이 중복되었습니다");
	}

	@DisplayName("사용자는 일반 회원가입한다")
	@MethodSource(value = "signupMethodSource")
	@ParameterizedTest
	void signup(SignUpRequest request, MultipartFile profileImageFile, String expectedProfileUrl) {
		// given
		given(amazonS3Service.upload(any(MultipartFile.class)))
			.willReturn("profileUrl");
		SignUpServiceRequest serviceRequest = SignUpServiceRequest.of(request, profileImageFile);

		// when
		SignUpServiceResponse response = memberService.signup(serviceRequest);

		// then
		assertThat(response)
			.extracting("nickname", "email", "profileUrl", "provider")
			.containsExactlyInAnyOrder("일개미1234", "dragonbead95@naver.com", expectedProfileUrl, "local");
	}

	@DisplayName("사용자는 닉네임이 중복되어 회원가입 할 수 없다")
	@Test
	void signup_whenDuplicatedNickname_thenResponse400Error() {
		// given
		String duplicatedNickname = "일개미1234";
		memberRepository.save(createMember(duplicatedNickname));
		SignUpRequest request = new SignUpRequest(
			duplicatedNickname,
			"nemo1234@naver.com",
			"nemo1234@",
			"nemo1234@"
		);
		SignUpServiceRequest serviceRequest = SignUpServiceRequest.of(request, createMockMultipartFile());

		// when
		Throwable throwable = catchThrowable(() -> memberService.signup(serviceRequest));

		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage(MemberErrorCode.REDUNDANT_NICKNAME.getMessage());
	}

	@DisplayName("사용자는 이메일이 중복되어 회원가입 할 수 없다")
	@Test
	void signup_whenDuplicatedEmail_thenResponse400Error() {
		// given
		String duplicatedEmail = "dragonbead95@naver.com";
		memberRepository.save(createMember("일개미1234"));
		SignUpRequest request = new SignUpRequest(
			"일개미4567",
			duplicatedEmail,
			"nemo1234@",
			"nemo1234@"
		);
		SignUpServiceRequest serviceRequest = SignUpServiceRequest.of(request, createMockMultipartFile());

		// when
		Throwable throwable = catchThrowable(() -> memberService.signup(serviceRequest));

		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage(MemberErrorCode.REDUNDANT_EMAIL.getMessage());
	}

	@DisplayName("사용자는 비밀번호와 비밀번호 확인이 일치하지 않아 회원가입 할 수 없다")
	@Test
	void signup_whenNotMatchPasswordAndPasswordConfirm_thenResponse400Error() {
		// given
		memberRepository.save(createMember("일개미1234"));
		SignUpRequest request = new SignUpRequest(
			"일개미4567",
			"nemo1234@naver.com",
			"nemo1234@",
			"nemo4567@"
		);
		SignUpServiceRequest serviceRequest = SignUpServiceRequest.of(request, createMockMultipartFile());

		// when
		Throwable throwable = catchThrowable(() -> memberService.signup(serviceRequest));

		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage(MemberErrorCode.PASSWORD_CHECK_FAIL.getMessage());
	}

	@DisplayName("사용자는 프로필 이미지 사이즈를 초과하여 회원가입 할 수 없다")
	@Test
	void signup_whenOverProfileImageFile_thenResponse400Error() {
		// given
		given(amazonS3Service.upload(any(MultipartFile.class)))
			.willThrow(new BadRequestException(MemberErrorCode.PROFILE_IMAGE_UPLOAD_FAIL));

		SignUpRequest request = new SignUpRequest(
			"일개미4567",
			"nemo1234@naver.com",
			"nemo1234@",
			"nemo1234@"
		);
		SignUpServiceRequest serviceRequest = SignUpServiceRequest.of(request, createMockMultipartFile());

		// when
		Throwable throwable = catchThrowable(() -> memberService.signup(serviceRequest));

		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage(MemberErrorCode.PROFILE_IMAGE_UPLOAD_FAIL.getMessage());
	}

	@DisplayName("사용자는 닉네임이 중복되었는지 체크한다")
	@Test
	void checkNickname() {
		// given
		String nickname = "일개미1234";
		// when & then
		assertDoesNotThrow(() -> memberService.checkNickname(nickname));
	}

	@DisplayName("사용자는 닉네임이 중복되어 에러를 받는다")
	@Test
	void checkNickname_whenDuplicatedNickname_thenThrow400Error() {
		// given
		memberRepository.save(createMember("일개미1234"));
		String nickname = "일개미1234";

		// when
		Throwable throwable = catchThrowable(() -> memberService.checkNickname(nickname));

		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage(MemberErrorCode.REDUNDANT_NICKNAME.getMessage());
	}

	@DisplayName("사용자는 이메일이 중복되었는지 검사한다")
	@Test
	void checkEmail() {
		// given
		String email = "dragonbead95@naver.com";
		// when & then
		assertDoesNotThrow(() -> memberService.checkEmail(email));
	}

	@DisplayName("사용자는 이메일 중복 검사 요청시 로컬 이메일이 존재하여 예외가 발생한다")
	@Test
	void checkEmail_whenDuplicatedLocalEmail_thenThrowBadRequestException() {
		// given
		Member member = memberRepository.save(createMember());
		String email = member.getEmail();

		// when
		Throwable throwable = catchThrowable(() -> memberService.checkEmail(email));

		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage(MemberErrorCode.REDUNDANT_EMAIL.getMessage());
	}

	@DisplayName("사용자는 이메일에 대한 검증 코드를 이메일로 전송받는다")
	@Test
	void sendVerifyCode() {
		// given
		given(verifyCodeGenerator.generate()).willReturn("123456");

		VerifyEmailRequest request = ObjectMapperUtil.deserialize(
			ObjectMapperUtil.serialize(Map.of("email", "dragonbead95@naver.com")),
			VerifyEmailRequest.class);

		// when
		memberService.sendVerifyCode(request);

		// then
		verify(redisService, times(1))
			.saveEmailVerifCode("dragonbead95@naver.com", "123456");
		verify(mailService, times(1))
			.sendEmail("dragonbead95@naver.com", "Finants 회원가입 인증 코드", "인증코드를 회원가입 페이지에 입력해주세요: 123456");
	}

	@DisplayName("사용자는 검증코드를 제출하여 검증코드가 일치하는지 검사한다")
	@Test
	void checkVerifyCode() {
		// given
		given(redisService.get("dragonbead95@naver.com"))
			.willReturn(Optional.of("123456"));

		VerifyCodeRequest request = ObjectMapperUtil.deserialize(
			ObjectMapperUtil.serialize(Map.of("email", "dragonbead95@naver.com", "code", "123456")),
			VerifyCodeRequest.class);

		// when & then
		assertDoesNotThrow(() -> memberService.checkVerifyCode(request));
	}

	@DisplayName("사용자는 매치되지 않은 검증 코드를 전달하며 검사를 요청했을때 예외가 발생한다")
	@Test
	void checkVerifyCode_whenNotMatchVerifyCode_thenThrowException() {
		// given
		given(redisService.get("dragonbead95@naver.com"))
			.willReturn(Optional.of("123456"));

		VerifyCodeRequest request = ObjectMapperUtil.deserialize(
			ObjectMapperUtil.serialize(Map.of("email", "dragonbead95@naver.com", "code", "234567")),
			VerifyCodeRequest.class);

		// when
		Throwable throwable = catchThrowable(() -> memberService.checkVerifyCode(request));

		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage(MemberErrorCode.VERIFICATION_CODE_CHECK_FAIL.getMessage());
	}

	@DisplayName("사용자는 프로필을 조회합니다.")
	@Test
	void readProfile() {
		// given
		Member member = memberRepository.save(createMember());

		// when
		ProfileResponse response = memberService.readProfile(AuthMember.from(member));

		// then
		assertThat(response)
			.extracting("user")
			.extracting("id", "nickname", "email", "profileUrl")
			.containsExactlyInAnyOrder(member.getId(), "nemo1234", "dragonbead95@naver.com", "profileUrl");
		assertThat(response)
			.extracting("user.notificationPreferences")
			.extracting("browserNotify", "targetGainNotify", "maxLossNotify", "targetPriceNotify")
			.containsExactlyInAnyOrder(false, true, true, true);
	}

	private AuthorizationRequest createAuthorizationRequest(String state) {
		String codeVerifier = "1234";
		String codeChallenge = "1234";
		String nonce = "f46e2977378ed6cdf2f03b5962101f7d";
		return AuthorizationRequest.of(state, codeVerifier, codeChallenge, nonce);
	}

	private OauthUserProfile createOauthUserProfile(String email, String provider) {
		String profileImage = "profileImage";
		return new OauthUserProfile(email, profileImage, provider);
	}

	private OauthMemberLoginRequest createOauthMemberLoginServiceRequest(String provider) {
		String code = "1234";
		String redirectUrl = "http://localhost:5173/signin?provider=" + provider;
		String state = "1234";
		return OauthMemberLoginRequest.of(provider,
			code, redirectUrl, state, LocalDate.of(2023, 11, 8).atStartOfDay());
	}

	private static Member createMember() {
		return createMember("nemo1234");
	}

	private static Member createMember(String nickname) {
		return createMember(nickname, "profileUrl");
	}

	private static Member createMember(String nickname, String profileUrl) {
		return Member.builder()
			.email("dragonbead95@naver.com")
			.nickname(nickname)
			.provider("local")
			.password("nemo1234@")
			.profileUrl(profileUrl)
			.build();
	}

	public static Stream<Arguments> changeProfileMethodSource() {
		String nickname = "nemo12345";
		return Stream.of(
			Arguments.of(
				null,
				createProfileChangeRequest(nickname)
			),
			Arguments.of(
				createMockMultipartFile(),
				null
			),
			Arguments.of(
				createMockMultipartFile(),
				createProfileChangeRequest(nickname)
			)
		);
	}

	public static MultipartFile createMockMultipartFile() {
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

	@NotNull
	private static ProfileChangeRequest createProfileChangeRequest(String nickname) {
		return new ProfileChangeRequest(nickname);
	}

	public static Stream<Arguments> signupMethodSource() {
		SignUpRequest request = new SignUpRequest(
			"일개미1234",
			"dragonbead95@naver.com",
			"nemo1234@",
			"nemo1234@"
		);
		MultipartFile profileImageFile = createMockMultipartFile();
		return Stream.of(
			Arguments.of(request, profileImageFile, "profileUrl"),
			Arguments.of(request, null, null)
		);
	}
}
