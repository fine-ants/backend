package codesquad.fineants.domain.stock_target_price;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockTargetPriceRepository extends JpaRepository<StockTargetPrice, Long> {

	@Query("select s from StockTargetPrice s where s.stock.tickerSymbol = :tickerSymbol")
	List<StockTargetPrice> findAllByTickerSymbol(@Param("tickerSymbol") String tickerSymbol);

	@Query("select s from StockTargetPrice s where s.stock.tickerSymbol = :tickerSymbol and s.targetPrice = :targetPrice")
	Optional<StockTargetPrice> findByTickerSymbolAndTargetPrice(
		@Param("tickerSymbol") String tickerSymbol,
		@Param("targetPrice") Long targetPrice);
}
