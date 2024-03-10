package codesquad.fineants.spring.api.fcm.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import codesquad.fineants.domain.fcm_token.FcmRepository;
import codesquad.fineants.domain.fcm_token.FcmToken;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.spring.api.common.errors.errorcode.FcmErrorCode;
import codesquad.fineants.spring.api.common.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.common.errors.exception.FineAntsException;
import codesquad.fineants.spring.api.common.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.fcm.request.FcmRegisterRequest;
import codesquad.fineants.spring.api.fcm.response.FcmDeleteResponse;
import codesquad.fineants.spring.api.fcm.response.FcmRegisterResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmService {

	private final FcmRepository fcmRepository;
	private final MemberRepository memberRepository;
	private final FirebaseMessaging firebaseMessaging;

	@Transactional
	public FcmRegisterResponse createToken(FcmRegisterRequest request, AuthMember authMember) {
		Member member = findMember(authMember);
		verifyFcmToken(request.getFcmToken());

		FcmToken fcmToken = fcmRepository.findByTokenAndMemberId(request.getFcmToken(), authMember.getMemberId())
			.orElseGet(() -> request.toEntity(member));
		fcmToken.refreshLatestActivationTime();
		try {
			FcmToken saveFcmToken = fcmRepository.save(fcmToken);
			FcmRegisterResponse response = FcmRegisterResponse.from(saveFcmToken);
			log.info("FCM Token 저장 결과 : {}", response);
			return response;
		} catch (DataIntegrityViolationException e) {
			throw new FineAntsException(FcmErrorCode.CONFLICT_FCM_TOKEN);
		}
	}

	private void verifyFcmToken(String token) {
		Message message = Message.builder()
			.setToken(token)
			.setNotification(Notification.builder().build())
			.build();

		try {
			firebaseMessaging.send(message, true);
		} catch (FirebaseMessagingException e) {
			log.info(e.getMessage(), e);
			throw new FineAntsException(FcmErrorCode.BAD_REQUEST_FCM_TOKEN);
		}
	}

	private Member findMember(AuthMember authMember) {
		return memberRepository.findById(authMember.getMemberId())
			.orElseThrow(() -> new NotFoundResourceException(MemberErrorCode.NOT_FOUND_MEMBER));
	}

	@Transactional
	public FcmDeleteResponse deleteToken(Long fcmTokenId, Long memberId) {
		int deleteCount = fcmRepository.deleteByFcmTokenId(fcmTokenId, memberId);
		log.info("FCM 토큰 삭제 개수 : deleteCount={}", deleteCount);
		return FcmDeleteResponse.from(fcmTokenId);
	}

	@Transactional
	public int deleteToken(String token) {
		int deleteCount = fcmRepository.deleteAllByToken(token);
		log.info("FCM 토큰 삭제 개수 : deleteCount={}", deleteCount);
		return deleteCount;
	}

	public List<String> findTokens(Long memberId) {
		return fcmRepository.findAllByMemberId(memberId).stream()
			.map(FcmToken::getToken)
			.collect(Collectors.toList());
	}
}
