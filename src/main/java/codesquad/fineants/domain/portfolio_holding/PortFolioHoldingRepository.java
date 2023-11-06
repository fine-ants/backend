package codesquad.fineants.domain.portfolio_holding;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import codesquad.fineants.domain.portfolio.Portfolio;

public interface PortFolioHoldingRepository extends JpaRepository<PortfolioHolding, Long> {

	int deleteAllByPortfolioId(Long portFolioId);

	List<PortfolioHolding> findAllByPortfolio(Portfolio portfolio);

}
