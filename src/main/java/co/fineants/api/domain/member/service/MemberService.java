package co.fineants.api.domain.member.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import co.fineants.api.domain.fcm.repository.FcmRepository;
import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.gainhistory.repository.PortfolioGainHistoryRepository;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.member.domain.dto.request.ModifyPasswordRequest;
import co.fineants.api.domain.member.domain.dto.request.OauthMemberRefreshRequest;
import co.fineants.api.domain.member.domain.dto.request.ProfileChangeServiceRequest;
import co.fineants.api.domain.member.domain.dto.request.SignUpServiceRequest;
import co.fineants.api.domain.member.domain.dto.request.VerifyCodeRequest;
import co.fineants.api.domain.member.domain.dto.request.VerifyEmailRequest;
import co.fineants.api.domain.member.domain.dto.response.OauthMemberRefreshResponse;
import co.fineants.api.domain.member.domain.dto.response.ProfileChangeResponse;
import co.fineants.api.domain.member.domain.dto.response.ProfileResponse;
import co.fineants.api.domain.member.domain.dto.response.SignUpServiceResponse;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.MemberRole;
import co.fineants.api.domain.member.domain.entity.Role;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.member.repository.RoleRepository;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import co.fineants.api.domain.notificationpreference.repository.NotificationPreferenceRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
import co.fineants.api.domain.stock_target_price.repository.StockTargetPriceRepository;
import co.fineants.api.domain.stock_target_price.repository.TargetPriceNotificationRepository;
import co.fineants.api.domain.watchlist.domain.entity.WatchList;
import co.fineants.api.domain.watchlist.domain.entity.WatchStock;
import co.fineants.api.domain.watchlist.repository.WatchListRepository;
import co.fineants.api.domain.watchlist.repository.WatchStockRepository;
import co.fineants.api.global.errors.errorcode.MemberErrorCode;
import co.fineants.api.global.errors.errorcode.NotificationPreferenceErrorCode;
import co.fineants.api.global.errors.errorcode.RoleErrorCode;
import co.fineants.api.global.errors.exception.BadRequestException;
import co.fineants.api.global.errors.exception.FineAntsException;
import co.fineants.api.global.errors.exception.NotFoundResourceException;
import co.fineants.api.global.security.oauth.dto.Token;
import co.fineants.api.global.security.oauth.service.TokenService;
import co.fineants.api.global.util.CookieUtils;
import co.fineants.api.infra.mail.service.MailService;
import co.fineants.api.infra.s3.service.AmazonS3Service;
import jakarta.annotation.security.PermitAll;
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
	private final NotificationPreferenceRepository notificationPreferenceRepository;
	private final NotificationRepository notificationRepository;
	private final FcmRepository fcmRepository;
	private final StockTargetPriceRepository stockTargetPriceRepository;
	private final TargetPriceNotificationRepository targetPriceNotificationRepository;
	private final TokenService tokenService;
	private final OauthMemberRedisService oauthMemberRedisService;
	private final RoleRepository roleRepository;

	public void logout(HttpServletRequest request, HttpServletResponse response) {
		// clear Authentication
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			new SecurityContextLogoutHandler().logout(request, response, authentication);
		}

		// ban accessToken
		String accessToken = CookieUtils.getAccessToken(request);
		if (accessToken != null) {
			oauthMemberRedisService.banAccessToken(accessToken);
		}

		// ban refreshToken
		String refreshToken = CookieUtils.getRefreshToken(request);
		if (refreshToken != null) {
			oauthMemberRedisService.banRefreshToken(refreshToken);
		}
	}

	@Transactional
	@PermitAll
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
		// 역할 추가
		Role userRole = roleRepository.findRoleByRoleName("ROLE_USER")
			.orElseThrow(() -> new FineAntsException(RoleErrorCode.NOT_EXIST_ROLE));
		Member member = request.toEntity(profileUrl, encryptedPassword);
		member.addMemberRole(MemberRole.create(member, userRole));
		member.setNotificationPreference(NotificationPreference.defaultSetting(member));
		// 회원 데이터베이스 저장
		Member saveMember = memberRepository.save(member);

		log.info("일반 회원가입 결과 : {}", saveMember);
		return SignUpServiceResponse.from(saveMember);
	}

	private String uploadProfileImageFile(MultipartFile profileImageFile) {
		return Optional.ofNullable(profileImageFile)
			.map(amazonS3Service::upload)
			.orElse(null);
	}

	private void verifyEmail(String email) {
		if (memberRepository.findMemberByEmailAndProvider(email, LOCAL_PROVIDER).isPresent()) {
			throw new BadRequestException(MemberErrorCode.REDUNDANT_EMAIL);
		}
	}

	private void verifyNickname(String nickname) {
		if (memberRepository.findMemberByNickname(nickname).isPresent()) {
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
	@PermitAll
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
	@PermitAll
	public void checkNickname(String nickname) {
		if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
			throw new BadRequestException(MemberErrorCode.BAD_SIGNUP_INPUT);
		}
		if (memberRepository.findMemberByNickname(nickname).isPresent()) {
			throw new BadRequestException(MemberErrorCode.REDUNDANT_NICKNAME);
		}

	}

	@Transactional
	@PermitAll
	public void checkEmail(String email) {
		if (!EMAIL_PATTERN.matcher(email).matches()) {
			throw new BadRequestException(MemberErrorCode.BAD_SIGNUP_INPUT);
		}
		if (memberRepository.findMemberByEmailAndProvider(email, LOCAL_PROVIDER).isPresent()) {
			throw new BadRequestException(MemberErrorCode.REDUNDANT_EMAIL);
		}
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
		} else if (profileImageFile.isEmpty()) { // 기본 프로필 파일로 변경인 경우
			// 회원의 기존 프로필 사진 제거
			// 기존 프로필 파일 삭제
			if (member.getProfileUrl() != null) {
				amazonS3Service.deleteFile(member.getProfileUrl());
			}
		} else if (!profileImageFile.isEmpty()) { // 새로운 프로필 파일로 변경인 경우
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
			member.changeNickname(nickname);
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
		member.changePassword(newEncodedPassword);
		int count = memberRepository.modifyMemberPassword(member.getPassword(), member.getId());
		log.info("회원 비밀번호 변경 결과 : {}", count);
	}

	private Member findMember(Long id) {
		return memberRepository.findById(id)
			.orElseThrow(() -> new BadRequestException(MemberErrorCode.NOT_FOUND_MEMBER));
	}

	@Transactional(readOnly = true)
	@PermitAll
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
			portfolioHoldings.stream().map(PortfolioHolding::getId).toList());
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
