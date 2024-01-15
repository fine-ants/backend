package codesquad.fineants.domain.watch_stock;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import codesquad.fineants.domain.watch_list.WatchList;

public interface WatchStockRepository extends JpaRepository<WatchStock, Long> {
	List<WatchStock> findByWatchList(WatchList watchList);

	@EntityGraph(attributePaths = {"stock", "stock.stockDividends"})
	List<WatchStock> findWithStockAndDividendsByWatchList(WatchList watchList);

	void deleteByWatchListAndStock_TickerSymbolIn(WatchList watchList, List<String> tickerSymbols);
}
