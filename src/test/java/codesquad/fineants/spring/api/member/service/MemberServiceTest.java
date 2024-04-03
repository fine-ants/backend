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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
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

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.fcm_token.FcmRepository;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.notification.NotificationRepository;
import codesquad.fineants.domain.notification_preference.NotificationPreference;
import codesquad.fineants.domain.notification_preference.NotificationPreferenceRepository;
import codesquad.fineants.domain.oauth.client.AuthorizationCodeRandomGenerator;
import codesquad.fineants.domain.oauth.client.OauthClient;
import codesquad.fineants.domain.oauth.repository.OauthClientRepository;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistoryRepository;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.portfolio_holding.PortfolioHoldingRepository;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.purchase_history.PurchaseHistoryRepository;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.domain.stock_dividend.StockDividend;
import codesquad.fineants.domain.stock_dividend.StockDividendRepository;
import codesquad.fineants.domain.stock_target_price.StockTargetPrice;
import codesquad.fineants.domain.stock_target_price.StockTargetPriceRepository;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotification;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotificationRepository;
import codesquad.fineants.domain.watch_list.WatchList;
import codesquad.fineants.domain.watch_list.WatchListRepository;
import codesquad.fineants.domain.watch_stock.WatchStock;
import codesquad.fineants.domain.watch_stock.WatchStockRepository;
import codesquad.fineants.spring.AbstractContainerBaseTest;
import codesquad.fineants.spring.api.S3.service.AmazonS3Service;
import codesquad.fineants.spring.api.common.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.common.errors.errorcode.OauthErrorCode;
import codesquad.fineants.spring.api.common.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.common.errors.exception.FineAntsException;
import codesquad.fineants.spring.api.common.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.mail.service.MailService;
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
import codesquad.fineants.spring.util.ObjectMapperUtil;

