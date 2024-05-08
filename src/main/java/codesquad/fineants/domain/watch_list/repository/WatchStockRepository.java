package codesquad.fineants.domain.watch_list.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import codesquad.fineants.domain.watch_list.domain.entity.WatchList;
import codesquad.fineants.domain.watch_list.domain.entity.WatchStock;

public interface WatchStockRepository extends JpaRepository<WatchStock, Long> {
	List<WatchStock> findByWatchList(WatchList watchList);

	@EntityGraph(attributePaths = {"stock", "stock.stockDividends"})
	List<WatchStock> findWithStockAndDividendsByWatchList(WatchList watchList);

	void deleteByWatchListAndStock_TickerSymbolIn(WatchList watchList, List<String> tickerSymbols);
}
