package codesquad.fineants.spring.api.member.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.jwt.Jwt;
import codesquad.fineants.domain.jwt.JwtProvider;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.client.OauthClient;
import codesquad.fineants.domain.oauth.repository.OauthClientRepository;
import codesquad.fineants.spring.api.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.errors.exception.FineAntsException;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.member.request.OauthMemberLogoutRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberRefreshRequest;
import codesquad.fineants.spring.api.member.response.OauthAccessTokenResponse;
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
	private final OauthClientRepository oauthClientRepository;
	private final MemberRepository memberRepository;
	private final JwtProvider jwtProvider;
	private final OauthMemberRedisService oauthMemberRedisService;
	private final OauthMemberRedisService redisService;

	public OauthMemberLoginResponse login(String provider, String code, LocalDateTime now, String redirectUrl) {
		log.info("provider : {}, code : {}", provider, code);

		OauthUserProfileResponse userProfileResponse = getOauthUserProfileResponse(provider, code, redirectUrl);
		log.info("userProfileResponse : {}", userProfileResponse);

		Optional<Member> optionalMember = getLoginMember(provider, userProfileResponse);

		Member member = optionalMember.orElseGet(() -> {
			Member newMember = Member.builder()
				.email(userProfileResponse.getEmail())
				.nickname(generateRandomNickname())
				.provider(provider)
				.profileUrl(userProfileResponse.getProfileImage())
				.build();
			memberRepository.save(newMember);
			return newMember;
		});

		Jwt jwt = jwtProvider.createJwtBasedOnMember(member, now);
		log.debug("로그인 서비스 요청 중 jwt 객체 생성 : {}", jwt);

		oauthMemberRedisService.saveRefreshToken(member.createRedisKey(), jwt);

		return OauthMemberLoginResponse.of(jwt, member);
	}

	private OauthUserProfileResponse getOauthUserProfileResponse(String provider, String authorizationCode,
		String redirectUrl) {
		OauthClient oauthClient = oauthClientRepository.findOneBy(provider);

		OauthAccessTokenResponse accessTokenResponse =
			oauthClient.exchangeAccessTokenByAuthorizationCode(authorizationCode, redirectUrl);
		log.info("{}", accessTokenResponse);

		OauthUserProfileResponse userProfileResponse =
			oauthClient.getUserProfileByAccessToken(accessTokenResponse);
		log.info("{}", userProfileResponse);
		return userProfileResponse;
	}

	private Optional<Member> getLoginMember(String provider, OauthUserProfileResponse userProfileResponse) {
		String email = userProfileResponse.getEmail();
		return memberRepository.findMemberByEmailAndProvider(email, provider);
	}

	private String generateRandomNickname() {
		String randomPart = UUID.randomUUID().toString()
			.replaceAll("-", "")
			.substring(0, 8);
		return "일개미" + randomPart;
	}

	public void logout(String accessToken, OauthMemberLogoutRequest request) {
		log.info("로그아웃 서비스 요청 : accessToken={}, request={}", accessToken, request);
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
}
