package codesquad.fineants.domain.dividend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import codesquad.fineants.domain.dividend.domain.entity.StockDividend;

public interface StockDividendRepository extends JpaRepository<StockDividend, Long> {

	@Query("select sd from StockDividend sd join fetch sd.stock s order by s.tickerSymbol, sd.recordDate")
	List<StockDividend> findAllStockDividends();

	@Query("select sd from StockDividend sd join fetch sd.stock s where s.tickerSymbol = :tickerSymbol order by s.tickerSymbol, sd.recordDate")
	List<StockDividend> findStockDividendsByTickerSymbol(@Param("tickerSymbol") String tickerSymbol);

	@Query("select sd from StockDividend sd join fetch sd.stock s where s.tickerSymbol = :tickerSymbol and sd.recordDate = :recordDate order by s.tickerSymbol, sd.recordDate")
	Optional<StockDividend> findByTickerSymbolAndRecordDate(@Param("tickerSymbol") String tickerSymbol,
		@Param("recordDate") LocalDate recordDate);

	@Modifying
	@Query("delete from StockDividend sd where sd.stock.stockCode in :stockCodes")
	int deleteByStockCodes(@Param("stockCodes") Set<String> stockCodes);
}
