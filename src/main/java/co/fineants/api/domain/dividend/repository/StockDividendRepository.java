package co.fineants.api.domain.dividend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;

public interface StockDividendRepository extends JpaRepository<StockDividend, Long> {

	@Query("select sd from StockDividend sd join fetch sd.stock s "
		+ "where sd.isDeleted = false "
		+ "order by s.tickerSymbol, sd.dividendDates.recordDate")
	List<StockDividend> findAllStockDividends();

	@Query("select sd from StockDividend sd join fetch sd.stock s "
		+ "where s.tickerSymbol = :tickerSymbol and sd.isDeleted = false "
		+ "order by s.tickerSymbol, sd.dividendDates.recordDate")
	List<StockDividend> findStockDividendsByTickerSymbol(@Param("tickerSymbol") String tickerSymbol);

	@Query("select sd from StockDividend sd join fetch sd.stock s "
		+ "where s.tickerSymbol = :tickerSymbol and sd.dividendDates.recordDate = :recordDate and sd.isDeleted = false "
		+ "order by s.tickerSymbol, sd.dividendDates.recordDate")
	Optional<StockDividend> findByTickerSymbolAndRecordDate(@Param("tickerSymbol") String tickerSymbol,
		@Param("recordDate") LocalDate recordDate);

	@Modifying
	@Query("update StockDividend sd set sd.isDeleted = true "
		+ "where sd.stock.tickerSymbol in :tickerSymbols")
	int deleteByTickerSymbols(@Param("tickerSymbols") Set<String> tickerSymbols);
}
