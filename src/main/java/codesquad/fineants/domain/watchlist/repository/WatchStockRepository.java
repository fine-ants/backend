package codesquad.fineants.domain.watchlist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import codesquad.fineants.domain.watchlist.domain.entity.WatchList;
import codesquad.fineants.domain.watchlist.domain.entity.WatchStock;

public interface WatchStockRepository extends JpaRepository<WatchStock, Long> {
	List<WatchStock> findByWatchList(WatchList watchList);

	@EntityGraph(attributePaths = {"stock", "stock.stockDividends"})
	List<WatchStock> findWithStockAndDividendsByWatchList(WatchList watchList);

	void deleteByWatchListAndStock_TickerSymbolIn(WatchList watchList, List<String> tickerSymbols);
}
