package codesquad.fineants.domain.purchase_history;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseHistoryRepository extends JpaRepository<PurchaseHistory, Long> {

	int deleteAllByPortfolioHoldingIdIn(List<Long> portfolioId);

	List<PurchaseHistory> findAllByPortfolioHoldingId(Long portfolioStockId);
}
