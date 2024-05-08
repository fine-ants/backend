package codesquad.fineants.domain.stock_dividend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import codesquad.fineants.domain.stock_dividend.domain.entity.StockDividend;

public interface StockDividendRepository extends JpaRepository<StockDividend, Long> {

	@Query("select sd from StockDividend sd join fetch sd.stock s order by s.tickerSymbol, sd.recordDate")
	List<StockDividend> findAllStockDividends();

	@Modifying
	@Query("delete from StockDividend sd where sd.stock.tickerSymbol in :tickerSymbols")
	int deleteByTickerSymbols(@Param("tickerSymbols") List<String> tickerSymbols);
}
