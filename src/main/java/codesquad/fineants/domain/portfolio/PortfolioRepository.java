package codesquad.fineants.domain.portfolio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import codesquad.fineants.domain.member.Member;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

	boolean existsByNameAndMember(String name, Member member);

	@Query("select p from Portfolio p join fetch p.member where p.member.id = :memberId order by p.id desc")
	List<Portfolio> findAllByMemberIdOrderByIdDesc(@Param("memberId") Long memberId);

	@Query("select p from Portfolio p join fetch p.member where p.member.id = :memberId")
	List<Portfolio> findAllByMemberId(@Param("memberId") Long memberId);
}
