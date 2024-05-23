package codesquad.fineants.domain.member.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import codesquad.fineants.domain.fcm_token.repository.FcmRepository;
import codesquad.fineants.domain.jwt.domain.Jwt;
import codesquad.fineants.domain.jwt.domain.JwtProvider;
import codesquad.fineants.domain.member.domain.dto.request.LoginRequest;
import codesquad.fineants.domain.member.domain.dto.request.ModifyPasswordRequest;
import codesquad.fineants.domain.member.domain.dto.request.OauthMemberRefreshRequest;
import codesquad.fineants.domain.member.domain.dto.request.ProfileChangeServiceRequest;
import codesquad.fineants.domain.member.domain.dto.request.SignUpServiceRequest;
import codesquad.fineants.domain.member.domain.dto.request.VerifyCodeRequest;
import codesquad.fineants.domain.member.domain.dto.request.VerifyEmailRequest;
import codesquad.fineants.domain.member.domain.dto.response.LoginResponse;
import codesquad.fineants.domain.member.domain.dto.response.OauthMemberRefreshResponse;
import codesquad.fineants.domain.member.domain.dto.response.ProfileChangeResponse;
import codesquad.fineants.domain.member.domain.dto.response.ProfileResponse;
import codesquad.fineants.domain.member.domain.dto.response.SignUpServiceResponse;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.notification.repository.NotificationRepository;
import codesquad.fineants.domain.notification_preference.domain.entity.NotificationPreference;
import codesquad.fineants.domain.notification_preference.repository.NotificationPreferenceRepository;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.repository.PortfolioRepository;
import codesquad.fineants.domain.portfolio_gain_history.domain.entity.PortfolioGainHistory;
import codesquad.fineants.domain.portfolio_gain_history.repository.PortfolioGainHistoryRepository;
import codesquad.fineants.domain.portfolio_holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.portfolio_holding.repository.PortfolioHoldingRepository;
import codesquad.fineants.domain.purchase_history.repository.PurchaseHistoryRepository;
import codesquad.fineants.domain.stock_target_price.domain.entity.StockTargetPrice;
import codesquad.fineants.domain.stock_target_price.repository.StockTargetPriceRepository;
import codesquad.fineants.domain.stock_target_price.repository.TargetPriceNotificationRepository;
import codesquad.fineants.domain.watch_list.domain.entity.WatchList;
import codesquad.fineants.domain.watch_list.domain.entity.WatchStock;
import codesquad.fineants.domain.watch_list.repository.WatchListRepository;
import codesquad.fineants.domain.watch_list.repository.WatchStockRepository;
import codesquad.fineants.global.errors.errorcode.MemberErrorCode;
import codesquad.fineants.global.errors.errorcode.NotificationPreferenceErrorCode;
import codesquad.fineants.global.errors.exception.BadRequestException;
import codesquad.fineants.global.errors.exception.FineAntsException;
import codesquad.fineants.global.errors.exception.NotFoundResourceException;
import codesquad.fineants.global.security.auth.dto.Token;
import codesquad.fineants.global.security.auth.service.TokenService;
import codesquad.fineants.infra.mail.service.MailService;
import codesquad.fineants.infra.s3.service.AmazonS3Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {

	private static final String LOCAL_PROVIDER = "local";
	public static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9]{2,10}$");
	public static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
	private final MemberRepository memberRepository;
	private final JwtProvider jwtProvider;
	private final OauthMemberRedisService redisService;
	private final MailService mailService;
	private final AmazonS3Service amazonS3Service;
	private final PasswordEncoder passwordEncoder;
	private final WatchListRepository watchListRepository;
	private final WatchStockRepository watchStockRepository;
	private final PortfolioHoldingRepository portfolioHoldingRepository;
	private final PortfolioRepository portfolioRepository;
	private final PortfolioGainHistoryRepository portfolioGainHistoryRepository;
	private final PurchaseHistoryRepository purchaseHistoryRepository;
	private final VerifyCodeGenerator verifyCodeGenerator;
	private final MemberNotificationPreferenceService preferenceService;
	private final NotificationPreferenceRepository notificationPreferenceRepository;
	private final NotificationRepository notificationRepository;
	private final FcmRepository fcmRepository;
	private final StockTargetPriceRepository stockTargetPriceRepository;
	private final TargetPriceNotificationRepository targetPriceNotificationRepository;
	private final TokenService tokenService;

	public void logout(HttpServletRequest request, HttpServletResponse response) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			new SecurityContextLogoutHandler().logout(request, response, authentication);
		}
	}

	@Transactional
	public OauthMemberRefreshResponse refreshAccessToken(OauthMemberRefreshRequest request, LocalDateTime now) {
		String refreshToken = request.getRefreshToken();

		Token token = tokenService.refreshToken(refreshToken, now);
		return OauthMemberRefreshResponse.from(token);
	}

	@Transactional
	public SignUpServiceResponse signup(SignUpServiceRequest request) {
		verifyEmail(request.getEmail());
		verifyNickname(request.getNickname());
		verifyPassword(request.getPassword(), request.getPasswordConfirm());

		// 프로필 이미지 파일 S3에 업로드
		String profileUrl = null;
		if (request.getProfileImageFile() != null && !request.getProfileImageFile().isEmpty()) {
			profileUrl = uploadProfileImageFile(request.getProfileImageFile());
		}

		// 비밀번호 암호화
		String encryptedPassword = passwordEncoder.encode(request.getPassword());
		// 회원 데이터베이스 저장
		Member member = memberRepository.save(request.toEntity(profileUrl, encryptedPassword));
		preferenceService.registerDefaultNotificationPreference(member);

		log.info("일반 회원가입 결과 : {}", member);
		return SignUpServiceResponse.from(member);
	}

	private String uploadProfileImageFile(MultipartFile profileImageFile) {
		return Optional.ofNullable(profileImageFile)
			.map(amazonS3Service::upload)
			.orElse(null);
	}

	private void verifyEmail(String email) {
		if (memberRepository.existsMemberByEmailAndProvider(email, LOCAL_PROVIDER)) {
			throw new BadRequestException(MemberErrorCode.REDUNDANT_EMAIL);
		}
	}

	private void verifyNickname(String nickname) {
		if (memberRepository.existsByNickname(nickname)) {
			throw new FineAntsException(MemberErrorCode.REDUNDANT_NICKNAME);
		}
	}

	// memberId을 제외한 다른 nickname이 존재하는지 검증
	private void verifyNickname(String nickname, Long memberId) {
		if (memberRepository.findMemberByNicknameAndNotMemberId(nickname, memberId).isPresent()) {
			throw new FineAntsException(MemberErrorCode.REDUNDANT_NICKNAME);
		}
	}

	private void verifyPassword(String password, String passwordConfirm) {
		if (!password.equals(passwordConfirm)) {
			throw new BadRequestException(MemberErrorCode.PASSWORD_CHECK_FAIL);
		}
	}

	@Transactional(readOnly = true)
	public void sendVerifyCode(VerifyEmailRequest request) {
		String email = request.getEmail();
		String verifyCode = verifyCodeGenerator.generate();

		// Redis에 생성한 검증 코드 임시 저장
		redisService.saveEmailVerifCode(email, verifyCode);

		try {
			// 사용자에게 검증 코드 메일 전송
			mailService.sendEmail(email,
				"Finants 회원가입 인증 코드",
				String.format("인증코드를 회원가입 페이지에 입력해주세요: %s", verifyCode));
		} catch (Exception e) {
			throw new BadRequestException(MemberErrorCode.SEND_EMAIL_VERIFY_CODE_FAIL);
		}
	}

	@Transactional
	public void checkNickname(String nickname) {
		if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
			throw new BadRequestException(MemberErrorCode.BAD_SIGNUP_INPUT);
		}
		if (memberRepository.existsByNickname(nickname)) {
			throw new BadRequestException(MemberErrorCode.REDUNDANT_NICKNAME);
		}

	}

	@Transactional
	public void checkEmail(String email) {
		if (!EMAIL_PATTERN.matcher(email).matches()) {
			throw new BadRequestException(MemberErrorCode.BAD_SIGNUP_INPUT);
		}
		if (memberRepository.existsMemberByEmailAndProvider(email, LOCAL_PROVIDER)) {
			throw new BadRequestException(MemberErrorCode.REDUNDANT_EMAIL);
		}
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		Member member = memberRepository.findMemberByEmailAndProvider(request.getEmail(), LOCAL_PROVIDER)
			.orElseThrow(() -> new BadRequestException(MemberErrorCode.LOGIN_FAIL));
		if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
			throw new BadRequestException(MemberErrorCode.LOGIN_FAIL);
		}
		if (request.getPassword().isBlank()) {
			throw new BadRequestException(MemberErrorCode.LOGIN_FAIL);
		}
		Jwt jwt = jwtProvider.createJwtBasedOnMember(member, LocalDateTime.now());
		return LoginResponse.from(jwt);
	}

	@Transactional
	@Secured("ROLE_USER")
	public ProfileChangeResponse changeProfile(ProfileChangeServiceRequest request) {
		Member member = findMemberById(request.getMemberId());
		MultipartFile profileImageFile = request.getProfileImageFile();
		String profileUrl = null;

		// 변경할 정보가 없는 경우
		if (profileImageFile == null && request.getNickname().isBlank()) {
			throw new FineAntsException(MemberErrorCode.NO_PROFILE_CHANGES);
		}

		// 기존 프로필 파일 유지
		if (profileImageFile == null) {
			profileUrl = member.getProfileUrl();
		}
		// 기본 프로필 파일로 변경인 경우
		else if (profileImageFile.isEmpty()) {
			// 회원의 기존 프로필 사진 제거
			// 기존 프로필 파일 삭제
			if (member.getProfileUrl() != null) {
				amazonS3Service.deleteFile(member.getProfileUrl());
			}
		}
		// 새로운 프로필 파일로 변경인 경우
		else if (!profileImageFile.isEmpty()) {
			// 기존 프로필 파일 삭제
			if (member.getProfileUrl() != null) {
				amazonS3Service.deleteFile(member.getProfileUrl());
			}

			// 새로운 프로필 파일 저장
			profileUrl = amazonS3Service.upload(profileImageFile);
		}
		member.updateProfileUrl(profileUrl);

		if (!request.getNickname().isBlank()) {
			String nickname = request.getNickname();
			verifyNickname(nickname, member.getId());
			member.updateNickname(nickname);
		}
		return ProfileChangeResponse.from(member);
	}

	private Member findMemberById(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new FineAntsException(MemberErrorCode.NOT_FOUND_MEMBER));
	}

	@Transactional
	@Secured("ROLE_USER")
	public void modifyPassword(ModifyPasswordRequest request, Long memberId) {
		Member member = findMember(memberId);
		if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
			throw new BadRequestException(MemberErrorCode.PASSWORD_CHECK_FAIL);
		}
		if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
			throw new BadRequestException(MemberErrorCode.NEW_PASSWORD_CONFIRM_FAIL);
		}
		String newEncodedPassword = passwordEncoder.encode(request.getNewPassword());
		member.updatePassword(newEncodedPassword);
		int count = memberRepository.modifyMemberPassword(member.getPassword(), member.getId());
		log.info("회원 비밀번호 변경 결과 : {}", count);
	}

	private Member findMember(Long id) {
		return memberRepository.findById(id)
			.orElseThrow(() -> new BadRequestException(MemberErrorCode.NOT_FOUND_MEMBER));
	}

	@Transactional(readOnly = true)
	public void checkVerifyCode(VerifyCodeRequest request) {
		Optional<String> verifyCode = redisService.get(request.getEmail());

		if (verifyCode.isEmpty() || !verifyCode.get().equals(request.getCode())) {
			throw new BadRequestException(MemberErrorCode.VERIFICATION_CODE_CHECK_FAIL);
		}
	}

	@Transactional
	@Secured("ROLE_USER")
	public void deleteMember(Long memberId) {
		Member member = findMember(memberId);
		List<Portfolio> portfolios = portfolioRepository.findAllByMemberId(memberId);
		List<PortfolioHolding> portfolioHoldings = new ArrayList<>();
		portfolios.forEach(
			portfolio -> portfolioHoldings.addAll(portfolioHoldingRepository.findAllByPortfolio(portfolio)));
		List<PortfolioGainHistory> portfolioGainHistories = new ArrayList<>();
		portfolios.forEach(portfolio -> portfolioGainHistories.addAll(
			portfolioGainHistoryRepository.findAllByPortfolioId(portfolio.getId())));
		purchaseHistoryRepository.deleteAllByPortfolioHoldingIdIn(
			portfolioHoldings.stream().map(PortfolioHolding::getId).collect(
				Collectors.toList()));
		portfolioGainHistoryRepository.deleteAll(portfolioGainHistories);
		portfolioHoldingRepository.deleteAll(portfolioHoldings);
		portfolioRepository.deleteAll(portfolios);
		List<WatchList> watchList = watchListRepository.findByMember(member);
		List<WatchStock> watchStocks = new ArrayList<>();
		watchList.forEach(w -> watchStocks.addAll(watchStockRepository.findByWatchList(w)));
		watchStockRepository.deleteAll(watchStocks);
		watchListRepository.deleteAll(watchList);
		fcmRepository.deleteAllByMemberId(member.getId());
		List<StockTargetPrice> stockTargetPrices = stockTargetPriceRepository.findAllByMemberId(member.getId());
		targetPriceNotificationRepository.deleteAllByStockTargetPrices(stockTargetPrices);
		stockTargetPriceRepository.deleteAllByMemberId(member.getId());
		notificationRepository.deleteAllByMemberId(member.getId());
		notificationPreferenceRepository.deleteAllByMemberId(member.getId());
		memberRepository.delete(member);
	}

	@Transactional(readOnly = true)
	@Secured("ROLE_USER")
	public ProfileResponse readProfile(Long memberId) {
		Member member = findMember(memberId);
		NotificationPreference preference = notificationPreferenceRepository.findByMemberId(member.getId())
			.orElseThrow(
				() -> new NotFoundResourceException(NotificationPreferenceErrorCode.NOT_FOUND_NOTIFICATION_PREFERENCE));
		return ProfileResponse.from(member, ProfileResponse.NotificationPreference.from(preference));
	}
}
