package codesquad.fineants.spring.api.member.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.jwt.Jwt;
import codesquad.fineants.domain.jwt.JwtProvider;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.client.AuthorizationCodeRandomGenerator;
import codesquad.fineants.domain.oauth.client.DecodedIdTokenPayload;
import codesquad.fineants.domain.oauth.client.OauthClient;
import codesquad.fineants.domain.oauth.repository.OauthClientRepository;
import codesquad.fineants.spring.api.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.OauthErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.errors.exception.FineAntsException;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.member.request.AuthorizationRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberLogoutRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberRefreshRequest;
import codesquad.fineants.spring.api.member.response.OauthAccessTokenResponse;
import codesquad.fineants.spring.api.member.response.OauthCreateUrlResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberLoginResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberRefreshResponse;
import codesquad.fineants.spring.api.member.response.OauthUserProfileResponse;
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
	private final AuthorizationCodeRandomGenerator authorizationCodeRandomGenerator;

	public OauthMemberLoginResponse login(String provider, String code, String state, LocalDateTime now) {
		AuthorizationRequest request = getCodeVerifier(state);
		OauthUserProfileResponse profileResponse = getOauthUserProfileResponse(provider, code, request, now);
		Optional<Member> optionalMember = getLoginMember(provider, profileResponse);
		Member member = optionalMember.orElseGet(() ->
			Member.builder()
				.email(profileResponse.getEmail())
				.nickname(generateRandomNickname())
				.provider(provider)
				.profileUrl(profileResponse.getProfileImage())
				.build());
		Member saveMember = memberRepository.save(member);
		Jwt jwt = jwtProvider.createJwtBasedOnMember(saveMember, now);
		redisService.saveRefreshToken(saveMember.createRedisKey(), jwt);
		return OauthMemberLoginResponse.of(jwt, saveMember);
	}

	private AuthorizationRequest getCodeVerifier(String state) {
		AuthorizationRequest request = authorizationRequestMap.remove(state);
		if (request == null) {
			throw new BadRequestException(OauthErrorCode.WRONG_STATE);
		}
		return request;
	}

	private OauthUserProfileResponse getOauthUserProfileResponse(String provider, String authorizationCode,
		AuthorizationRequest authorizationRequest, LocalDateTime now) {
		OauthClient oauthClient = oauthClientRepository.findOneBy(provider);
		OauthAccessTokenResponse accessTokenResponse = webClientWrapper.post(
			oauthClient.getTokenUri(),
			oauthClient.createTokenHeader(),
			oauthClient.createTokenBody(authorizationCode, authorizationRequest.getCodeVerifier()),
			OauthAccessTokenResponse.class);

		DecodedIdTokenPayload payload = oauthClient.decodeIdToken(
			accessTokenResponse.getIdToken(),
			authorizationRequest.getNonce(),
			now);
		return OauthUserProfileResponse.from(payload);
	}

	private Optional<Member> getLoginMember(String provider, OauthUserProfileResponse userProfileResponse) {
		return memberRepository.findMemberByEmailAndProvider(userProfileResponse.getEmail(), provider);
	}

	private String generateRandomNickname() {
		String randomPart = UUID.randomUUID().toString()
			.replaceAll("-", "")
			.substring(0, 8);
		return "일개미" + randomPart;
	}

	public void logout(String accessToken, OauthMemberLogoutRequest request) {
		String refreshToken = request.getRefreshToken();
		deleteRefreshTokenBy(refreshToken);
		banAccessToken(accessToken);
	}

	private void deleteRefreshTokenBy(String refreshToken) {
		String email;
		try {
			email = redisService.findEmailBy(refreshToken);
			log.debug("리프레시 토큰 값에 따른 이메일 조회 결과 : email={}", email);
		} catch (FineAntsException e) {
			log.error("리프레시 토큰에 따른 이메일 없음 : {}", e.toString());
			return;
		}

		boolean result = redisService.deleteRefreshToken(String.format("RT:%s", email));
		log.debug("리프레쉬 토큰 삭제 결과 : {}", result);
	}

	private void banAccessToken(String accessToken) {
		long expiration;
		try {
			expiration = ((Integer)jwtProvider.getClaims(accessToken).get("exp")).longValue();
		} catch (FineAntsException e) {
			log.error("토큰 에러 : {}", accessToken);
			log.error("액세스 토큰 밴 에러 : {}", e.toString());
			return;
		}
		redisService.banAccessToken(accessToken, expiration);

	}

	public OauthMemberRefreshResponse refreshAccessToken(OauthMemberRefreshRequest request, LocalDateTime now) {
		String refreshToken = request.getRefreshToken();

		jwtProvider.validateToken(refreshToken);
		log.debug("refreshToken is valid token : {}", refreshToken);

		String email = redisService.findEmailBy(refreshToken);
		log.debug("findEmailByRefreshToken 결과 : email={}", email);
		Member member = memberRepository.findMemberByEmail(email)
			.orElseThrow(() -> new NotFoundResourceException(MemberErrorCode.NOT_FOUND_MEMBER));
		log.debug("findMemberByEmail 결과 : member={}", member);

		Jwt jwt = jwtProvider.createJwtWithRefreshTokenBasedOnMember(member, refreshToken, now);

		return OauthMemberRefreshResponse.from(jwt);
	}

	@Transactional(readOnly = true)
	public OauthCreateUrlResponse createAuthorizationCodeURL(String provider) {
		OauthClient oauthClient = oauthClientRepository.findOneBy(provider);
		AuthorizationRequest request = authorizationCodeRandomGenerator.generateAuthorizationRequest();
		authorizationRequestMap.put(request.getState(), request);
		return new OauthCreateUrlResponse(
			oauthClient.createAuthURL(request),
			request);
	}
}
