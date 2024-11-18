package co.fineants.api.domain.member.domain.entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.AbstractContainerBaseTest;

class MemberRoleTest extends AbstractContainerBaseTest {

	@DisplayName("두 MemberRole은 동일하다")
	@Test
	void testEquals() {
		// given
		Role memberRole = Role.create("ROLE_USER", "회원");
		MemberRole memberRole1 = MemberRole.of(createMember(), memberRole);
		MemberRole memberRole2 = MemberRole.of(createMember(), memberRole);
		Set<MemberRole> set = Collections.unmodifiableSet(new HashSet<>(List.of(memberRole1)));
		// when
		boolean actual = set.contains(memberRole2);
		// then
		Assertions.assertThat(actual).isTrue();
	}

	@DisplayName("")
	@Test
	void test() {
		// given
		Role userRole = Role.create("ROLE_USER", "회원");
		Role managerRole = Role.create("ROLE_MANAGER", "매니저");

		Member existMember = createMember();
		MemberRole memberRole1 = MemberRole.of(existMember, managerRole);

		Member other = createMember("other1", "other1@gmail.com");
		// when
		memberRole1.setMember(other);
		// then
		Assertions.assertThat(memberRole1.getMember()).isEqualTo(other);
		Assertions.assertThat(other.getRoles())
			.hasSize(2)
			.containsExactlyInAnyOrder(
				MemberRole.of(other, userRole),
				MemberRole.of(other, managerRole)
			);
		Assertions.assertThat(existMember.getRoles())
			.hasSize(1)
			.containsExactlyInAnyOrder(
				MemberRole.of(existMember, userRole)
			);
	}

}
