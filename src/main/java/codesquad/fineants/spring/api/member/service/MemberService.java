package codesquad.fineants.spring.api.member.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import codesquad.fineants.domain.fcm_token.FcmRepository;
import codesquad.fineants.domain.jwt.Jwt;
import codesquad.fineants.domain.jwt.JwtProvider;
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
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistoryRepository;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.portfolio_holding.PortfolioHoldingRepository;
import codesquad.fineants.domain.purchase_history.PurchaseHistoryRepository;
import codesquad.fineants.domain.stock_target_price.StockTargetPrice;
import codesquad.fineants.domain.stock_target_price.StockTargetPriceRepository;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotificationRepository;
import codesquad.fineants.domain.watch_list.WatchList;
import codesquad.fineants.domain.watch_list.WatchListRepository;
import codesquad.fineants.domain.watch_stock.WatchStock;
import codesquad.fineants.domain.watch_stock.WatchStockRepository;
import codesquad.fineants.spring.api.S3.service.AmazonS3Service;
import codesquad.fineants.spring.api.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.NotificationPreferenceErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.OauthErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.errors.exception.FineAntsException;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.mail.MailService;
import codesquad.fineants.spring.api.member.manager.AuthorizationRequestManager;
import codesquad.fineants.spring.api.member.request.AuthorizationRequest;
import codesquad.fineants.spring.api.member.request.LoginRequest;
import codesquad.fineants.spring.api.member.request.ModifyPasswordRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberLoginRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberLogoutRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberRefreshRequest;
import codesquad.fineants.spring.api.member.request.ProfileChangeRequest;
import codesquad.fineants.spring.api.member.request.VerifyCodeRequest;
import codesquad.fineants.spring.api.member.request.VerifyEmailRequest;
import codesquad.fineants.spring.api.member.response.LoginResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberLoginResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberRefreshResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberResponse;
import codesquad.fineants.spring.api.member.response.OauthSaveUrlResponse;
import codesquad.fineants.spring.api.member.response.OauthUserProfile;
import codesquad.fineants.spring.api.member.response.ProfileChangeResponse;
import codesquad.fineants.spring.api.member.response.ProfileResponse;
import codesquad.fineants.spring.api.member.service.request.ProfileChangeServiceRequest;
import codesquad.fineants.spring.api.member.service.request.SignUpServiceRequest;
import codesquad.fineants.spring.api.member.service.response.SignUpServiceResponse;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class MemberService {

	private static final String LOCAL_PROVIDER = "local";
	private static final String DEFAULT_PROFILE_NAME = "default.png";
	private static final String DEFAULT_PROFILE_URL = "https://fineants.s3.ap-northeast-2.amazonaws.com/profile/default/default.png";
	private final AuthorizationRequestManager authorizationRequestManager;
	private final OauthClientRepository oauthClientRepository;
	private final MemberRepository memberRepository;
	private final JwtProvider jwtProvider;
	private final OauthMemberRedisService redisService;
	private final MailService mailService;
	private final AmazonS3Service amazonS3Service;
	private final AuthorizationCodeRandomGenerator authorizationCodeRandomGenerator;
	private final MemberFactory memberFactory;
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

	public OauthMemberLoginResponse login(OauthMemberLoginRequest request) {
		log.info("로그인 서비스 요청 : loginRequest={}", request);
		String provider = request.getProvider();
		CompletableFuture<OauthMemberLoginResponse> future = CompletableFuture
			.supplyAsync(supplyOauthClient(provider))
			.thenApply(fetchProfile(request))
			.thenCompose(saveMember(provider))
			.thenCompose(registerNotificationPreference())
			.thenApply(createLoginResponse(request.getRequestTime()));

		try {
			return future.get(5L, TimeUnit.SECONDS);
		} catch (ExecutionException | InterruptedException | TimeoutException e) {
			throw new FineAntsException(OauthErrorCode.FAIL_LOGIN, e.getMessage());
		}
	}

	private Supplier<OauthClient> supplyOauthClient(String provider) {
		return () -> findOauthClient(provider);
	}

	private OauthClient findOauthClient(String provider) {
		return oauthClientRepository.findOneBy(provider);
	}

	private Function<OauthClient, OauthUserProfile> fetchProfile(
		OauthMemberLoginRequest request) {
		return oauthClient -> oauthClient.fetchProfile(request, popAuthorizationRequest(request.getState()));
	}

	private AuthorizationRequest popAuthorizationRequest(String state) {
		return authorizationRequestManager.pop(state);
	}

	private Function<OauthUserProfile, CompletionStage<Member>> saveMember(
		String provider) {
		return profile -> CompletableFuture
			.supplyAsync(supplyMember(provider, profile))
			.thenApplyAsync(this::saveMemberToRepository);
	}

	private Supplier<Member> supplyMember(String provider, OauthUserProfile profile) {
		return () -> findMember(profile.getEmail(), provider)
			.orElseGet(() -> memberFactory.createMember(profile));
	}

	private Function<Member, CompletionStage<Member>> registerNotificationPreference() {
		return member -> {
			saveDefaultNotificationPreference(member);
			return CompletableFuture.supplyAsync(() -> member);
		};
	}

	private void saveDefaultNotificationPreference(Member member) {
		preferenceService.registerDefaultNotificationPreference(member);
	}

	private Optional<Member> findMember(String email, String provider) {
		return memberRepository.findMemberByEmailAndProvider(email, provider);
	}

	private Member saveMemberToRepository(Member member) {
		return memberRepository.save(member);
	}

	private Function<Member, OauthMemberLoginResponse> createLoginResponse(
		LocalDateTime requestTime) {
		return member -> {
			Jwt jwt = createToken(member, requestTime);
			return OauthMemberLoginResponse.of(jwt, OauthMemberResponse.from(member));
		};
	}

	private Jwt createToken(Member member, LocalDateTime requestTime) {
		return jwtProvider.createJwtBasedOnMember(member, requestTime);
	}

	public void logout(String accessToken, OauthMemberLogoutRequest request) {
		String refreshToken = request.getRefreshToken();
		banTokens(accessToken, refreshToken);
	}

	private void banTokens(String accessToken, String refreshToken) {
		long accessTokenExpiration;
		long refreshTokenExpiration;
		try {
			accessTokenExpiration = ((Integer)jwtProvider.getAccessTokenClaims(accessToken).get("exp")).longValue();
			refreshTokenExpiration = ((Integer)jwtProvider.getRefreshTokenClaims(accessToken).get("exp")).longValue();
		} catch (FineAntsException e) {
			log.error(e.getMessage(), e);
			return;
		}
		redisService.banToken(accessToken, accessTokenExpiration);
		redisService.banToken(refreshToken, refreshTokenExpiration);
	}

	public OauthMemberRefreshResponse refreshAccessToken(OauthMemberRefreshRequest request, LocalDateTime now) {
		String refreshToken = request.getRefreshToken();
		Claims claims = jwtProvider.getRefreshTokenClaims(refreshToken);
		Jwt jwt = jwtProvider.createJwtWithRefreshToken(claims, refreshToken, now);
		return OauthMemberRefreshResponse.from(jwt);
	}

	@Transactional(readOnly = true)
	public OauthSaveUrlResponse saveAuthorizationCodeURL(final String provider) {
		final OauthClient oauthClient = oauthClientRepository.findOneBy(provider);
		final AuthorizationRequest request = authorizationCodeRandomGenerator.generateAuthorizationRequest();
		authorizationRequestManager.add(request.getState(), request);
		return new OauthSaveUrlResponse(oauthClient.createAuthURL(request), request);
	}

	@Transactional
	public SignUpServiceResponse signup(SignUpServiceRequest request) {
		verifyEmail(request.getEmail());
		verifyNickname(request.getNickname());
		verifyPassword(request.getPassword(), request.getPasswordConfirm());

		// 프로필 이미지 파일 S3에 업로드
		String profileUrl = DEFAULT_PROFILE_URL;
		if (!Objects.equals(request.getProfileImageFile().getOriginalFilename(), DEFAULT_PROFILE_NAME)) {
			profileUrl = uploadProfileImageFile(request.getProfileImageFile());
		}

		// 비밀번호 암호화
		String encryptedPassword = encryptPassword(request.getPassword());
		// 회원 데이터베이스 저장
		Member member = saveMemberToRepository(
			request.toEntity(
				profileUrl,
				encryptedPassword
			)
		);
		saveDefaultNotificationPreference(member);

		log.info("일반 회원가입 결과 : {}", member);
		return SignUpServiceResponse.from(member);
	}

	private String encryptPassword(String password) {
		return passwordEncoder.encode(password);
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
			throw new BadRequestException(MemberErrorCode.REDUNDANT_NICKNAME);
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

	public void checkNickname(String nickname) {
		if (memberRepository.existsByNickname(nickname)) {
			throw new BadRequestException(MemberErrorCode.REDUNDANT_NICKNAME);
		}
	}

	public void checkEmail(String email) {
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
		return LoginResponse.from(jwt, OauthMemberResponse.from(member));
	}

	@Transactional
	public ProfileChangeResponse changeProfile(ProfileChangeServiceRequest serviceRequest) {
		verifyNoProfileChanges(serviceRequest);
		Member member = findMemberById(serviceRequest.getMemberId());
		extractMemberProfileImage(serviceRequest)
			.map(amazonS3Service::upload)
			.ifPresent(member::updateProfileUrl);
		extractNickname(serviceRequest)
			.ifPresent(member::updateNickname);
		return ProfileChangeResponse.from(member);
	}

	private void verifyNoProfileChanges(ProfileChangeServiceRequest serviceRequest) {
		if (serviceRequest.getProfileImageFile().isEmpty() && serviceRequest.getRequest().isEmpty()) {
			throw new BadRequestException(MemberErrorCode.NO_PROFILE_CHANGES);
		}
	}

	private Member findMemberById(Optional<Long> optionalMemberId) {
		return optionalMemberId
			.flatMap(memberRepository::findById)
			.orElseThrow(() -> new BadRequestException(MemberErrorCode.NOT_FOUND_MEMBER));
	}

	private Optional<MultipartFile> extractMemberProfileImage(ProfileChangeServiceRequest serviceRequest) {
		return serviceRequest.getProfileImageFile();
	}

	private Optional<String> extractNickname(ProfileChangeServiceRequest serviceRequest) {
		return serviceRequest.getRequest()
			.map(ProfileChangeRequest::getNickname)
			.map(nickname -> {
				verifyNickname(nickname);
				return nickname;
			});
	}

	@Transactional
	public void modifyPassword(ModifyPasswordRequest request, AuthMember authMember) {
		Member member = findMember(authMember.getMemberId());
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
	public void deleteMember(AuthMember authMember) {
		Member member = findMember(authMember.getMemberId());
		List<Portfolio> portfolios = portfolioRepository.findAllByMemberId(authMember.getMemberId());
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
	public ProfileResponse readProfile(AuthMember authMember) {
		Member member = findMember(authMember.getMemberId());
		NotificationPreference preference = notificationPreferenceRepository.findByMemberId(member.getId())
			.orElseThrow(
				() -> new NotFoundResourceException(NotificationPreferenceErrorCode.NOT_FOUND_NOTIFICATION_PREFERENCE));
		return ProfileResponse.from(member, ProfileResponse.NotificationPreference.from(preference));
	}
}
