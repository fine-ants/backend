package codesquad.fineants.spring.api.fcm.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.domain.fcm_token.FcmRepository;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.support.AuthMember;
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

	@AfterEach
	void tearDown() {
		fcmRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
	}

	@DisplayName("사용자는 FCM 토큰을 등록한다")
	@Test
	void registerToken() {
		// given
		Member member = memberRepository.save(createMember());
		FcmRegisterRequest request = FcmRegisterRequest.builder()
			.fcmToken("fcmToken")
			.build();

		// when
		FcmRegisterResponse response = fcmService.registerToken(request, AuthMember.from(member));

		// then
		assertThat(response.getFcmTokenId()).isGreaterThan(0);
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
