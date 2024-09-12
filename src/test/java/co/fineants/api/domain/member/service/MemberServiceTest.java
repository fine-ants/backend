package co.fineants.api.domain.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.dividend.repository.StockDividendRepository;
import co.fineants.api.domain.fcm.repository.FcmRepository;
import co.fineants.api.domain.gainhistory.repository.PortfolioGainHistoryRepository;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.member.domain.dto.request.ProfileChangeRequest;
import co.fineants.api.domain.member.domain.dto.request.ProfileChangeServiceRequest;
import co.fineants.api.domain.member.domain.dto.request.SignUpRequest;
import co.fineants.api.domain.member.domain.dto.request.SignUpServiceRequest;
import co.fineants.api.domain.member.domain.dto.request.VerifyCodeRequest;
import co.fineants.api.domain.member.domain.dto.request.VerifyEmailRequest;
import co.fineants.api.domain.member.domain.dto.response.ProfileChangeResponse;
import co.fineants.api.domain.member.domain.dto.response.ProfileResponse;
import co.fineants.api.domain.member.domain.dto.response.SignUpServiceResponse;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.member.repository.MemberRoleRepository;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.domain.notificationpreference.repository.NotificationPreferenceRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
import co.fineants.api.domain.stock_target_price.repository.StockTargetPriceRepository;
import co.fineants.api.domain.stock_target_price.repository.TargetPriceNotificationRepository;
import co.fineants.api.domain.watchlist.domain.entity.WatchList;
import co.fineants.api.domain.watchlist.repository.WatchListRepository;
import co.fineants.api.domain.watchlist.repository.WatchStockRepository;
import co.fineants.api.global.errors.errorcode.MemberErrorCode;
import co.fineants.api.global.errors.exception.BadRequestException;
import co.fineants.api.global.errors.exception.FineAntsException;
import co.fineants.api.global.util.ObjectMapperUtil;
import co.fineants.api.infra.mail.service.MailService;
import co.fineants.api.infra.s3.service.AmazonS3Service;

public class MemberServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberRoleRepository memberRoleRepository;

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
	private AmazonS3Service amazonS3Service;

	@MockBean
	private MailService mailService;

	@MockBean
	private OauthMemberRedisService redisService;

	@MockBean
	private VerifyCodeGenerator verifyCodeGenerator;

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

		// when
		ProfileResponse response = memberService.readProfile(member.getId());

		// then
		assertThat(response)
			.extracting("user")
			.extracting("id", "nickname", "email", "profileUrl")
			.containsExactlyInAnyOrder(member.getId(), "nemo1234", "dragonbead95@naver.com", "profileUrl");
		assertThat(response)
			.extracting("user.notificationPreferences")
			.extracting("browserNotify", "targetGainNotify", "maxLossNotify", "targetPriceNotify")
			.containsExactlyInAnyOrder(true, true, true, true);
	}

	@DisplayName("사용자는 계정을 삭제한다")
	@Test
	void deleteMember() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		stockDividendRepository.saveAll(createStockDividendWith(stock));
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(3);
		Money purchasePricePerShare = Money.won(50000);
		String memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePricePerShare, memo, portfolioHolding));
		StockTargetPrice stockTargetPrice = stockTargetPriceRepository.save(createStockTargetPrice(member, stock));
		targetPriceNotificationRepository.save(createTargetPriceNotification(stockTargetPrice));
		WatchList watchList = watchListRepository.save(createWatchList(member));
		watchStockRepository.save(createWatchStock(watchList, stock));

		// when
		memberService.deleteMember(member.getId());

		// then
		assertThat(memberRepository.findById(member.getId())).isEmpty();
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
}
