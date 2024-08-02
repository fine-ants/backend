package codesquad.fineants.domain.stock.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import codesquad.fineants.domain.stock.domain.entity.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {

	Optional<Stock> findByTickerSymbol(String tickerSymbol);

	@Query("select s from Stock s where s.tickerSymbol in :tickerSymbols")
	List<Stock> findAllByTickerSymbols(@Param("tickerSymbols") List<String> tickerSymbols);

	@Query("select distinct s from Stock s join fetch s.stockDividends sd where s.tickerSymbol in (:tickerSymbols)")
	List<Stock> findAllWithDividends(@Param("tickerSymbols") List<String> tickerSymbols);

	@Query("select s from Stock s where s.stockCode like %:keyword% or s.tickerSymbol like %:keyword% or s.companyName like %:keyword% or s.companyNameEng like %:keyword%")
	List<Stock> search(@Param("keyword") String keyword);

	@Modifying
	@Query("update Stock s set s.isDeleted = true where s.stockCode in :stockCodes")
	int deleteAllByStockCodes(@Param("stockCodes") Set<String> stockCodes);
}
