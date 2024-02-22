package codesquad.fineants.spring.api.fcm.service;

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
import codesquad.fineants.spring.api.errors.errorcode.FcmErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
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
	public FcmRegisterResponse registerToken(FcmRegisterRequest request, AuthMember authMember) {
		Member member = findMember(authMember);
		verifyFcmToken(request.getFcmToken());

		FcmToken fcmToken = fcmRepository.findByTokenAndMemberId(request.getFcmToken(), authMember.getMemberId())
			.orElseGet(() -> request.toEntity(member));
		fcmToken.refreshLatestActivationTime();
		FcmToken saveFcmToken = fcmRepository.save(fcmToken);
		return FcmRegisterResponse.from(saveFcmToken);
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

			throw new BadRequestException(FcmErrorCode.BAD_REQUEST_FCM_TOKEN);
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
}
