package codesquad.fineants.spring.api.watch_list;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalArgumentResolver;
import codesquad.fineants.spring.api.common.errors.handler.GlobalExceptionHandler;
import codesquad.fineants.spring.api.watch_list.controller.WatchListRestController;
import codesquad.fineants.spring.api.watch_list.request.ChangeWatchListNameRequest;
import codesquad.fineants.spring.api.watch_list.request.CreateWatchListRequest;
import codesquad.fineants.spring.api.watch_list.request.CreateWatchStockRequest;
import codesquad.fineants.spring.api.watch_list.request.DeleteWatchListsRequests;
import codesquad.fineants.spring.api.watch_list.request.DeleteWatchStocksRequest;
import codesquad.fineants.spring.api.watch_list.response.CreateWatchListResponse;
import codesquad.fineants.spring.api.watch_list.response.ReadWatchListResponse;
import codesquad.fineants.spring.api.watch_list.response.ReadWatchListsResponse;
import codesquad.fineants.spring.api.watch_list.response.WatchListHasStockResponse;
import codesquad.fineants.spring.api.watch_list.service.WatchListService;
import codesquad.fineants.spring.config.JacksonConfig;
import codesquad.fineants.spring.config.JpaAuditingConfiguration;
import codesquad.fineants.spring.config.SpringConfig;

