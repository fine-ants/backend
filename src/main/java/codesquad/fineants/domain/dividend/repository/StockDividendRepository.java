package codesquad.fineants.domain.dividend.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import codesquad.fineants.domain.dividend.domain.entity.StockDividend;

public interface StockDividendRepository extends JpaRepository<StockDividend, Long> {

	@Query("select sd from StockDividend sd join fetch sd.stock s order by s.tickerSymbol, sd.recordDate")
	List<StockDividend> findAllStockDividends();

	@Modifying
	@Query("delete from StockDividend sd where sd.stock.tickerSymbol in :tickerSymbols")
	int deleteByTickerSymbols(@Param("tickerSymbols") Set<String> tickerSymbols);

	@Modifying
	@Query("delete from StockDividend sd where sd.stock.stockCode in :stockCodes")
	int deleteByStockCodes(@Param("stockCodes") Set<String> stockCodes);
}
