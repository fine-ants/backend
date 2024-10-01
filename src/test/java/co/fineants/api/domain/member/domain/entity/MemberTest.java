package co.fineants.api.domain.member.domain.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;

class MemberTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberRepository repository;

	@Transactional
	@DisplayName("회원에 매니저 역할을 추가한다")
	@Test
	void addMemberRole() {
		// given
		Member member = createMember();
		Role memberRole = Role.create("ROLE_USER", "회원");
		Role managerRole = Role.create("ROLE_MANAGER", "매니저");
		MemberRole memberMemberRole = MemberRole.of(member, memberRole);
		MemberRole managerMemberRole = MemberRole.of(member, managerRole);
		// when
		member.addMemberRole(managerMemberRole);
		// then
		Member saveMember = repository.save(member);
		Assertions.assertThat(saveMember.getRoles())
			.hasSize(2)
			.containsExactly(memberMemberRole, managerMemberRole);
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
