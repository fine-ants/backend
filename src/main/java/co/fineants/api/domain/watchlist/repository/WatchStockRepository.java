package co.fineants.api.domain.watchlist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.fineants.api.domain.watchlist.domain.entity.WatchList;
import co.fineants.api.domain.watchlist.domain.entity.WatchStock;

public interface WatchStockRepository extends JpaRepository<WatchStock, Long> {
	List<WatchStock> findByWatchList(WatchList watchList);

	@EntityGraph(attributePaths = {"stock", "stock.stockDividends"})
	List<WatchStock> findWithStockAndDividendsByWatchList(WatchList watchList);

	void deleteByWatchListAndStock_TickerSymbolIn(WatchList watchList, List<String> tickerSymbols);

	@Query("select w from WatchStock w where w.watchList.id = :watchListId and w.stock.tickerSymbol in (:tickerSymbols)")
	List<WatchStock> findByWatchListAndStock_TickerSymbolIn(@Param("watchListId") Long watchListId,
		@Param("tickerSymbols") List<String> tickerSymbols);
}
