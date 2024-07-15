package codesquad.fineants.domain.fcm.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import codesquad.fineants.domain.fcm.domain.dto.request.FcmRegisterRequest;
import codesquad.fineants.domain.fcm.domain.dto.response.FcmDeleteResponse;
import codesquad.fineants.domain.fcm.domain.dto.response.FcmRegisterResponse;
import codesquad.fineants.domain.fcm.domain.entity.FcmToken;
import codesquad.fineants.domain.fcm.repository.FcmRepository;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.global.errors.errorcode.FcmErrorCode;
import codesquad.fineants.global.errors.errorcode.MemberErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;
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
	@Secured("ROLE_USER")
	public FcmDeleteResponse deleteToken(Long fcmTokenId, Long memberId) {
		FcmToken fcmToken = fcmRepository.findById(fcmTokenId)
			.orElseThrow(() -> new FineAntsException(FcmErrorCode.NOT_FOUND_FCM_TOKEN));
		validateFcmTokenAuthorization(fcmToken, memberId);
		int deleteCount = fcmRepository.deleteByFcmTokenId(fcmTokenId, memberId);
		log.info("FCM 토큰 삭제 개수 : deleteCount={}", deleteCount);
		return FcmDeleteResponse.from(fcmTokenId);
	}

	private void validateFcmTokenAuthorization(FcmToken fcmToken, Long memberId) {
		if (!fcmToken.hasAuthorization(memberId)) {
			throw new FineAntsException(FcmErrorCode.FORBIDDEN_FCM_TOKEN);
		}
	}

	@Transactional
	@Secured(value = {"ROLE_USER", "ROLE_MANAGER", "ROLE_ADMIN"})
	public void deleteToken(String token) {
		int deleteCount = fcmRepository.deleteAllByToken(token);
		log.info("FCM 토큰 삭제 개수 : deleteCount={}", deleteCount);
	}

	public List<String> findTokens(Long memberId) {
		return fcmRepository.findAllByMemberId(memberId).stream()
			.map(FcmToken::getToken)
			.collect(Collectors.toList());
	}
}
