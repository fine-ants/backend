package codesquad.fineants.domain.stock_target_price;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockTargetPriceRepository extends JpaRepository<StockTargetPrice, Long> {

	@Query("select s from StockTargetPrice s where s.stock.tickerSymbol = :tickerSymbol")
	List<StockTargetPrice> findAllByTickerSymbol(@Param("tickerSymbol") String tickerSymbol);

	@Query("select s from StockTargetPrice s where s.stock.tickerSymbol = :tickerSymbol and s.targetPrice = :targetPrice")
	Optional<StockTargetPrice> findByTickerSymbolAndTargetPrice(
		@Param("tickerSymbol") String tickerSymbol,
		@Param("targetPrice") Long targetPrice);

	@Modifying
	@Query("delete from StockTargetPrice s where s.id in (:targetPriceNotificationIds) and s.member.id = :memberId and s.stock.tickerSymbol = :tickerSymbol")
	void deleteAllByIdAndMemberIdAndTickerSymbol(
		@Param("targetPriceNotificationIds") List<Long> targetPriceNotificationIds,
		@Param("memberId") Long memberId,
		@Param("tickerSymbol") String tickerSymbol);
}
