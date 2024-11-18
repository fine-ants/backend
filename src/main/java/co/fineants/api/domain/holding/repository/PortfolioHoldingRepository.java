package co.fineants.api.domain.holding.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;

public interface PortfolioHoldingRepository extends JpaRepository<PortfolioHolding, Long> {

	List<PortfolioHolding> findAllByPortfolio(Portfolio portfolio);

	@Query("select p.stock.tickerSymbol from PortfolioHolding p group by p.stock.tickerSymbol order by p.stock.tickerSymbol")
	List<String> findAllTickerSymbol();

	@Query("select p from PortfolioHolding p "
		+ "where p.portfolio.id = :portfolioId and p.stock.tickerSymbol = :tickerSymbol")
	Optional<PortfolioHolding> findByPortfolioIdAndTickerSymbol(
		@Param("portfolioId") Long portfolioId,
		@Param("tickerSymbol") String tickerSymbol);

	@Query("select p from PortfolioHolding p join fetch p.portfolio "
		+ "where p.id = :portfolioHoldingId and p.portfolio.id = :portfolioId")
	Optional<PortfolioHolding> findByPortfolioHoldingIdAndPortfolioIdWithPortfolio(
		@Param("portfolioHoldingId") Long portfolioHoldingId,
		@Param("portfolioId") Long portfolioId);

	@Modifying
	@Query("delete from PortfolioHolding p where p.portfolio.id = :portFolioId")
	int deleteAllByPortfolioId(@Param("portFolioId") Long portFolioId);

	@Modifying
	@Query("delete from PortfolioHolding p where p.id in :holdingIds")
	int deleteAllByIdIn(@Param("holdingIds") List<Long> holdingIds);
}
