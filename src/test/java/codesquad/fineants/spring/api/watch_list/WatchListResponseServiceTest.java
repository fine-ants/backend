package codesquad.fineants.spring.api.watch_list;

import static org.assertj.core.api.Assertions.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.domain.watch_list.WatchList;
import codesquad.fineants.domain.watch_list.WatchListRepository;
import codesquad.fineants.domain.watch_stock.WatchStock;
import codesquad.fineants.domain.watch_stock.WatchStockRepository;
import codesquad.fineants.spring.api.watch_list.request.CreateWatchListRequest;
import codesquad.fineants.spring.api.watch_list.request.CreateWatchStockRequest;
import codesquad.fineants.spring.api.watch_list.response.CreateWatchListResponse;
import codesquad.fineants.spring.api.watch_list.response.ReadWatchListsResponse;

@ActiveProfiles("test")
@SpringBootTest
class WatchListResponseServiceTest {

	@Autowired
	private WatchListService watchListService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private WatchListRepository watchListRepository;

	private Member member;
	@Autowired
	private WatchStockRepository watchStockRepository;
	@Autowired
	private StockRepository stockRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@BeforeEach
	void init() {
		Member member = Member.builder()
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.password("kim1234@")
			.provider("local")
			.build();
		this.member = memberRepository.save(member);
	}

	@AfterEach
	void tearDown() {
		watchStockRepository.deleteAllInBatch();
		watchListRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
	}

	@DisplayName("회원이 watchlist를 추가한다.")
	@Test
	void createWatchList() {
		// given
		AuthMember authMember = AuthMember.from(member);
		CreateWatchListRequest request = new CreateWatchListRequest("My WatchList");

		// when
		CreateWatchListResponse response = watchListService.createWatchList(authMember, request);

		// then
		assertThat(response.getWatchlistId()).isNotNull();
	}

	@DisplayName("회원이 watchlist 목록을 조회한다.")
	@Test
	void readWatchLists() {
		//given
		AuthMember authMember = AuthMember.from(member);
		WatchList watchList1 = watchListRepository.save(
			WatchList.builder()
				.name("My WatchList 1")
				.member(member)
				.build()
		);
		WatchList watchList2 = watchListRepository.save(
			WatchList.builder()
				.name("My WatchList 2")
				.member(member)
				.build()
		);

		//when
		ReadWatchListsResponse response = watchListService.readWatchLists(authMember);

		//then
		assertThat(response.getWatchLists().size()).isEqualTo(2);
	}

	@DisplayName("회원이 watchlist에 종목을 추가한다.")
	@Test
	void createWatchStock() {
		// given
		String tickerSymbol = "005930";
		Stock stock = stockRepository.save(
			Stock.builder()
				.tickerSymbol(tickerSymbol)
				.build()
		);

		AuthMember authMember = AuthMember.from(member);
		CreateWatchStockRequest request = new CreateWatchStockRequest(tickerSymbol);


		WatchList watchList = watchListRepository.save(WatchList.builder()
			.name("My WatchList")
			.member(member)
			.build());
		Long watchListId = watchList.getId();


		// when
		watchListService.createWatchStock(authMember, watchListId, request);

		// then
		assertThat(watchStockRepository.findByWatchList(watchList)).hasSize(1);
		assertThat(watchStockRepository.findByWatchList(watchList).get(0).getStock().getTickerSymbol()).isEqualTo(tickerSymbol);
	}

	@DisplayName("회원이 watchlist를 삭제한다.")
	@Test
	void deleteWatchList() {
		// given
		String tickerSymbol = "005930";
		Stock stock = stockRepository.save(
			Stock.builder()
				.tickerSymbol(tickerSymbol)
				.build()
		);

		AuthMember authMember = AuthMember.from(member);
		CreateWatchStockRequest request = new CreateWatchStockRequest(tickerSymbol);


		WatchList watchList = watchListRepository.save(WatchList.builder()
			.name("My WatchList")
			.member(member)
			.build());
		Long watchListId = watchList.getId();

		WatchStock watchStock = watchStockRepository.save(
			WatchStock.builder()
				.watchList(watchList)
				.stock(stock)
				.build()
		);

		// when
		watchListService.deleteWatchList(authMember, watchListId);

		// then
		assertThat(watchListRepository.findById(watchListId).isPresent()).isFalse();
		assertThat(watchStockRepository.findByWatchList(watchList)).hasSize(0);
	}

	@DisplayName("회원이 watchlist에서 종목을 삭제한다.")
	@Test
	void deleteWatchStock(){
		// given
		String tickerSymbol = "005930";
		Stock stock = stockRepository.save(
			Stock.builder()
				.tickerSymbol(tickerSymbol)
				.build()
		);

		AuthMember authMember = AuthMember.from(member);
		CreateWatchStockRequest request = new CreateWatchStockRequest(tickerSymbol);


		WatchList watchList = watchListRepository.save(WatchList.builder()
			.name("My WatchList")
			.member(member)
			.build());
		Long watchListId = watchList.getId();

		WatchStock watchStock = watchStockRepository.save(
			WatchStock.builder()
				.watchList(watchList)
				.stock(stock)
				.build()
		);
		Long stockId = watchStock.getId();

		// when
		watchListService.deleteWatchStock(authMember, watchListId, stockId);

		// then
		assertThat(watchStockRepository.findById(stockId).isPresent()).isFalse();
	}
}
