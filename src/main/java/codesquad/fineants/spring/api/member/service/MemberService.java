package codesquad.fineants.spring.api.member.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import codesquad.fineants.domain.jwt.Jwt;
import codesquad.fineants.domain.jwt.JwtProvider;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.client.AuthorizationCodeRandomGenerator;
import codesquad.fineants.domain.oauth.client.DecodedIdTokenPayload;
import codesquad.fineants.domain.oauth.client.OauthClient;
import codesquad.fineants.domain.oauth.repository.OauthClientRepository;
import codesquad.fineants.spring.api.S3.service.AmazonS3Service;
import codesquad.fineants.spring.api.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.OauthErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.errors.exception.FineAntsException;
import codesquad.fineants.spring.api.member.request.AuthorizationRequest;
import codesquad.fineants.spring.api.member.request.LoginRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberLogoutRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberRefreshRequest;
import codesquad.fineants.spring.api.member.request.SignUpRequest;
import codesquad.fineants.spring.api.member.request.VerifyEmailRequest;
import codesquad.fineants.spring.api.member.response.LoginResponse;
import codesquad.fineants.spring.api.member.response.OauthAccessTokenResponse;
import codesquad.fineants.spring.api.member.response.OauthCreateUrlResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberLoginResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberRefreshResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberResponse;
import codesquad.fineants.spring.api.member.response.OauthUserProfileResponse;
import codesquad.fineants.spring.api.portfolio_notification.MailService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class MemberService {
	private static final Map<String, AuthorizationRequest> authorizationRequestMap = new ConcurrentHashMap<>();
	private final OauthClientRepository oauthClientRepository;
	private final MemberRepository memberRepository;
	private final JwtProvider jwtProvider;
	private final OauthMemberRedisService redisService;
	private final WebClientWrapper webClientWrapper;
	private final MailService mailService;
	private final AmazonS3Service amazonS3Service;
	private final AuthorizationCodeRandomGenerator authorizationCodeRandomGenerator;

	public OauthMemberLoginResponse login(String provider, String code, String redirectUrl, String state,
		LocalDateTime now) {
		log.info("로그인 서비스 요청 : provider = {}, code = {}, redirectUrl = {}, state = {}", provider, code, redirectUrl,
			state);
		AuthorizationRequest request = getCodeVerifier(state);
		OauthUserProfileResponse profileResponse = getOauthUserProfileResponse(provider, code, redirectUrl, request,
			now, state);
		Optional<Member> optionalMember = getLoginMember(provider, profileResponse);
		Member member = optionalMember.orElseGet(() -> Member.builder()
			.email(profileResponse.getEmail())
			.nickname(generateRandomNickname())
			.provider(provider)
			.profileUrl(profileResponse.getProfileImage())
			.build());
		Member saveMember = memberRepository.save(member);
		Jwt jwt = jwtProvider.createJwtBasedOnMember(saveMember, now);
		return OauthMemberLoginResponse.of(jwt, saveMember);
	}

	private AuthorizationRequest getCodeVerifier(String state) {
		log.info("AuthorizationRequest 조회 : state={}", state);
		log.info("authorizationRequestMap : {}", authorizationRequestMap);
		AuthorizationRequest request = authorizationRequestMap.remove(state);
		if (request == null) {
			throw new BadRequestException(OauthErrorCode.WRONG_STATE);
		}
		return request;
	}

	private OauthUserProfileResponse getOauthUserProfileResponse(String provider, String authorizationCode,
		String redirectUrl, AuthorizationRequest authorizationRequest, LocalDateTime now, String state) {
		OauthClient oauthClient = oauthClientRepository.findOneBy(provider);
		OauthAccessTokenResponse accessTokenResponse = webClientWrapper.post(oauthClient.getTokenUri(),
			oauthClient.createTokenHeader(),
			oauthClient.createTokenBody(authorizationCode, redirectUrl, authorizationRequest.getCodeVerifier(), state),
			OauthAccessTokenResponse.class);

		if (oauthClient.isSupportOICD()) {
			DecodedIdTokenPayload payload = oauthClient.decodeIdToken(accessTokenResponse.getIdToken(),
				authorizationRequest.getNonce(), now);
			return OauthUserProfileResponse.from(payload);
		}
		LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
		header.add(HttpHeaders.AUTHORIZATION,
			String.format("%s %s", accessTokenResponse.getTokenType(), accessTokenResponse.getAccessToken()));
		Map<String, Object> attributes = webClientWrapper.get(oauthClient.getUserInfoUri(), header,
			new ParameterizedTypeReference<>() {
			});
		return oauthClient.createOauthUserProfileResponse(attributes);
	}

	private Optional<Member> getLoginMember(String provider, OauthUserProfileResponse userProfileResponse) {
		return memberRepository.findMemberByEmailAndProvider(userProfileResponse.getEmail(), provider);
	}

	private String generateRandomNickname() {
		String randomPart = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
		return "일개미" + randomPart;
	}

	public void logout(String accessToken, OauthMemberLogoutRequest request) {
		String refreshToken = request.getRefreshToken();
		banTokens(accessToken, refreshToken);
	}

	private void banTokens(String accessToken, String refreshToken) {
		long accessTokenExpiration;
		long refreshTokenExpiration;
		try {
			accessTokenExpiration = ((Integer)jwtProvider.getClaims(accessToken).get("exp")).longValue();
			refreshTokenExpiration = ((Integer)jwtProvider.getClaims(accessToken).get("exp")).longValue();
		} catch (FineAntsException e) {
			log.error("토큰 에러 : {}", accessToken);
			log.error("액세스 토큰 밴 에러 : {}", e.toString());
			return;
		}
		redisService.banToken(accessToken, accessTokenExpiration);
		redisService.banToken(refreshToken, refreshTokenExpiration);
	}

	public OauthMemberRefreshResponse refreshAccessToken(OauthMemberRefreshRequest request, LocalDateTime now) {
		String refreshToken = request.getRefreshToken();
		Claims claims = jwtProvider.getClaims(refreshToken);
		log.debug("refreshToken is valid token : {}", refreshToken);
		Jwt jwt = jwtProvider.createJwtWithRefreshToken(claims, refreshToken, now);
		return OauthMemberRefreshResponse.from(jwt);
	}

	@Transactional(readOnly = true)
	public OauthCreateUrlResponse createAuthorizationCodeURL(final String provider) {
		final OauthClient oauthClient = oauthClientRepository.findOneBy(provider);
		final AuthorizationRequest request = authorizationCodeRandomGenerator.generateAuthorizationRequest();
		authorizationRequestMap.put(request.getState(), request);
		return new OauthCreateUrlResponse(oauthClient.createAuthURL(request), request);
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
		String verifCode = redisService.get(request.getEmail());
		if (!verifCode.equals(request.getVerificationCode())) {
			throw new BadRequestException(MemberErrorCode.VERIFICATION_CODE_CHECK_FAIL);
		}
		String url = null;
		if (imageFile != null) {
			url = amazonS3Service.upload(imageFile);
		}
		Member member = Member.builder()
			.email(request.getEmail())
			.nickname(request.getNickname())
			.profileUrl(url)
			.password(request.getPassword())
			.build();
		memberRepository.save(member);
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

	public LoginResponse login(LoginRequest request) {
		Member member = memberRepository.findMemberByEmail(request.getEmail())
			.orElseThrow(() -> new BadRequestException(MemberErrorCode.LOGIN_FAIL));
		if (!member.getPassword().equals(request.getPassword())) {
			throw new BadRequestException(MemberErrorCode.LOGIN_FAIL);
		}
		Jwt jwt = jwtProvider.createJwtBasedOnMember(member, LocalDateTime.now());
		return LoginResponse.from(jwt, OauthMemberResponse.from(member));
	}
}
