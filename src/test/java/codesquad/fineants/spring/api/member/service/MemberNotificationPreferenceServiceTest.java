package codesquad.fineants.spring.api.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.notification_preference.NotificationPreference;
import codesquad.fineants.domain.notification_preference.NotificationPreferenceRepository;
import codesquad.fineants.spring.api.member.request.MemberNotificationPreferenceRequest;
import codesquad.fineants.spring.api.member.response.MemberNotificationPreferenceResponse;

@ActiveProfiles("test")
@SpringBootTest
class MemberNotificationPreferenceServiceTest {

	@Autowired
	private MemberNotificationPreferenceService service;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private NotificationPreferenceRepository repository;

	@AfterEach
	void tearDown() {
		repository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
	}

	@DisplayName("사용자는 계정의 알림 설정을 변경한다")
	@Test
	void updateNotificationPreference() {
		// given
		Member member = memberRepository.save(createMember());
		repository.save(createNotificationPreference(member));
		MemberNotificationPreferenceRequest request = MemberNotificationPreferenceRequest.builder()
			.browserNotify(false)
			.targetGainNotify(true)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.build();

		// when
		MemberNotificationPreferenceResponse response = service.updateNotificationPreference(
			member.getId(), request);

		// then
		NotificationPreference preference = repository.findByMemberId(member.getId()).orElseThrow();
		assertAll(
			() -> assertThat(response)
				.extracting("browserNotify", "targetGainNotify", "maxLossNotify", "targetPriceNotify")
				.containsExactly(false, true, true, true),
			() -> assertThat(preference)
				.extracting("browserNotify", "targetGainNotify", "maxLossNotify", "targetPriceNotify")
				.containsExactly(false, true, true, true)
		);
	}

	private Member createMember() {
		return Member.builder()
			.nickname("일개미1234")
			.email("dragonbead95@naver.com")
			.password("kim1234@")
			.provider("local")
			.build();
	}

	private NotificationPreference createNotificationPreference(Member member) {
		return NotificationPreference.builder()
			.browserNotify(true)
			.targetGainNotify(false)
			.maxLossNotify(false)
			.targetPriceNotify(false)
			.member(member)
			.build();
	}
}
