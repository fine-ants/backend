package co.fineants.api.domain.gainhistory.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;

public interface PortfolioGainHistoryRepository extends JpaRepository<PortfolioGainHistory, Long> {
	@Query(value = "select p, p2 from PortfolioGainHistory p inner join Portfolio p2 on p.portfolio.id = p2.id "
		+ "where p.portfolio.id = :portfolioId and p.createAt <= :createAt order by p.createAt desc")
	List<PortfolioGainHistory> findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc(
		@Param("portfolioId") Long portfolioId, @Param("createAt") LocalDateTime createAt);

	List<PortfolioGainHistory> findAllByPortfolioId(Long portfolioId);

	int deleteAllByPortfolioId(Long portfolioId);
}
