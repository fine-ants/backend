package codesquad.fineants.domain.member;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {
	Optional<Member> findMemberByEmailAndProvider(String email, String provider);

	Optional<Member> findMemberByEmail(String email);

	boolean existsMemberByEmailAndProvider(String email, String provider);

	boolean existsByNickname(String nickname);

	@Modifying
	@Query("update Member m set m.nickname = :nickname, m.profileUrl = :profileUrl where m.id = :id")
	int updateMember(@Param("nickname") String nickname, @Param("profileUrl") String profileUrl, @Param("id") Long id);

	@Modifying
	@Query("update Member m set m.password = :password where m.id = :id")
	int modifyMemberPassword(@Param("password") String password, @Param("id") Long id);
}
