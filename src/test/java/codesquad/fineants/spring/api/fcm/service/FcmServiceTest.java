package codesquad.fineants.spring.api.fcm.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import codesquad.fineants.domain.fcm_token.FcmRepository;
import codesquad.fineants.domain.fcm_token.FcmToken;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.spring.api.errors.errorcode.FcmErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.errors.exception.ConflictException;
import codesquad.fineants.spring.api.fcm.request.FcmRegisterRequest;
import codesquad.fineants.spring.api.fcm.response.FcmRegisterResponse;

@ActiveProfiles("test")
@SpringBootTest
class FcmServiceTest {

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

	@DisplayName("사용자는 이미 동일한 FCM 토큰이 존재하면 등록할 수 없다")
	@Test
	void registerToken_whenAlreadyFcmToken_thenThrow409Error() {
		// given
		Member member = memberRepository.save(createMember());
		fcmRepository.save(FcmToken.builder()
			.latestActivationTime(LocalDateTime.now())
			.token("fcmToken")
			.member(member)
			.build());
		FcmRegisterRequest request = FcmRegisterRequest.builder()
			.fcmToken("fcmToken")
			.build();
		// when
		Throwable throwable = catchThrowable(() -> fcmService.registerToken(request, AuthMember.from(member)));

		// then
		assertThat(throwable)
			.isInstanceOf(ConflictException.class)
			.hasMessage(FcmErrorCode.CONFLICT_FCM_TOKEN.getMessage());
	}

	private Member createMember() {
		return Member.builder()
			.nickname("nemo1234")
			.email("dragonbead95@naver.com")
			.password("nemo1234")
			.provider("local")
			.build();
	}
}
