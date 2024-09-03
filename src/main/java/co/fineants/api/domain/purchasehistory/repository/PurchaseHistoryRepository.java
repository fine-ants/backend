package co.fineants.api.domain.purchasehistory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;

public interface PurchaseHistoryRepository extends JpaRepository<PurchaseHistory, Long> {

	@Query("select p from PurchaseHistory p where p.portfolioHolding.id in(:holdingIds)")
	List<PurchaseHistory> findAllByHoldingIds(@Param("holdingIds") List<Long> holdingIds);

	List<PurchaseHistory> findAllByPortfolioHoldingId(Long portfolioStockId);

	@Modifying
	@Query("delete from PurchaseHistory p where p.portfolioHolding.id in(:holdingIds)")
	int deleteAllByPortfolioHoldingIdIn(@Param("holdingIds") List<Long> holdingIds);

	void deleteByPortfolioHoldingId(Long portfolioHoldingId);

	@Modifying
	@Query("delete from PurchaseHistory p where p.portfolioHolding.id in(:holdingIds)")
	int deleteAllByHoldingIds(@Param("holdingIds") List<Long> holdingIds);
}
