package co.fineants.api.domain.watchlist.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import co.fineants.api.ControllerTestSupport;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.watchlist.domain.dto.request.ChangeWatchListNameRequest;
import co.fineants.api.domain.watchlist.domain.dto.request.CreateWatchListRequest;
import co.fineants.api.domain.watchlist.domain.dto.request.CreateWatchStockRequest;
import co.fineants.api.domain.watchlist.domain.dto.request.DeleteWatchStocksRequest;
import co.fineants.api.domain.watchlist.domain.dto.response.CreateWatchListResponse;
import co.fineants.api.domain.watchlist.domain.dto.response.ReadWatchListResponse;
import co.fineants.api.domain.watchlist.domain.dto.response.ReadWatchListsResponse;
import co.fineants.api.domain.watchlist.domain.dto.response.WatchListHasStockResponse;
import co.fineants.api.domain.watchlist.service.WatchListService;

@WebMvcTest(controllers = WatchListRestController.class)
class WatchListRestControllerTest extends ControllerTestSupport {

	@MockBean
	private WatchListService watchListService;

	@Override
	protected Object initController() {
		return new WatchListRestController(watchListService);
	}

	@DisplayName("사용자가 watchlist를 추가한다.")
	@Test
	void createWatchList() throws Exception {
		// given
		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("name", "My watchlist");
		String body = objectMapper.writeValueAsString(requestBodyMap);

		CreateWatchListResponse response = CreateWatchListResponse.create(1L);

		given(watchListService.createWatchList(anyLong(), any(CreateWatchListRequest.class)))
			.willReturn(response);

		// when & then
		mockMvc.perform(post("/api/watchlists")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심종목 목록이 추가되었습니다")))
			.andExpect(jsonPath("data.watchlistId").value(1));
	}

	@DisplayName("사용자가 watchlist 목록을 조회한다.")
	@Test
	void readWatchLists() throws Exception {
		// given
		List<ReadWatchListsResponse> response = Arrays.asList(
			ReadWatchListsResponse.create(1L, "My WatchList 1"),
			ReadWatchListsResponse.create(2L, "My WatchList 2")
		);
		given(watchListService.readWatchLists(anyLong())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/watchlists")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심목록 목록 조회가 완료되었습니다")))
			.andExpect(jsonPath("data[0].id").value(1L))
			.andExpect(jsonPath("data[0].name").value("My WatchList 1"))
			.andExpect(jsonPath("data[1].id").value(2L))
			.andExpect(jsonPath("data[1].name").value("My WatchList 2"));
	}

	@DisplayName("사용자가 watchlist 단일 조회를 한다.")
	@Test
	void readWatchList() throws Exception {
		// given
		ReadWatchListResponse.WatchStockResponse watchStockResponse = ReadWatchListResponse.WatchStockResponse.builder()
			.id(1L)
			.companyName("삼성전자")
			.tickerSymbol("005930")
			.currentPrice(Money.won(68000))
			.dailyChange(Money.won(1200))
			.dailyChangeRate(Percentage.from(0.0185))
			.annualDividendYield(Percentage.from(0.0212))
			.sector("제조업")
			.dateAdded(LocalDateTime.of(2023, 12, 2, 15, 0, 0))
			.build();

		ReadWatchListResponse response = new ReadWatchListResponse("My Watchlist", List.of(watchStockResponse));

		given(watchListService.readWatchList(anyLong(), any(Long.class))).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/watchlists/1")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심종목 단일 목록 조회가 완료되었습니다")))
			.andExpect(jsonPath("data.watchStocks[0].id").value(equalTo(1)))
			.andExpect(jsonPath("data.watchStocks[0].companyName").value(equalTo("삼성전자")))
			.andExpect(jsonPath("data.watchStocks[0].tickerSymbol").value(equalTo("005930")))
			.andExpect(jsonPath("data.watchStocks[0].currentPrice").value(equalTo(68000)))
			.andExpect(jsonPath("data.watchStocks[0].dailyChange").value(equalTo(1200)))
			.andExpect(jsonPath("data.watchStocks[0].dailyChangeRate").value(equalTo(1.85)))
			.andExpect(jsonPath("data.watchStocks[0].annualDividendYield").value(equalTo(2.12)))
			.andExpect(jsonPath("data.watchStocks[0].sector").value(equalTo("제조업")))
			.andExpect(jsonPath("data.watchStocks[0].dateAdded").value(equalTo("2023-12-02T15:00:00")));
	}

	@DisplayName("사용자가 watchlist에 종목을 추가한다.")
	@Test
	void createWatchStocks() throws Exception {
		// given
		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("tickerSymbols", List.of("005930"));
		String body = objectMapper.writeValueAsString(requestBodyMap);

		doNothing().when(watchListService)
			.createWatchStocks(anyLong(), any(Long.class), any(CreateWatchStockRequest.class));

		// when & then
		mockMvc.perform(post("/api/watchlists/1/stock")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심종목 목록에 종목이 추가되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자가 watchlist를 삭제한다.")
	@Test
	void deleteWatchLists() throws Exception {
		// given
		Map<String, Object> requestBodyMap = new HashMap<>();
		List<Long> watchListIds = new ArrayList<>();
		requestBodyMap.put("watchlistIds", watchListIds);
		String body = objectMapper.writeValueAsString(requestBodyMap);

		doNothing().when(watchListService).deleteWatchLists(anyLong(), anyList());

		// when & then
		mockMvc.perform(delete("/api/watchlists")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심종목 목록이 삭제가 완료되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자가 watchlist에서 종목을 여러개 삭제한다.")
	@Test
	void deleteWatchStocks() throws Exception {
		// given
		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("tickerSymbols", List.of("005930"));
		String body = objectMapper.writeValueAsString(requestBodyMap);

		doNothing().when(watchListService)
			.deleteWatchStocks(anyLong(), any(Long.class), any(DeleteWatchStocksRequest.class));

		// when & then
		mockMvc.perform(delete("/api/watchlists/1/stock")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심목록 종목이 삭제되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자가 watchlist에서 종목을 삭제한다.")
	@Test
	void deleteWatchStock() throws Exception {
		// given
		doNothing().when(watchListService)
			.deleteWatchStocks(anyLong(), any(Long.class), any(DeleteWatchStocksRequest.class));

		// when & then
		mockMvc.perform(delete("/api/watchlists/1/stock/\"005930\"")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심목록 종목이 삭제되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자가 watchlist 이름을 변경한다.")
	@Test
	void changeWatchListName() throws Exception {
		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("name", "My watchlist");
		String body = objectMapper.writeValueAsString(requestBodyMap);

		doNothing().when(watchListService)
			.changeWatchListName(anyLong(), any(Long.class), any(ChangeWatchListNameRequest.class));

		// when & then
		mockMvc.perform(put("/api/watchlists/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심종목 목록 이름이 변경되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자가 모든 watchlist에 대해 tickerSymbol 보유 여부롤 조회한다.")
	@Test
	void watchListHasStock() throws Exception {
		// given
		String tickerSymbol = "005930";
		List<WatchListHasStockResponse> response = List.of(
			WatchListHasStockResponse.create(1L, "My WatchList1", true),
			WatchListHasStockResponse.create(2L, "My WatchList2", false)
		);
		given(watchListService.hasStock(anyLong(), any(String.class))).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/watchlists/stockExists/" + tickerSymbol)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심목록의 주식 포함 여부 조회가 완료되었습니다")))
			.andExpect(jsonPath("data[0].id").value(equalTo(1)))
			.andExpect(jsonPath("data[0].name").value(equalTo("My WatchList1")))
			.andExpect(jsonPath("data[0].hasStock").value(equalTo(true)))
			.andExpect(jsonPath("data[1].id").value(equalTo(2)))
			.andExpect(jsonPath("data[1].name").value(equalTo("My WatchList2")))
			.andExpect(jsonPath("data[1].hasStock").value(equalTo(false)));
	}
}
