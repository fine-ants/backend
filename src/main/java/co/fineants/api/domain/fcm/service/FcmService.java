package co.fineants.api.domain.fcm.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import co.fineants.api.domain.fcm.domain.dto.request.FcmRegisterRequest;
import co.fineants.api.domain.fcm.domain.dto.response.FcmDeleteResponse;
import co.fineants.api.domain.fcm.domain.dto.response.FcmRegisterResponse;
import co.fineants.api.domain.fcm.domain.entity.FcmToken;
import co.fineants.api.domain.fcm.repository.FcmRepository;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.global.common.authorized.Authorized;
import co.fineants.api.global.common.authorized.service.FcmAuthorizedService;
import co.fineants.api.global.common.resource.ResourceId;
import co.fineants.api.global.errors.errorcode.FcmErrorCode;
import co.fineants.api.global.errors.errorcode.MemberErrorCode;
import co.fineants.api.global.errors.exception.FineAntsException;
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
	@Secured("ROLE_USER")
	public FcmRegisterResponse createToken(FcmRegisterRequest request, Long memberId) {
		Member member = findMember(memberId);
		verifyFcmToken(request.getFcmToken());

		FcmToken fcmToken = fcmRepository.findByTokenAndMemberId(request.getFcmToken(), memberId)
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

	private Member findMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new FineAntsException(MemberErrorCode.NOT_FOUND_MEMBER));
	}

	@Transactional
	@Authorized(serviceClass = FcmAuthorizedService.class)
	@Secured("ROLE_USER")
	public FcmDeleteResponse deleteToken(@ResourceId Long fcmTokenId) {
		int deleteCount = fcmRepository.deleteByFcmTokenId(fcmTokenId);
		log.info("FCM 토큰 삭제 개수 : deleteCount={}", deleteCount);
		return FcmDeleteResponse.from(fcmTokenId);
	}

	@Transactional
	public void deleteToken(String token) {
		int deleteCount = fcmRepository.deleteAllByToken(token);
		log.info("FCM 토큰 삭제 개수 : deleteCount={}", deleteCount);
	}

	public List<String> findTokens(Long memberId) {
		return fcmRepository.findAllByMemberId(memberId).stream()
			.map(FcmToken::getToken)
			.toList();
	}
}
