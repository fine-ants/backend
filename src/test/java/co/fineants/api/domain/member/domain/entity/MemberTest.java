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
			.containsExactlyInAnyOrder(memberMemberRole, managerMemberRole);
	}

	@DisplayName("MemberRole을 다른 회원의 역할에 추가하면 기존 연관관계를 해제한다")
	@Test
	void addMemberRole_whenAssignMemberRoleToOtherMember_thenReleaseEntityRelationShip() {
		// given
		Member member = createMember();
		Role memberRole = Role.create("ROLE_USER", "회원");
		Role managerRole = Role.create("ROLE_MANAGER", "매니저");
		MemberRole managerMemberRole = MemberRole.of(member, managerRole);
		member.addMemberRole(managerMemberRole);

		Member otherMember = createMember("other1", "other1@gmail.com");
		// when
		otherMember.addMemberRole(managerMemberRole);
		// then
		Assertions.assertThat(member.getRoles())
			.hasSize(1)
			.containsExactly(MemberRole.of(member, memberRole));
		Assertions.assertThat(otherMember.getRoles())
			.hasSize(2)
			.containsExactlyInAnyOrder(MemberRole.of(otherMember, memberRole), MemberRole.of(otherMember, managerRole));
	}

	@Transactional
	@DisplayName("회원의 알림 설정을 변경한다")
	@Test
	void setNotificationPreference() {
		// given
		Member member = createMember();
		NotificationPreference preference = NotificationPreference.create(false, false, false, false);
		// when
		member.setNotificationPreference(preference);
		// then
		Assertions.assertThat(member.getNotificationPreference()).isEqualTo(preference);
		Assertions.assertThat(preference.getMember()).isEqualTo(member);
	}

	@DisplayName("알림이 회원을 변경한다")
	@Test
	void setMember() {
		// given
		Member member = createMember();
		NotificationPreference preference = NotificationPreference.create(false, false, false, false);
		// when
		preference.setMember(member);
		// then
		Assertions.assertThat(member.getNotificationPreference()).isEqualTo(preference);
		Assertions.assertThat(preference.getMember()).isEqualTo(member);
	}

	@DisplayName("회원의 간단한 정보를 출력한다")
	@Test
	void givenMember_whenToString_thenGenerateMemberInfo() {
		// given
		Member member = createMember();
		// when
		String actual = member.toString();
		// then
		Assertions.assertThat(actual)
			.isEqualTo("Member(id=null, nickname=nemo1234, email=dragonbead95@naver.com, roles=[ROLE_USER])");
	}
}
