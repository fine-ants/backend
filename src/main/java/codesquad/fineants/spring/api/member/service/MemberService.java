package codesquad.fineants.spring.api.member.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

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
import codesquad.fineants.spring.api.S3.service.AmazonS3Service;
import codesquad.fineants.spring.api.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.OauthErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.errors.exception.FineAntsException;
import codesquad.fineants.spring.api.member.manager.AuthorizationRequestManager;
import codesquad.fineants.spring.api.member.request.AuthorizationRequest;
import codesquad.fineants.spring.api.member.request.LoginRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberLoginRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberLogoutRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberRefreshRequest;
import codesquad.fineants.spring.api.member.request.SignUpRequest;
import codesquad.fineants.spring.api.member.request.VerifCodeRequest;
import codesquad.fineants.spring.api.member.request.VerifyEmailRequest;
import codesquad.fineants.spring.api.member.response.LoginResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberLoginResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberRefreshResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberResponse;
import codesquad.fineants.spring.api.member.response.OauthSaveUrlResponse;
import codesquad.fineants.spring.api.member.response.OauthUserProfile;
import codesquad.fineants.spring.api.portfolio_notification.MailService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class MemberService {
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
		if (memberRepository.existsByEmail(request.getEmail())) {
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
			.provider("local")
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
		if (memberRepository.existsByEmail(email)) {
			throw new BadRequestException(MemberErrorCode.REDUNDANT_EMAIL);
		}
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		Member member = memberRepository.findMemberByEmailAndProvider(request.getEmail(), "local")
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
	public void changeProfileImage(MultipartFile profileImageFile, AuthMember authMember) {
		Member member = memberRepository.findById(authMember.getMemberId())
			.orElseThrow(() -> new BadRequestException(MemberErrorCode.NOT_FOUND_MEMBER));
		String url = null;
		if (profileImageFile != null) {
			url = amazonS3Service.upload(profileImageFile);
		}
		member.updateProfileImage(url);
	}

	public void checkVerifCode(VerifCodeRequest request) {
		String verifCode = redisService.get(request.getEmail());
		if (verifCode == null || !verifCode.equals(request.getCode())) {
			throw new BadRequestException(MemberErrorCode.VERIFICATION_CODE_CHECK_FAIL);
		}
	}
}
