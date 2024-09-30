package co.fineants.api.domain.member.domain.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.member.repository.MemberRepository;

class MemberTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberRepository repository;

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

}