public class MemberServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private NotificationPreferenceRepository preferenceRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private PortfolioHoldingRepository portfolioHoldingRepository;

	@Autowired
	private PurchaseHistoryRepository purchaseHistoryRepository;

	@Autowired
	private StockTargetPriceRepository stockTargetPriceRepository;

	@Autowired
	private TargetPriceNotificationRepository targetPriceNotificationRepository;

	@Autowired
	private StockDividendRepository stockDividendRepository;

	@Autowired
	private FcmRepository fcmRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private PortfolioGainHistoryRepository portfolioGainHistoryRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private WatchListRepository watchListRepository;

	@Autowired
	private WatchStockRepository watchStockRepository;

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
		fcmRepository.deleteAllInBatch();
		notificationRepository.deleteAllInBatch();
		targetPriceNotificationRepository.deleteAllInBatch();
		stockTargetPriceRepository.deleteAllInBatch();
		preferenceRepository.deleteAllInBatch();
		purchaseHistoryRepository.deleteAllInBatch();
		portfolioHoldingRepository.deleteAllInBatch();
		portfolioGainHistoryRepository.deleteAllInBatch();
		portfolioRepository.deleteAllInBatch();
		watchStockRepository.deleteAllInBatch();
		watchListRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
		stockDividendRepository.deleteAllInBatch();
		stockRepository.deleteAllInBatch();
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

	@DisplayName("사용자는 회원의 프로필에서 새 프로필 사진과 닉네임을 변경한다")
	@Test
	void changeProfile() {
		// given
		Member member = memberRepository.save(createMember());
		ProfileChangeServiceRequest serviceRequest = ProfileChangeServiceRequest.of(
			createProfileFile(),
			new ProfileChangeRequest("nemo12345"),
			member.getId()
		);

		given(amazonS3Service.upload(any(MultipartFile.class)))
			.willReturn("profileUrl");
		// when
		ProfileChangeResponse response = memberService.changeProfile(serviceRequest);

		// then
		assertThat(response)
			.extracting("user")
			.extracting("id", "nickname", "email", "profileUrl")
			.containsExactlyInAnyOrder(member.getId(), "nemo12345", "dragonbead95@naver.com", "profileUrl");
	}

	@DisplayName("사용자는 회원 프로필에서 새 프로필만 변경한다")
	@Test
	void changeProfile_whenNewProfile_thenOK() {
		// given
		Member member = memberRepository.save(createMember());
		ProfileChangeServiceRequest serviceRequest = ProfileChangeServiceRequest.of(
			createProfileFile(),
			null,
			member.getId()
		);

		given(amazonS3Service.upload(any(MultipartFile.class)))
			.willReturn("profileUrl");
		// when
		ProfileChangeResponse response = memberService.changeProfile(serviceRequest);

		// then
		assertThat(response)
			.extracting("user")
			.extracting("id", "nickname", "email", "profileUrl")
			.containsExactlyInAnyOrder(member.getId(), "nemo1234", "dragonbead95@naver.com", "profileUrl");
	}

	@DisplayName("사용자는 회원 프로필에서 기본 프로필로만 변경한다")
	@Test
	void changeProfile_whenEmptyProfile_thenOK() {
		// given
		Member member = memberRepository.save(createMember());
		ProfileChangeServiceRequest serviceRequest = ProfileChangeServiceRequest.of(
			createEmptyProfileImageFile(),
			null,
			member.getId()
		);

		given(amazonS3Service.upload(any(MultipartFile.class)))
			.willReturn("profileUrl");
		// when
		ProfileChangeResponse response = memberService.changeProfile(serviceRequest);

		// then
		assertThat(response)
			.extracting("user")
			.extracting("id", "nickname", "email", "profileUrl")
			.containsExactlyInAnyOrder(member.getId(), "nemo1234", "dragonbead95@naver.com", null);
	}

	@DisplayName("사용자는 회원 프로필에서 프로필은 유지하고 닉네임만 변경한다")
	@Test
	void changeProfile_whenChangeNickname_thenOK() {
		// given
		Member member = memberRepository.save(createMember());
		ProfileChangeServiceRequest serviceRequest = ProfileChangeServiceRequest.of(
			null,
			new ProfileChangeRequest("nemo12345"),
			member.getId()
		);

		// when
		ProfileChangeResponse response = memberService.changeProfile(serviceRequest);

		// then
		assertThat(response)
			.extracting("user")
			.extracting("id", "nickname", "email", "profileUrl")
			.containsExactlyInAnyOrder(member.getId(), "nemo12345", "dragonbead95@naver.com", "profileUrl");
	}

	@DisplayName("사용자는 회원 프로필에서 자기 닉네임을 그대로 수정한다")
	@Test
	void changeProfile_whenNoChangeNickname_thenOK() {
		// given
		Member member = memberRepository.save(createMember());
		ProfileChangeServiceRequest serviceRequest = ProfileChangeServiceRequest.of(
			createProfileFile(),
			new ProfileChangeRequest("nemo1234"),
			member.getId()
		);

		given(amazonS3Service.upload(any(MultipartFile.class)))
			.willReturn("profileUrl");

		// when
		ProfileChangeResponse response = memberService.changeProfile(serviceRequest);

		// then
		assertThat(response)
			.extracting("user")
			.extracting("id", "nickname", "email", "profileUrl")
			.containsExactlyInAnyOrder(member.getId(), "nemo1234", "dragonbead95@naver.com", "profileUrl");
	}

	@DisplayName("사용자는 회원 프로필에서 닉네임 변경시 중복되어 변경하지 못한다")
	@Test
	void changeProfile_whenDuplicateNickname_thenThrowException() {
		// given
		memberRepository.save(createMember("nemo12345"));
		Member member = memberRepository.save(createMember());
		ProfileChangeServiceRequest serviceRequest = ProfileChangeServiceRequest.of(
			null,
			new ProfileChangeRequest("nemo12345"),
			member.getId()
		);

		// when
		Throwable throwable = catchThrowable(() -> memberService.changeProfile(serviceRequest));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage(MemberErrorCode.REDUNDANT_NICKNAME.getMessage());
	}

	@DisplayName("사용자는 회원 프로필에서 변경할 정보가 없어서 실패한다")
	@Test
	void changeProfile_whenNoChangeProfile_thenThrowException() {
		// given
		Member member = memberRepository.save(createMember());
		ProfileChangeServiceRequest serviceRequest = ProfileChangeServiceRequest.of(
			null,
			null,
			member.getId()
		);

		// when
		Throwable throwable = catchThrowable(() -> memberService.changeProfile(serviceRequest));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage(MemberErrorCode.NO_PROFILE_CHANGES.getMessage());
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

	@DisplayName("사용자는 일반 회원가입 할때 프로필 사진을 기본 프로필 사진으로 가입한다")
	@Test
	void signup_whenDefaultProfile_thenSaveDefaultProfileUrl() {
		// given
		SignUpRequest request = new SignUpRequest(
			"일개미1234",
			"dragonbead95@naver.com",
			"nemo1234@",
			"nemo1234@"
		);
		MultipartFile profileImageFile = null;
		SignUpServiceRequest serviceRequest = SignUpServiceRequest.of(request, profileImageFile);

		// when
		SignUpServiceResponse response = memberService.signup(serviceRequest);

		// then
		assertThat(response)
			.extracting("nickname", "email", "profileUrl", "provider")
			.containsExactlyInAnyOrder("일개미1234", "dragonbead95@naver.com", null, "local");
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
		SignUpServiceRequest serviceRequest = SignUpServiceRequest.of(request, createProfileFile());

		// when
		Throwable throwable = catchThrowable(() -> memberService.signup(serviceRequest));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
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
		SignUpServiceRequest serviceRequest = SignUpServiceRequest.of(request, createProfileFile());

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
		SignUpServiceRequest serviceRequest = SignUpServiceRequest.of(request, createProfileFile());

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
		SignUpServiceRequest serviceRequest = SignUpServiceRequest.of(request, createProfileFile());

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

	@DisplayName("사용자가 닉네임 중복 체크시 입력형식이 잘못되어 실패한다")
	@Test
	void checkNickname_whenInvalidInput_thenThrowException() {
		// given
		String nickname = "일";
		// when & then
		Throwable throwable = catchThrowable(() -> memberService.checkNickname(nickname));
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage(MemberErrorCode.BAD_SIGNUP_INPUT.getMessage());
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
		preferenceRepository.save(NotificationPreference.defaultSetting(member));

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
			.containsExactlyInAnyOrder(false, false, false, false);
	}

	@DisplayName("사용자는 계정을 삭제한다")
	@Test
	void deleteMember() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		stockDividendRepository.saveAll(createStockDividendWith(stock));
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding));
		preferenceRepository.save(createNotificationPreference(member));
		StockTargetPrice stockTargetPrice = stockTargetPriceRepository.save(createStockTargetPrice(member, stock));
		targetPriceNotificationRepository.save(createTargetPriceNotification(stockTargetPrice));
		WatchList watchList = watchListRepository.save(createWatchList(member));
		watchStockRepository.save(createWatchStock(watchList, stock));

		// when
		memberService.deleteMember(AuthMember.from(member));

		// then
		assertThat(memberRepository.findById(member.getId())).isEmpty();
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

	public static MultipartFile createProfileFile() {
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

	private static MultipartFile createEmptyProfileImageFile() {
		return new MockMultipartFile("profileImageFile", new byte[] {});
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
		MultipartFile profileImageFile = createProfileFile();
		return Stream.of(
			Arguments.of(request, profileImageFile, "profileUrl")
		);
	}

	private Portfolio createPortfolio(Member member) {
		return Portfolio.builder()
			.name("내꿈은 워렌버핏")
			.securitiesFirm("토스")
			.budget(Money.from(1000000L))
			.targetGain(1500000L)
			.maximumLoss(900000L)
			.member(member)
			.targetGainIsActive(false)
			.maximumLossIsActive(false)
			.build();
	}

	private Stock createStock() {
		return Stock.builder()
			.companyName("삼성전자보통주")
			.tickerSymbol("005930")
			.companyNameEng("SamsungElectronics")
			.stockCode("KR7005930003")
			.sector("전기전자")
			.market(Market.KOSPI)
			.build();
	}

	private Stock createStock(String companyName, String tickerSymbol, String companyNameEng, String stockCode,
		String sector, Market market) {
		return Stock.builder()
			.companyName(companyName)
			.tickerSymbol(tickerSymbol)
			.companyNameEng(companyNameEng)
			.stockCode(stockCode)
			.sector(sector)
			.market(market)
			.build();
	}

	private StockDividend createStockDividend(LocalDate exDividendDate, LocalDate recordDate, LocalDate paymentDate,
		Stock stock) {
		return StockDividend.builder()
			.dividend(361L)
			.exDividendDate(exDividendDate)
			.recordDate(recordDate)
			.paymentDate(paymentDate)
			.stock(stock)
			.build();
	}

	private PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock) {
		return PortfolioHolding.builder()
			.portfolio(portfolio)
			.stock(stock)
			.build();
	}

	private PurchaseHistory createPurchaseHistory(PortfolioHolding portfolioHolding) {
		return PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.of(2023, 9, 26, 9, 30, 0))
			.numShares(3L)
			.purchasePricePerShare(50000.0)
			.memo("첫구매")
			.portfolioHolding(portfolioHolding)
			.build();
	}

	private List<StockDividend> createStockDividendWith(Stock stock) {
		return List.of(
			createStockDividend(
				LocalDate.of(2022, 12, 30),
				LocalDate.of(2022, 12, 31),
				LocalDate.of(2023, 4, 14),
				stock),
			createStockDividend(
				LocalDate.of(2023, 3, 30),
				LocalDate.of(2023, 3, 31),
				LocalDate.of(2023, 5, 17),
				stock),
			createStockDividend(
				LocalDate.of(2023, 6, 29),
				LocalDate.of(2023, 6, 30),
				LocalDate.of(2023, 8, 16),
				stock),
			createStockDividend(
				LocalDate.of(2023, 9, 27),
				LocalDate.of(2023, 9, 30),
				LocalDate.of(2023, 11, 20),
				stock),
			createStockDividend(
				LocalDate.of(2024, 3, 29),
				LocalDate.of(2024, 3, 31),
				LocalDate.of(2024, 5, 17),
				stock),
			createStockDividend(
				LocalDate.of(2024, 6, 28),
				LocalDate.of(2024, 6, 30),
				LocalDate.of(2024, 8, 16),
				stock),
			createStockDividend(
				LocalDate.of(2024, 9, 27),
				LocalDate.of(2024, 9, 30),
				LocalDate.of(2024, 11, 20),
				stock)
		);
	}

	private NotificationPreference createNotificationPreference(Member member) {
		return NotificationPreference.builder()
			.browserNotify(true)
			.targetGainNotify(true)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.member(member)
			.build();
	}

	private StockTargetPrice createStockTargetPrice(Member member, Stock stock) {
		return StockTargetPrice.builder()
			.isActive(true)
			.stock(stock)
			.member(member)
			.build();
	}

	private TargetPriceNotification createTargetPriceNotification(StockTargetPrice stockTargetPrice) {
		return TargetPriceNotification.builder()
			.targetPrice(60000L)
			.stockTargetPrice(stockTargetPrice)
			.build();
	}

	private WatchList createWatchList(Member member) {
		return WatchList.builder()
			.name("관심 종목1")
			.member(member)
			.build();
	}

	private WatchStock createWatchStock(WatchList watchList, Stock stock) {
		return WatchStock.builder()
			.stock(stock)
			.watchList(watchList)
			.build();
	}
}
