package codesquad.fineants.domain.watchlist.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.dividend.domain.entity.StockDividend;
import codesquad.fineants.domain.dividend.repository.StockDividendRepository;
import codesquad.fineants.domain.kis.client.KisCurrentPrice;
import codesquad.fineants.domain.kis.domain.dto.response.KisClosingPrice;
import codesquad.fineants.domain.kis.repository.ClosingPriceRepository;
import codesquad.fineants.domain.kis.repository.CurrentPriceRepository;
import codesquad.fineants.domain.kis.service.KisService;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.stock.domain.entity.Market;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockRepository;
import codesquad.fineants.domain.watchlist.domain.dto.request.ChangeWatchListNameRequest;
import codesquad.fineants.domain.watchlist.domain.dto.request.CreateWatchListRequest;
import codesquad.fineants.domain.watchlist.domain.dto.request.CreateWatchStockRequest;
import codesquad.fineants.domain.watchlist.domain.dto.request.DeleteWatchListsRequests;
import codesquad.fineants.domain.watchlist.domain.dto.request.DeleteWatchStocksRequest;
import codesquad.fineants.domain.watchlist.domain.dto.response.CreateWatchListResponse;
import codesquad.fineants.domain.watchlist.domain.dto.response.ReadWatchListResponse;
import codesquad.fineants.domain.watchlist.domain.dto.response.ReadWatchListsResponse;
import codesquad.fineants.domain.watchlist.domain.dto.response.WatchListHasStockResponse;
import codesquad.fineants.domain.watchlist.domain.entity.WatchList;
import codesquad.fineants.domain.watchlist.domain.entity.WatchStock;
import codesquad.fineants.domain.watchlist.repository.WatchListRepository;
import codesquad.fineants.domain.watchlist.repository.WatchStockRepository;
import codesquad.fineants.global.errors.errorcode.WatchListErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;

class WatchListServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private WatchListService watchListService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private WatchListRepository watchListRepository;

	@Autowired
	private CurrentPriceRepository currentPriceRepository;

	@Autowired
	private ClosingPriceRepository closingPriceRepository;

	@Autowired
	private WatchStockRepository watchStockRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockDividendRepository stockDividendRepository;

	@MockBean
	private KisService kisService;

	@AfterEach
	void tearDown() {
		watchStockRepository.deleteAllInBatch();
		watchListRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
		stockRepository.deleteAllInBatch();
	}

	@DisplayName("회원이 watchlist를 추가한다.")
	@Test
	void createWatchList() {
		// given
		Member member = memberRepository.save(createMember());
		CreateWatchListRequest request = new CreateWatchListRequest("My WatchList");

		// when
		CreateWatchListResponse response = watchListService.createWatchList(member.getId(), request);

		// then
		assertThat(response.getWatchlistId()).isNotNull();
	}

	@DisplayName("회원이 watchlist 목록을 조회한다.")
	@Test
	void readWatchLists() {
		//given
		Member member = memberRepository.save(createMember());
		watchListRepository.save(
			WatchList.builder()
				.name("My WatchList 1")
				.member(member)
				.build()
		);
		watchListRepository.save(
			WatchList.builder()
				.name("My WatchList 2")
				.member(member)
				.build()
		);

		//when
		List<ReadWatchListsResponse> response = watchListService.readWatchLists(member.getId());

		//then
		assertThat(response.size()).isEqualTo(2);
	}

	@DisplayName("회원이 watchlist 단일 목록을 조회한다.")
	@Test
	void readWatchList() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(
			Stock.builder()
				.companyName("삼성전자보통주")
				.tickerSymbol("005930")
				.companyNameEng("SamsungElectronics")
				.stockCode("KR7005930003")
				.sector("전기전자")
				.market(Market.KOSPI)
				.build()
		);
		stockDividendRepository.save(
			StockDividend.builder()
				.exDividendDate(LocalDate.now())
				.recordDate(LocalDate.now())
				.paymentDate(LocalDate.now())
				.dividend(Money.won(362L))
				.stock(stock)
				.build()
		);

		WatchList watchList = watchListRepository.save(
			WatchList.builder()
				.name("My WatchList 1")
				.member(member)
				.build()
		);
		watchStockRepository.save(
			WatchStock.builder()
				.stock(stock)
				.watchList(watchList)
				.build()
		);

		currentPriceRepository.addCurrentPrice(KisCurrentPrice.create("005930", 77000L));
		closingPriceRepository.addPrice(KisClosingPrice.create("005930", 77000L));

		// when
		ReadWatchListResponse response = watchListService.readWatchList(member.getId(), watchList.getId());

		// then
		assertThat(response.getName()).isEqualTo(watchList.getName());
		assertThat(response)
			.extracting(ReadWatchListResponse::getWatchStocks)
			.asList()
			.extracting("companyName", "tickerSymbol", "currentPrice", "dailyChange", "dailyChangeRate",
				"annualDividendYield", "sector")
			.usingComparatorForType(Money::compareTo, Money.class)
			.usingComparatorForType(Percentage::compareTo, Percentage.class)
			.containsExactlyInAnyOrder(
				tuple(
					"삼성전자보통주",
					"005930",
					Money.won(77000),
					Money.zero(),
					Percentage.zero(),
					Percentage.from(0.0047),
					"전기전자"
				)
			);
	}

	@DisplayName("회원이 watchlist에 종목을 추가한다.")
	@Test
	void createWatchStocks() {
		// given
		String tickerSymbol = "005930";
		stockRepository.save(
			Stock.builder()
				.tickerSymbol(tickerSymbol)
				.market(Market.KOSPI)
				.build()
		);

		Member member = memberRepository.save(createMember());
		CreateWatchStockRequest request = new CreateWatchStockRequest(List.of(tickerSymbol));

		WatchList watchList = watchListRepository.save(WatchList.builder()
			.name("My WatchList")
			.member(member)
			.build());
		Long watchListId = watchList.getId();

		// when
		watchListService.createWatchStocks(member.getId(), watchListId, request);

		// then
		assertThat(watchStockRepository.findByWatchList(watchList)).hasSize(1);
		assertThat(watchStockRepository.findByWatchList(watchList).get(0).getStock().getTickerSymbol()).isEqualTo(
			tickerSymbol);
	}

	@DisplayName("사용자는 한 관심 종목에 이미 추가된 종목을 추가할 수 없다")
	@Test
	void createWatchStocks_whenAlreadyStock_thenNotSaveStock() {
		// given
		Member member = memberRepository.save(createMember());
		stockRepository.save(createStock());

		CreateWatchListResponse watchlist1 = watchListService.createWatchList(member.getId(),
			new CreateWatchListRequest("watchlist1"));
		CreateWatchStockRequest request = new CreateWatchStockRequest(List.of("005930"));
		watchListService.createWatchStocks(member.getId(), watchlist1.getWatchlistId(), request);
		// when
		Throwable throwable = catchThrowable(
			() -> watchListService.createWatchStocks(member.getId(), watchlist1.getWatchlistId(), request));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage(WatchListErrorCode.ALREADY_WATCH_STOCK.getMessage());
	}

	@DisplayName("회원이 watchlist를 삭제한다.")
	@Test
	void deleteWatchLists() {
		// given
		String tickerSymbol = "005930";
		Stock stock = stockRepository.save(
			Stock.builder()
				.tickerSymbol(tickerSymbol)
				.market(Market.KOSPI)
				.build()
		);

		Member member = memberRepository.save(createMember());

		WatchList watchList = watchListRepository.save(WatchList.builder()
			.name("My WatchList")
			.member(member)
			.build());
		Long watchListId = watchList.getId();

		watchStockRepository.save(
			WatchStock.builder()
				.watchList(watchList)
				.stock(stock)
				.build()
		);

		// when
		watchListService.deleteWatchLists(member.getId(), new DeleteWatchListsRequests(List.of(watchListId)));

		// then
		assertThat(watchListRepository.findById(watchListId).isPresent()).isFalse();
		assertThat(watchStockRepository.findByWatchList(watchList)).hasSize(0);
	}

	@DisplayName("회원이 watchlist에서 종목을 여러개 삭제한다.")
	@Test
	void deleteWatchStocks() {
		// given
		String tickerSymbol = "005930";
		Stock stock = stockRepository.save(
			Stock.builder()
				.tickerSymbol(tickerSymbol)
				.market(Market.KOSPI)
				.build()
		);

		Member member = memberRepository.save(createMember());

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
		Long watchStockId = watchStock.getId();

		DeleteWatchStocksRequest request = new DeleteWatchStocksRequest(
			List.of(watchStock.getStock().getTickerSymbol()));

		// when
		watchListService.deleteWatchStocks(member.getId(), watchListId, request);

		// then
		assertThat(watchStockRepository.findById(watchStockId).isPresent()).isFalse();
	}

	@DisplayName("회원이 watchlist에서 종목을 삭제한다.")
	@Test
	void deleteWatchStock() {
		// given
		String tickerSymbol = "005930";
		Stock stock = stockRepository.save(
			Stock.builder()
				.tickerSymbol(tickerSymbol)
				.market(Market.KOSPI)
				.build()
		);

		Member member = memberRepository.save(createMember());

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
		Long watchStockId = watchStock.getId();

		// when
		watchListService.deleteWatchStock(member.getId(), watchListId, stock.getTickerSymbol());

		// then
		assertThat(watchStockRepository.findById(watchStockId).isPresent()).isFalse();
	}

	@DisplayName("회원이 watchlist의 이름을 수정한다.")
	@Test
	void changeWatchListName() {
		// given
		Member member = memberRepository.save(createMember());

		WatchList watchList = watchListRepository.save(WatchList.builder()
			.name("My WatchList")
			.member(member)
			.build());
		Long watchListId = watchList.getId();

		String name = "New Name";
		ChangeWatchListNameRequest request = new ChangeWatchListNameRequest(name);

		// when
		watchListService.changeWatchListName(member.getId(), watchListId, request);

		// then
		WatchList findWatchList = watchListRepository.findById(watchListId).orElseThrow();
		assertThat(findWatchList.getName()).isEqualTo(name);
	}

	@DisplayName("회원이 watchlist들이 주식을 포함하고 있는지 조회한다.")
	@Test
	void hasStock() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(
			Stock.builder()
				.companyName("삼성전자보통주")
				.tickerSymbol("005930")
				.companyNameEng("SamsungElectronics")
				.stockCode("KR7005930003")
				.sector("전기전자")
				.market(Market.KOSPI)
				.build()
		);
		WatchList watchList1 = watchListRepository.save(WatchList.builder()
			.name("My WatchList 1")
			.member(member)
			.build());

		watchStockRepository.save(
			WatchStock.builder()
				.watchList(watchList1)
				.stock(stock)
				.build()
		);

		WatchList watchList2 = watchListRepository.save(WatchList.builder()
			.name("My WatchList 2")
			.member(member)
			.build());

		// when
		List<WatchListHasStockResponse> responseList = watchListService.hasStock(member.getId(),
			stock.getTickerSymbol());

		// then
		assertThat(responseList.size()).isEqualTo(2);

		boolean hasStockForWatchList1 = false;
		boolean hasStockForWatchList2 = false;

		for (WatchListHasStockResponse response : responseList) {
			if (response.getId() == watchList1.getId()) {
				hasStockForWatchList1 = response.isHasStock();
			} else if (response.getId() == watchList2.getId()) {
				hasStockForWatchList2 = response.isHasStock();
			}
		}

		assertThat(hasStockForWatchList1).isTrue();
		assertThat(hasStockForWatchList2).isFalse();
	}

	private Stock createStock() {
		return Stock.builder()
			.tickerSymbol("005930")
			.stockCode("KR7005930003")
			.companyName("삼성전자보통주")
			.companyNameEng("SamsungElectronics")
			.market(Market.KOSPI)
			.build();
	}
}
