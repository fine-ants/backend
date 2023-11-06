package codesquad.fineants.domain.member;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
	Optional<Member> findMemberByEmailAndProvider(String email, String provider);

	Optional<Member> findMemberByEmail(String email);
}
