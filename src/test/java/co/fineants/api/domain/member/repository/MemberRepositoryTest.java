package co.fineants.api.domain.member.repository;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.member.domain.entity.Member;

class MemberRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberRepository repository;

	@DisplayName("이메일과 provider과 동일한 회원이 있으면 true를 반환한다")
	@Test
	void findMemberByEmailAndProvider() {
		// given
		Member member = repository.save(createMember());
		// when
		Optional<Member> findMember = repository.findMemberByEmailAndProvider(member.getEmail(),
			member.getProvider());
		// then
		Assertions.assertThat(findMember).isPresent();
	}

}
