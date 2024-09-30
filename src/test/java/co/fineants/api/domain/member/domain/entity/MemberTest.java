package co.fineants.api.domain.member.domain.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import co.fineants.api.domain.notificationpreference.repository.NotificationPreferenceRepository;

class MemberTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberRepository repository;

	@Autowired
	private NotificationPreferenceRepository notificationPreferenceRepository;

	@Transactional
	@DisplayName("회원에 매니저 역할을 추가한다")
	@Test
	void addMemberRole() {
		// given
		Member member = createMember();
		// when
		member.addMemberRole(MemberRole.create(member, Role.create("ROLE_MANAGER", "manager")));
		// then
		Member saveMember = repository.save(member);
		Assertions.assertThat(saveMember.getRoles())
			.hasSize(2)
			.map(MemberRole::getRoleName)
			.containsExactly("ROLE_USER", "ROLE_MANAGER");
	}

	@Transactional
	@DisplayName("회원의 알림 설정을 변경한다")
	@Test
	void changeNotificationPreference() {
		// given
		Member member = createMember();
		NotificationPreference preference = NotificationPreference.create(false, false, false, false);
		// when
		member.changeNotificationPreference(preference);
		// then
		Assertions.assertThat(member.getNotificationPreference()).isEqualTo(preference);
		Assertions.assertThat(preference.getMember()).isEqualTo(member);
	}

	@DisplayName("알림이 회원을 변경한다")
	@Test
	void changeMember() {
		// given
		Member member = createMember();
		NotificationPreference preference = NotificationPreference.create(false, false, false, false);
		// when
		preference.changeMember(member);
		// then
		Assertions.assertThat(member.getNotificationPreference()).isEqualTo(preference);
		Assertions.assertThat(preference.getMember()).isEqualTo(member);
	}
}
