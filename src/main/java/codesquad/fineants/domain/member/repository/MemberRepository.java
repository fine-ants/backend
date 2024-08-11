package codesquad.fineants.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import codesquad.fineants.domain.member.domain.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
	@Query("select distinct m from Member m join fetch m.roles where m.email = :email and m.provider = :provider")
	Optional<Member> findMemberByEmailAndProvider(@Param("email") String email, @Param("provider") String provider);

	@Query("select m from Member m where m.nickname = :nickname and m.id != :memberId")
	Optional<Member> findMemberByNicknameAndNotMemberId(@Param("nickname") String nickname,
		@Param("memberId") Long memberId);

	boolean existsMemberByEmailAndProvider(String email, String provider);

	boolean existsByNickname(String nickname);

	@Modifying
	@Query("update Member m set m.password = :password where m.id = :id")
	int modifyMemberPassword(@Param("password") String password, @Param("id") Long id);
}
