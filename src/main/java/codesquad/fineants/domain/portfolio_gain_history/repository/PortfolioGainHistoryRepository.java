package codesquad.fineants.domain.portfolio_gain_history.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import codesquad.fineants.domain.portfolio_gain_history.domain.entity.PortfolioGainHistory;

public interface PortfolioGainHistoryRepository extends JpaRepository<PortfolioGainHistory, Long> {
	@Query(value = "select p.*, p2.* from fineAnts.portfolio_gain_history p "
		+ "inner join fineAnts.portfolio p2 on p.portfolio_id = p2.id "
		+ "where p.portfolio_id = :portfolioId and p.create_at <= :createAt "
		+ "order by p.create_at desc "
		+ "limit 1", nativeQuery = true)
	Optional<PortfolioGainHistory> findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc(
		@Param("portfolioId") Long portfolioId, @Param("createAt") LocalDateTime createAt);

	List<PortfolioGainHistory> findAllByPortfolioId(Long portfolioId);

	int deleteAllByPortfolioId(Long portfolioId);
}