@ActiveProfiles("test")
@WebMvcTest(controllers = WatchListRestController.class)
@Import(value = {SpringConfig.class, JacksonConfig.class})
@MockBean(JpaAuditingConfiguration.class)
class WatchListRestControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private WatchListRestController watchListRestController;

	@Autowired
	private GlobalExceptionHandler globalExceptionHandler;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AuthPrincipalArgumentResolver authPrincipalArgumentResolver;

	@MockBean
	private WatchListService watchListService;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(watchListRestController)
			.setControllerAdvice(globalExceptionHandler)
			.setCustomArgumentResolvers(authPrincipalArgumentResolver)
			.setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
			.alwaysDo(print())
			.build();
	}

	@DisplayName("사용자가 watchlist를 추가한다.")
	@Test
	void createWatchList() throws Exception {
		// given
		Member member = Member.builder()
			.id(1L)
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.provider("local")
			.password("kim1234@")
			.profileUrl("profileValue")
			.build();
		AuthMember authMember = AuthMember.from(member);

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("name", "My watchlist");
		String body = objectMapper.writeValueAsString(requestBodyMap);

		CreateWatchListResponse response = CreateWatchListResponse.create(1L);

		given(authPrincipalArgumentResolver.supportsParameter(any())).willReturn(true);
		given(authPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(authMember);
		given(watchListService.createWatchList(any(AuthMember.class), any(CreateWatchListRequest.class)))
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
		Member member = Member.builder()
			.id(1L)
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.provider("local")
			.password("kim1234@")
			.profileUrl("profileValue")
			.build();
		AuthMember authMember = AuthMember.from(member);
		given(authPrincipalArgumentResolver.supportsParameter(any())).willReturn(true);
		given(authPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(authMember);

		List<ReadWatchListsResponse> response = Arrays.asList(
			new ReadWatchListsResponse(1L, "My WatchList 1"),
			new ReadWatchListsResponse(2L, "My WatchList 2")
		);
		given(watchListService.readWatchLists(any(AuthMember.class))).willReturn(response);

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
		Member member = Member.builder()
			.id(1L)
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.provider("local")
			.password("kim1234@")
			.profileUrl("profileValue")
			.build();
		AuthMember authMember = AuthMember.from(member);
		given(authPrincipalArgumentResolver.supportsParameter(any())).willReturn(true);
		given(authPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(authMember);

		ReadWatchListResponse.WatchStockResponse watchStockResponse = ReadWatchListResponse.WatchStockResponse.builder()
			.id(1L)
			.companyName("삼성전자")
			.tickerSymbol("005930")
			.currentPrice(Money.from(68000))
			.dailyChange(Money.from(1200))
			.dailyChangeRate(1.85)
			.annualDividendYield(2.12)
			.sector("제조업")
			.dateAdded(LocalDateTime.of(2023, 12, 2, 15, 0, 0))
			.build();

		ReadWatchListResponse response = new ReadWatchListResponse("My Watchlist", List.of(watchStockResponse));

		given(watchListService.readWatchList(any(AuthMember.class), any(Long.class))).willReturn(response);

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
		Member member = Member.builder()
			.id(1L)
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.provider("local")
			.password("kim1234@")
			.profileUrl("profileValue")
			.build();
		AuthMember authMember = AuthMember.from(member);

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("tickerSymbols", List.of("005930"));
		String body = objectMapper.writeValueAsString(requestBodyMap);

		given(authPrincipalArgumentResolver.supportsParameter(any())).willReturn(true);
		given(authPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(authMember);
		doNothing().when(watchListService)
			.createWatchStocks(any(AuthMember.class), any(Long.class), any(CreateWatchStockRequest.class));

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
		Member member = Member.builder()
			.id(1L)
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.provider("local")
			.password("kim1234@")
			.profileUrl("profileValue")
			.build();
		AuthMember authMember = AuthMember.from(member);

		Map<String, Object> requestBodyMap = new HashMap<>();
		List<Long> watchListIds = new ArrayList<>();
		requestBodyMap.put("watchlistIds", watchListIds);
		String body = objectMapper.writeValueAsString(requestBodyMap);

		given(authPrincipalArgumentResolver.supportsParameter(any())).willReturn(true);
		given(authPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(authMember);
		doNothing().when(watchListService).deleteWatchLists(any(AuthMember.class), any(DeleteWatchListsRequests.class));

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
		Member member = Member.builder()
			.id(1L)
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.provider("local")
			.password("kim1234@")
			.profileUrl("profileValue")
			.build();
		AuthMember authMember = AuthMember.from(member);

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("tickerSymbols", List.of("005930"));
		String body = objectMapper.writeValueAsString(requestBodyMap);

		given(authPrincipalArgumentResolver.supportsParameter(any())).willReturn(true);
		given(authPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(authMember);
		doNothing().when(watchListService)
			.deleteWatchStocks(any(AuthMember.class), any(Long.class), any(DeleteWatchStocksRequest.class));

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
		Member member = Member.builder()
			.id(1L)
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.provider("local")
			.password("kim1234@")
			.profileUrl("profileValue")
			.build();
		AuthMember authMember = AuthMember.from(member);

		given(authPrincipalArgumentResolver.supportsParameter(any())).willReturn(true);
		given(authPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(authMember);
		doNothing().when(watchListService)
			.deleteWatchStocks(any(AuthMember.class), any(Long.class), any(DeleteWatchStocksRequest.class));

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
		Member member = Member.builder()
			.id(1L)
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.provider("local")
			.password("kim1234@")
			.profileUrl("profileValue")
			.build();
		AuthMember authMember = AuthMember.from(member);

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("name", "My watchlist");
		String body = objectMapper.writeValueAsString(requestBodyMap);

		given(authPrincipalArgumentResolver.supportsParameter(any())).willReturn(true);
		given(authPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(authMember);
		doNothing().when(watchListService)
			.changeWatchListName(any(AuthMember.class), any(Long.class), any(ChangeWatchListNameRequest.class));

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
		Member member = Member.builder()
			.id(1L)
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.provider("local")
			.password("kim1234@")
			.profileUrl("profileValue")
			.build();
		AuthMember authMember = AuthMember.from(member);
		given(authPrincipalArgumentResolver.supportsParameter(any())).willReturn(true);
		given(authPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(authMember);

		String tickerSymbol = "005930";
		List<WatchListHasStockResponse> response = List.of(
			WatchListHasStockResponse.create(1L, "My WatchList1", true),
			WatchListHasStockResponse.create(2L, "My WatchList2", false)
		);
		given(watchListService.hasStock(any(AuthMember.class), any(String.class))).willReturn(response);

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
