package codesquad.fineants.domain.stock_target_price;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockTargetPriceRepository extends JpaRepository<StockTargetPrice, Long> {

	@Query("select s from StockTargetPrice s where s.stock.tickerSymbol = :tickerSymbol and s.member.id = :memberId")
	Optional<StockTargetPrice> findByTickerSymbolAndMemberId(
		@Param("tickerSymbol") String tickerSymbol,
		@Param("memberId") Long memberId);

	@Query("select distinct s from StockTargetPrice s join fetch s.targetPriceNotifications t join fetch s.stock where s.member.id = :memberId")
	List<StockTargetPrice> findAllByMemberId(@Param("memberId") Long memberId);

	@Modifying
	@Query("delete from StockTargetPrice s where s.stock.tickerSymbol = :tickerSymbol and s.member.id = :memberId")
	int deleteByTickerSymbolAndMemberId(@Param("tickerSymbol") String tickerSymbol, @Param("memberId") Long memberId);
}
