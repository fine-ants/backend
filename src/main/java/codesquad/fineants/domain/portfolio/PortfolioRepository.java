package codesquad.fineants.domain.portfolio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import codesquad.fineants.domain.member.Member;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

	boolean existsByNameAndMember(String name, Member member);

	List<Portfolio> findAllByMemberIdOrderByIdDesc(@Param("memberId") Long memberId);

	@Query("select p from Portfolio p where p.member.id = :memberId order by p.createAt asc")
	List<Portfolio> findAllByMemberId(@Param("memberId") Long memberId);

	@Query("select distinct p from Portfolio p join fetch p.member m join fetch m.notificationPreference join fetch p.portfolioHoldings ph join fetch ph.stock s")
	List<Portfolio> findAllWithAll();

	@Query("select p from Portfolio p where p.id = :id")
	@EntityGraph(value = "Portfolio.withAll", type = EntityGraph.EntityGraphType.LOAD)
	Optional<Portfolio> findByPortfolioIdWithAll(@Param("id") Long id);

	@Query("select distinct p from Portfolio p join fetch p.member m join fetch m.notificationPreference join fetch p.portfolioHoldings ph join fetch ph.stock s where p.id = :id")
	Optional<Portfolio> findByIdWithAll(@Param("id") Long id);
}
