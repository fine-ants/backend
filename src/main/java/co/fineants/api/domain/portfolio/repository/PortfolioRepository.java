package co.fineants.api.domain.portfolio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

	@Query("select p from Portfolio p where p.detail.name = :name and p.member = :member")
	Optional<Portfolio> findByNameAndMember(@Param("name") String name, @Param("member") Member member);

	@Query("select p from Portfolio p where p.member.id = :memberId order by p.id desc")
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

	@Query("select p from Portfolio p where p.id = :id")
	Optional<Portfolio> findByPortfolioId(@Param("id") Long id);
}
