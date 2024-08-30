package co.fineants.api.domain.watchlist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.watchlist.domain.dto.response.WatchListHasStockResponse;
import co.fineants.api.domain.watchlist.domain.entity.WatchList;

public interface WatchListRepository extends JpaRepository<WatchList, Long> {
	List<WatchList> findByMember(Member member);

	@Query(
		"select new co.fineants.domain.watchlist.domain.dto.response.WatchListHasStockResponse(wl.id, wl.name, case when ws.id is not null then true else false end) "
			+
			"from WatchList wl left join wl.watchStocks ws on ws.stock.tickerSymbol = :tickerSymbol " +
			"where wl.member = :member")
	List<WatchListHasStockResponse> findWatchListsAndStockPresenceByMemberAndTickerSymbol(
		@Param("member") Member member,
		@Param("tickerSymbol") String tickerSymbol);
}
