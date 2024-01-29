package codesquad.fineants.domain.watch_list;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.spring.api.watch_list.response.WatchListHasStockResponse;

public interface WatchListRepository extends JpaRepository<WatchList, Long> {
	List<WatchList> findByMember(Member member);

	@Query("select new codesquad.fineants.spring.api.watch_list.response.WatchListHasStockResponse(wl.id, wl.name, case when ws.id is not null then true else false end) " +
		"from WatchList wl left join wl.watchStocks ws on ws.stock.tickerSymbol = :tickerSymbol " +
		"where wl.member = :member")
	List<WatchListHasStockResponse> findWatchListsAndStockPresenceByMemberAndTickerSymbol(@Param("member") Member member,
		@Param("tickerSymbol") String tickerSymbol);
}
