package codesquad.fineants.domain.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.fcm.domain.entity.FcmToken;
import codesquad.fineants.domain.fcm.repository.FcmRepository;
import codesquad.fineants.domain.member.domain.dto.request.MemberNotificationPreferenceRequest;
import codesquad.fineants.domain.member.domain.dto.response.MemberNotificationPreferenceResponse;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.notificationpreference.domain.entity.NotificationPreference;
import codesquad.fineants.domain.notificationpreference.repository.NotificationPreferenceRepository;

class MemberNotificationPreferenceServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberNotificationPreferenceService service;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private NotificationPreferenceRepository repository;

	@Autowired
	private FcmRepository fcmRepository;

	@AfterEach
	void tearDown() {
		fcmRepository.deleteAllInBatch();
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
				.containsExactly(false, false, false, false),
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

	@DisplayName("사용자가 계정 설정시 기존 설정이 없다면 새로 등록한다")
	@Test
	void updateNotificationPreference_whenNotExistPreference_thenRegisterPreference() {
		// given
		Member member = memberRepository.save(createMember());
		MemberNotificationPreferenceRequest request = MemberNotificationPreferenceRequest.builder()
			.browserNotify(false)
			.targetGainNotify(true)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.build();

		// when
		MemberNotificationPreferenceResponse response = service.updateNotificationPreference(member.getId(), request);

		// then
		NotificationPreference preference = repository.findByMemberId(member.getId()).orElseThrow();
		assertAll(
			() -> assertThat(response)
				.extracting("browserNotify", "targetGainNotify", "maxLossNotify", "targetPriceNotify")
				.containsExactly(false, false, false, false),
			() -> assertThat(preference)
				.extracting("browserNotify", "targetGainNotify", "maxLossNotify", "targetPriceNotify")
				.containsExactly(false, false, false, false)
		);
	}

	@DisplayName("사용자는 회원 알림 설정 수정시 설정값을 모두 비활성화하는 경우 FcmToken을 제거하도록 합니다")
	@Test
	void updateNotificationPreference_whenPreferenceIsAllInActive_thenDeleteFcmToken() {
		// given
		Member member = memberRepository.save(createMember());
		FcmToken fcmToken = fcmRepository.save(createFcmToken(member));
		repository.save(createNotificationPreference(member));
		MemberNotificationPreferenceRequest request = MemberNotificationPreferenceRequest.builder()
			.browserNotify(false)
			.targetGainNotify(false)
			.maxLossNotify(false)
			.targetPriceNotify(false)
			.fcmTokenId(fcmToken.getId())
			.build();

		// when
		MemberNotificationPreferenceResponse response = service.updateNotificationPreference(member.getId(), request);

		// then
		NotificationPreference preference = repository.findByMemberId(member.getId()).orElseThrow();
		assertAll(
			() -> assertThat(response)
				.extracting("browserNotify", "targetGainNotify", "maxLossNotify", "targetPriceNotify")
				.containsExactly(false, false, false, false),
			() -> assertThat(preference)
				.extracting("browserNotify", "targetGainNotify", "maxLossNotify", "targetPriceNotify")
				.containsExactly(false, false, false, false),
			() -> assertThat(fcmRepository.findById(fcmToken.getId()).isEmpty()).isTrue()
		);
	}

	@DisplayName("사용자는 푸시 알림을 허용하지 않은 상태(FCM 토큰 미등록 상태)에서 회원 알람 설정 수정시 FCM 토큰을 삭제하지 않는다")
	@Test
	void updateNotificationPreference_whenPreferenceIsAllInActiveAndFcmTokenIsNotStored_thenNotDeleteFcmToken() {
		// given
		Member member = memberRepository.save(createMember());
		repository.save(createNotificationPreference(member));
		MemberNotificationPreferenceRequest request = MemberNotificationPreferenceRequest.builder()
			.browserNotify(false)
			.targetGainNotify(false)
			.maxLossNotify(false)
			.targetPriceNotify(false)
			.fcmTokenId(null)
			.build();

		// when
		MemberNotificationPreferenceResponse response = service.updateNotificationPreference(member.getId(), request);

		// then
		NotificationPreference preference = repository.findByMemberId(member.getId()).orElseThrow();
		assertAll(
			() -> assertThat(response)
				.extracting("browserNotify", "targetGainNotify", "maxLossNotify", "targetPriceNotify")
				.containsExactly(false, false, false, false),
			() -> assertThat(preference)
				.extracting("browserNotify", "targetGainNotify", "maxLossNotify", "targetPriceNotify")
				.containsExactly(false, false, false, false),
			() -> assertThat(fcmRepository.findAllByMemberId(member.getId()).isEmpty()).isTrue()
		);
	}

	private FcmToken createFcmToken(Member member) {
		return FcmToken.builder()
			.token("token")
			.latestActivationTime(LocalDateTime.now())
			.member(member)
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
