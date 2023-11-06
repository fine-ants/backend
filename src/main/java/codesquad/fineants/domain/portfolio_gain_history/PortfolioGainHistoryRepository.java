package codesquad.fineants.domain.portfolio_gain_history;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PortfolioGainHistoryRepository extends JpaRepository<PortfolioGainHistory, Long> {
	@Query(value = "select p.*, p2.* from fineAnts.portfolio_gain_history p "
		+ "inner join fineAnts.portfolio p2 on p.portfolio_id = p2.id "
		+ "where p.portfolio_id = :portfolioId and p.create_at <= :createAt "
		+ "order by p.create_at desc "
		+ "limit 1", nativeQuery = true)
	Optional<PortfolioGainHistory> findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc(
		Long portfolioId, LocalDateTime createAt);
}
