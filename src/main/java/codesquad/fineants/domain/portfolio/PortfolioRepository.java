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

	@Query("select p from Portfolio p where p.member.id = :memberId")
	List<Portfolio> findAllByMemberId(@Param("memberId") Long memberId);

	@Query("select distinct p from Portfolio p join fetch p.member join fetch p.portfolioHoldings holding join fetch holding.stock where p.id = :id")
	Optional<Portfolio> findByPortfolioId(@Param("id") Long id);

	@Query("select p from Portfolio p where p.id = :id")
	@EntityGraph(value = "Portfolio.withAll", type = EntityGraph.EntityGraphType.LOAD)
	Optional<Portfolio> findByPortfolioIdWithAll(@Param("id") Long id);
}
