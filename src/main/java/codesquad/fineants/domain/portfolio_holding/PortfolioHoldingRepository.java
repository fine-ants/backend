package codesquad.fineants.domain.portfolio_holding;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import codesquad.fineants.domain.portfolio.Portfolio;

public interface PortfolioHoldingRepository extends JpaRepository<PortfolioHolding, Long> {

	int deleteAllByPortfolioId(Long portFolioId);

	List<PortfolioHolding> findAllByPortfolio(Portfolio portfolio);

	@Query("select distinct s.tickerSymbol from PortfolioHolding p inner join Stock s on p.stock.tickerSymbol = s.tickerSymbol")
	List<String> findAllTickerSymbol();

	int deleteAllByIdIn(List<Long> portfolioHoldingIds);

	@Query("select count(p) > 0 from PortfolioHolding p where p.id = :portfolioHoldingId and p.portfolio.member.id = :memberId")
	boolean existsByIdAndMemberId(@Param("portfolioHoldingId") Long portfolioHoldingId,
		@Param("memberId") Long memberId);
}
