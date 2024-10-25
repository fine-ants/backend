package co.fineants.api.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.fineants.api.domain.member.domain.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
	@Query("select distinct m from Member m join fetch m.roles "
		+ "where m.profile.email = :email and m.profile.provider = :provider")
	Optional<Member> findMemberByEmailAndProvider(@Param("email") String email, @Param("provider") String provider);

	@Query("select distinct m from Member m join fetch m.roles "
		+ "where m.profile.email = :#{#member.profile.email} and m.profile.provider = :provider")
	Optional<Member> findMemberByEmailAndProvider(@Param("member") Member member, @Param("provider") String provider);

	@Query("select m from Member m where m.profile.nickname = :nickname and m.id != :memberId")
	Optional<Member> findMemberByNicknameAndNotMemberId(@Param("nickname") String nickname,
		@Param("memberId") Long memberId);

	@Query("select m from Member m where m.profile.nickname = :nickname")
	Optional<Member> findMemberByNickname(@Param("nickname") String nickname);

	@Query("select m from Member m where m.profile.nickname = :#{#member.profile.nickname}")
	Optional<Member> findMemberByNickname(@Param("member") Member member);

	@Modifying
	@Query("update Member m set m.profile.password = :password where m.id = :id")
	int modifyMemberPassword(@Param("password") String password, @Param("id") Long id);
}
