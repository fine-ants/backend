package codesquad.fineants.spring.api.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

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

	@DisplayName("사용자는 계정 알림 설정을 등록합니다")
	@Test
	void registerDefaultNotificationPreference() {
		// given
		Member member = memberRepository.save(createMember());

		// when
		MemberNotificationPreferenceResponse response = service.registerDefaultNotificationPreference(member);

		// then
		assertAll(
			() -> assertThat(response)
				.extracting("browserNotify", "targetGainNotify", "maxLossNotify", "targetPriceNotify")
				.containsExactly(false, true, true, true),
			() -> assertThat(repository.findByMemberId(member.getId()).isPresent())
				.isTrue()
		);
	}

	@DisplayName("사용자는 이미 등록한 계정 알림 설정은 새로 등록하지 않는다")
	@Test
	void registerDefaultNotificationPreference_whenExistNotificationPreference_thenNotRegister() {
		// given
		Member member = memberRepository.save(createMember());
		repository.save(NotificationPreference.defaultSetting(member));

		// when
		service.registerDefaultNotificationPreference(member);

		// then
		List<NotificationPreference> preferences = repository.findAll();
		assertThat(preferences).hasSize(1);
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
