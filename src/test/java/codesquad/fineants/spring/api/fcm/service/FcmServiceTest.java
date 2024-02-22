package codesquad.fineants.spring.api.fcm.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import codesquad.fineants.domain.fcm_token.FcmRepository;
import codesquad.fineants.domain.fcm_token.FcmToken;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.spring.AbstractContainerBaseTest;
import codesquad.fineants.spring.api.errors.errorcode.FcmErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.fcm.request.FcmRegisterRequest;
import codesquad.fineants.spring.api.fcm.response.FcmDeleteResponse;
import codesquad.fineants.spring.api.fcm.response.FcmRegisterResponse;

class FcmServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private FcmService fcmService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private FcmRepository fcmRepository;

	@MockBean
	private FirebaseMessaging firebaseMessaging;

	@AfterEach
	void tearDown() {
		fcmRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
	}

	@DisplayName("사용자는 FCM 토큰을 등록한다")
	@Test
	void registerToken() throws FirebaseMessagingException {
		// given
		Member member = memberRepository.save(createMember());
		FcmRegisterRequest request = FcmRegisterRequest.builder()
			.fcmToken("fcmToken")
			.build();

		String messageId = "1";
		given(firebaseMessaging.send(any(Message.class), anyBoolean()))
			.willReturn(messageId);

		// when
		FcmRegisterResponse response = fcmService.registerToken(request, AuthMember.from(member));

		// then
		assertThat(response.getFcmTokenId()).isGreaterThan(0);
	}

	@DisplayName("사용자는 유효하지 않은 FCM 토큰을 등록할 수 없다")
	@Test
	void registerToken_whenInvalidToken_thenThrow400Error() throws FirebaseMessagingException {
		// given
		Member member = memberRepository.save(createMember());
		FcmRegisterRequest request = FcmRegisterRequest.builder()
			.fcmToken("fcmToken")
			.build();

		given(firebaseMessaging.send(any(Message.class), anyBoolean()))
			.willThrow(FirebaseMessagingException.class);

		// when
		Throwable throwable = catchThrowable(() -> fcmService.registerToken(request, AuthMember.from(member)));

		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage(FcmErrorCode.BAD_REQUEST_FCM_TOKEN.getMessage());
	}

	@DisplayName("사용자는 이미 동일한 FCM 토큰이 등록되어 있는 경우 최신 활성화 시간을 업데이트한다")
	@Test
	void registerToken_whenAlreadyFcmToken_thenThrow409Error() {
		// given
		Member member = memberRepository.save(createMember());
		FcmToken token = fcmRepository.save(createFcmToken(member));
		FcmRegisterRequest request = FcmRegisterRequest.builder()
			.fcmToken("fcmToken")
			.build();
		// when
		FcmRegisterResponse response = fcmService.registerToken(request, AuthMember.from(member));

		// then
		Assertions.assertAll(
			() -> assertThat(response)
				.extracting("fcmTokenId")
				.isEqualTo(token.getId()),
			() -> {
				FcmToken findFcmToken = fcmRepository.findById(token.getId()).orElseThrow();
				assertThat(token.getLatestActivationTime().isBefore(findFcmToken.getLatestActivationTime())).isTrue();
			}
		);
	}

	@DisplayName("사용자는 FCM 토큰을 삭제한다")
	@Test
	void deleteToken() {
		// given
		Member member = memberRepository.save(createMember());
		FcmToken fcmToken = fcmRepository.save(createFcmToken(member));

		// when
		FcmDeleteResponse response = fcmService.deleteToken(fcmToken.getId(), member.getId());

		// then
		Assertions.assertAll(
			() -> assertThat(response)
				.extracting("fcmTokenId")
				.isEqualTo(fcmToken.getId()),
			() -> assertThat(fcmRepository.findById(fcmToken.getId()).isEmpty()).isTrue()
		);
	}

	private Member createMember() {
		return Member.builder()
			.nickname("nemo1234")
			.email("dragonbead95@naver.com")
			.password("nemo1234")
			.provider("local")
			.build();
	}

	private FcmToken createFcmToken(Member member) {
		return FcmToken.builder()
			.latestActivationTime(LocalDateTime.now())
			.token("fcmToken")
			.member(member)
			.build();
	}
}
