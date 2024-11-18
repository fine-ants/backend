package co.fineants.api.domain.stock_target_price.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;

public interface StockTargetPriceRepository extends JpaRepository<StockTargetPrice, Long> {

	@Query("select s from StockTargetPrice s join fetch s.stock stock where stock.tickerSymbol = :tickerSymbol and s.member.id = :memberId")
	Optional<StockTargetPrice> findByTickerSymbolAndMemberId(
		@Param("tickerSymbol") String tickerSymbol,
		@Param("memberId") Long memberId);

	@Query("select distinct s from StockTargetPrice s join fetch s.stock stock join fetch s.targetPriceNotifications t where stock.tickerSymbol = :tickerSymbol and s.member.id = :memberId order by t.targetPrice asc")
	Optional<StockTargetPrice> findByTickerSymbolAndMemberIdUsingFetchJoin(
		@Param("tickerSymbol") String tickerSymbol,
		@Param("memberId") Long memberId);

	@Query("select distinct s from StockTargetPrice s join fetch s.targetPriceNotifications t join fetch s.stock join fetch s.member m join fetch m.notificationPreference where s.member.id = :memberId order by s.createAt asc, t.targetPrice asc")
	List<StockTargetPrice> findAllByMemberId(@Param("memberId") Long memberId);

	@Query("select distinct s from StockTargetPrice s join fetch s.targetPriceNotifications t join fetch s.stock stock join fetch s.member m join fetch m.notificationPreference where stock.tickerSymbol in (:tickerSymbols) order by s.createAt asc, t.targetPrice asc")
	List<StockTargetPrice> findAllByTickerSymbols(@Param("tickerSymbols") List<String> tickerSymbols);

	@Modifying
	@Query("delete from StockTargetPrice s where s.stock.tickerSymbol = :tickerSymbol and s.member.id = :memberId")
	int deleteByTickerSymbolAndMemberId(@Param("tickerSymbol") String tickerSymbol, @Param("memberId") Long memberId);

	@Modifying
	@Query("delete from StockTargetPrice s where s.member.id = :memberId")
	int deleteAllByMemberId(@Param("memberId") Long memberId);
}
