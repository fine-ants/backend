package codesquad.fineants.domain.purchase_history;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseHistoryRepository extends JpaRepository<PurchaseHistory, Long> {

	int deleteAllByPortFolioHoldingIdIn(List<Long> portfolioId);

	List<PurchaseHistory> findAllByPortFolioHoldingId(Long portfolioStockId);
}
