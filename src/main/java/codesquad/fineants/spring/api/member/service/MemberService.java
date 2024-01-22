package codesquad.fineants.spring.api.member.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
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

import codesquad.fineants.domain.jwt.Jwt;
import codesquad.fineants.domain.jwt.JwtProvider;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
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
import codesquad.fineants.domain.watch_list.WatchList;
import codesquad.fineants.domain.watch_list.WatchListRepository;
import codesquad.fineants.domain.watch_stock.WatchStock;
import codesquad.fineants.domain.watch_stock.WatchStockRepository;
import codesquad.fineants.spring.api.S3.service.AmazonS3Service;
import codesquad.fineants.spring.api.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.OauthErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.errors.exception.FineAntsException;
import codesquad.fineants.spring.api.member.manager.AuthorizationRequestManager;
import codesquad.fineants.spring.api.member.request.AuthorizationRequest;
import codesquad.fineants.spring.api.member.request.LoginRequest;
import codesquad.fineants.spring.api.member.request.ModifyPasswordRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberLoginRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberLogoutRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberRefreshRequest;
import codesquad.fineants.spring.api.member.request.ProfileChangeRequest;
import codesquad.fineants.spring.api.member.request.SignUpRequest;
import codesquad.fineants.spring.api.member.request.VerifCodeRequest;
import codesquad.fineants.spring.api.member.request.VerifyEmailRequest;
import codesquad.fineants.spring.api.member.response.LoginResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberLoginResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberRefreshResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberResponse;
import codesquad.fineants.spring.api.member.response.OauthSaveUrlResponse;
import codesquad.fineants.spring.api.member.response.OauthUserProfile;
import codesquad.fineants.spring.api.member.response.ProfileChangeResponse;
import codesquad.fineants.spring.api.portfolio_notification.MailService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class MemberService {

	private static final String LOCAL_PROVIDER = "local";
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

	public OauthMemberLoginResponse login(OauthMemberLoginRequest request) {
		log.info("로그인 서비스 요청 : loginRequest={}", request);
		String provider = request.getProvider();
		CompletableFuture<OauthMemberLoginResponse> future = CompletableFuture
			.supplyAsync(supplyOauthClient(provider))
			.thenApply(fetchProfile(request))
			.thenCompose(saveMember(provider))
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
	public void signup(MultipartFile imageFile, SignUpRequest request) {
		checkForm(request);
		if (memberRepository.existsMemberByEmailAndProvider(request.getEmail(), LOCAL_PROVIDER)) {
			throw new BadRequestException(MemberErrorCode.REDUNDANT_EMAIL);
		}
		if (memberRepository.existsByNickname(request.getNickname())) {
			throw new BadRequestException(MemberErrorCode.REDUNDANT_NICKNAME);
		}
		if (!request.getPassword().equals(request.getPasswordConfirm())) {
			throw new BadRequestException(MemberErrorCode.PASSWORD_CHECK_FAIL);
		}
		String url = null;
		if (imageFile != null) {
			url = amazonS3Service.upload(imageFile);
		}
		Member member = Member.builder()
			.email(request.getEmail())
			.nickname(request.getNickname())
			.profileUrl(url)
			.password(passwordEncoder.encode(request.getPassword()))
			.provider(LOCAL_PROVIDER)
			.build();
		saveMemberToRepository(member);
	}

	private void checkForm(SignUpRequest request) {
		if (!isValidNickname(request.getNickname())) {
			throw new BadRequestException(MemberErrorCode.BAD_SIGNUP_INPUT);
		}
		if (!isValidEmail(request.getEmail())) {
			throw new BadRequestException(MemberErrorCode.BAD_SIGNUP_INPUT);
		}
		if (!isValidPassword(request.getPassword())) {
			throw new BadRequestException(MemberErrorCode.BAD_SIGNUP_INPUT);
		}
	}

	private boolean isValidNickname(String nickname) {
		return nickname != null && nickname.matches("^[가-힣a-zA-Z0-9]{2,10}$");
	}

	private boolean isValidEmail(String email) {
		return email != null && email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
	}

	private boolean isValidPassword(String password) {
		return password != null && password.matches("^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,16}$");
	}

	@Transactional
	public void sendEmailVerif(VerifyEmailRequest request) {
		Random random = new Random();
		int code = random.nextInt(1000000); // Generates a number between 0 and 999999
		String verifCode = String.format("%06d", code);
		redisService.saveEmailVerifCode(request.getEmail(), verifCode);
		try {
			mailService.sendEmail(request.getEmail(),
				"Finants 회원가입 인증 코드",
				String.format("인증코드를 회원가입 페이지에 입력해주세요: %s", verifCode));
		} catch (Exception e) {
			throw new BadRequestException(MemberErrorCode.SEND_EMAIL_VERIF_FAIL);
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
	public ProfileChangeResponse changeProfile(MultipartFile profileImageFile, AuthMember authMember,
		ProfileChangeRequest request) {
		Member member = memberRepository.findById(authMember.getMemberId())
			.orElseThrow(() -> new BadRequestException(MemberErrorCode.NOT_FOUND_MEMBER));
		if (profileImageFile != null) {
			String url = amazonS3Service.upload(profileImageFile);
			member.updateProfileImage(url);
		}
		if (!isValidNickname(request.getNickname())) {
			throw new BadRequestException(MemberErrorCode.BAD_SIGNUP_INPUT);
		}
		if (memberRepository.existsByNickname(request.getNickname())) {
			throw new BadRequestException(MemberErrorCode.REDUNDANT_NICKNAME);
		}
		member.updateNickname(request.getNickname());
		int count = memberRepository.updateMember(member.getNickname(), member.getProfileUrl(), member.getId());
		log.info("회원 프로필 정보 변경 개수 : {}", count);
		return ProfileChangeResponse.from(member);
	}

	@Transactional(readOnly = true)
	public void modifyPassword(ModifyPasswordRequest request, AuthMember authMember) {
		Member member = memberRepository.findById(authMember.getMemberId())
			.orElseThrow(() -> new BadRequestException(MemberErrorCode.NOT_FOUND_MEMBER));
		if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
			throw new BadRequestException(MemberErrorCode.PASSWORD_CHECK_FAIL);
		}
		if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
			throw new BadRequestException(MemberErrorCode.NEW_PASSWORD_CONFIRM_FAIL);
		}
		String newEncodedPassword = passwordEncoder.encode(request.getNewPassword());
		member.updatePassword(newEncodedPassword);
	}

	public void checkVerifCode(VerifCodeRequest request) {
		String verifCode = redisService.get(request.getEmail());
		if (verifCode == null || !verifCode.equals(request.getCode())) {
			throw new BadRequestException(MemberErrorCode.VERIFICATION_CODE_CHECK_FAIL);
		}
	}

	@Transactional
	public void deleteMember(AuthMember authMember) {
		Member member = memberRepository.findById(authMember.getMemberId())
			.orElseThrow(() -> new BadRequestException(MemberErrorCode.NOT_FOUND_MEMBER));
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
		memberRepository.delete(member);
	}
}
