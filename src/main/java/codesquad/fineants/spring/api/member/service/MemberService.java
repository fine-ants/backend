package codesquad.fineants.spring.api.member.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;

import codesquad.fineants.domain.jwt.Jwt;
import codesquad.fineants.domain.jwt.JwtProvider;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.client.AuthorizationCodeRandomGenerator;
import codesquad.fineants.domain.oauth.client.DecodedIdTokenPayload;
import codesquad.fineants.domain.oauth.client.OauthClient;
import codesquad.fineants.domain.oauth.repository.OauthClientRepository;
import codesquad.fineants.spring.api.errors.errorcode.OauthErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.errors.exception.FineAntsException;
import codesquad.fineants.spring.api.member.request.AuthorizationRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberLogoutRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberRefreshRequest;
import codesquad.fineants.spring.api.member.response.OauthAccessTokenResponse;
import codesquad.fineants.spring.api.member.response.OauthCreateUrlResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberLoginResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberRefreshResponse;
import codesquad.fineants.spring.api.member.response.OauthUserProfileResponse;
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
	private final AuthorizationCodeRandomGenerator authorizationCodeRandomGenerator;

	public OauthMemberLoginResponse login(String provider, String code, String redirectUrl, String state,
		LocalDateTime now) {
		log.info("로그인 서비스 요청 : provider = {}, code = {}, redirectUrl = {}, state = {}", provider, code, redirectUrl,
			state);
		AuthorizationRequest request = getCodeVerifier(state);
		OauthUserProfileResponse profileResponse = getOauthUserProfileResponse(provider, code, redirectUrl, request,
			now, state);
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
		OauthAccessTokenResponse accessTokenResponse = webClientWrapper.post(
			oauthClient.getTokenUri(),
			oauthClient.createTokenHeader(),
			oauthClient.createTokenBody(authorizationCode, redirectUrl, authorizationRequest.getCodeVerifier(), state),
			OauthAccessTokenResponse.class);

		if (oauthClient.isSupportOICD()) {
			DecodedIdTokenPayload payload = oauthClient.decodeIdToken(
				accessTokenResponse.getIdToken(),
				authorizationRequest.getNonce(),
				now);
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
		String randomPart = UUID.randomUUID().toString()
			.replaceAll("-", "")
			.substring(0, 8);
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
		return new OauthCreateUrlResponse(
			oauthClient.createAuthURL(request),
			request);
	}
}
