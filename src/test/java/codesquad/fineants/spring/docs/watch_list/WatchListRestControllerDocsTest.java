package codesquad.fineants.spring.docs.watch_list;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.spring.api.watch_list.controller.WatchListRestController;
import codesquad.fineants.spring.api.watch_list.request.CreateWatchListRequest;
import codesquad.fineants.spring.api.watch_list.response.CreateWatchListResponse;
import codesquad.fineants.spring.api.watch_list.response.ReadWatchListResponse;
import codesquad.fineants.spring.api.watch_list.response.ReadWatchListsResponse;
import codesquad.fineants.spring.api.watch_list.response.WatchListHasStockResponse;
import codesquad.fineants.spring.api.watch_list.service.WatchListService;
import codesquad.fineants.spring.docs.RestDocsSupport;
import codesquad.fineants.spring.util.ObjectMapperUtil;

public class WatchListRestControllerDocsTest extends RestDocsSupport {

	private final WatchListService service = Mockito.mock(WatchListService.class);

	@Override
	protected Object initController() {
		return new WatchListRestController(service);
	}

	@DisplayName("Watchlist 추가 API")
	@Test
	void createWatchList() throws Exception {
		// given
		given(service.createWatchList(
			ArgumentMatchers.any(AuthMember.class),
			ArgumentMatchers.any(CreateWatchListRequest.class)))
			.willReturn(CreateWatchListResponse.create(1L));

		Map<String, Object> body = Map.of(
			"name", "My watchlist"
		);

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/watchlists")
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심종목 목록이 추가되었습니다")))
			.andExpect(jsonPath("data.watchlistId").value(equalTo(1)))
			.andDo(
				document(
					"watchlist-create",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					requestFields(
						fieldWithPath("name").type(JsonFieldType.STRING)
							.description("관심 종목 목록 이름")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.OBJECT)
							.description("응답 데이터"),
						fieldWithPath("data.watchlistId").type(JsonFieldType.NUMBER)
							.description("관심 종목 리스트 등록 번호")
					)
				)
			);

	}

	@DisplayName("Watchlist 목록 조회 API")
	@Test
	void readWatchLists() throws Exception {
		// given
		Member member = createMember();

		given(service.readWatchLists(ArgumentMatchers.any(AuthMember.class)))
			.willReturn(ReadWatchListsResponse.from(List.of(
				createWatchList(member)
			)));

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/watchlists")
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심목록 목록 조회가 완료되었습니다")))
			.andExpect(jsonPath("data[0].id").value(equalTo(1)))
			.andExpect(jsonPath("data[0].name").value(equalTo("my watchlist 1")))
			.andDo(
				document(
					"watchlist-multiple-search",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.ARRAY)
							.description("응답 데이터"),
						fieldWithPath("data[].id").type(JsonFieldType.NUMBER)
							.description("관심 종목 리스트 등록 번호"),
						fieldWithPath("data[].name").type(JsonFieldType.STRING)
							.description("관심 종목 리스트 이름")
					)
				)
			);

	}

	@DisplayName("Watchlist 다수 삭제 API")
	@Test
	void deleteWatchLists() throws Exception {
		// given
		Map<String, Object> body = Map.of(
			"watchlistIds", List.of(1, 2)
		);

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/watchlists")
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심종목 목록이 삭제가 완료되었습니다")))
			.andDo(
				document(
					"watchlist-multiple-delete",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					requestFields(
						fieldWithPath("watchlistIds").type(JsonFieldType.ARRAY).description("관심종목 리스트 등록번호 리스트")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.NULL)
							.description("응답 데이터")
					)
				)
			);

	}

	@DisplayName("Watchlist 조회 API")
	@Test
	void readWatchList() throws Exception {
		// given
		Long watchlistId = 1L;

		given(service.readWatchList(ArgumentMatchers.any(AuthMember.class), anyLong()))
			.willReturn(ReadWatchListResponse.builder()
				.name("My WatchList")
				.watchStocks(List.of(
					ReadWatchListResponse.WatchStockResponse.builder()
						.id(1L)
						.companyName("삼성전자")
						.tickerSymbol("005930")
						.currentPrice(63800L)
						.dailyChange(1200L)
						.dailyChangeRate(1.85)
						.annualDividendYield(2.12)
						.sector("제조업")
						.dateAdded(LocalDateTime.of(2023, 12, 2, 15, 0, 0))
						.build()
				))
				.build());

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/watchlists/{watchlistId}", watchlistId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심종목 단일 목록 조회가 완료되었습니다")))
			.andDo(
				document(
					"watchlist-one-search",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					pathParameters(
						parameterWithName("watchlistId").description("관심종목 리스트 등록번호")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.OBJECT)
							.description("응답 데이터"),
						fieldWithPath("data.name").type(JsonFieldType.STRING)
							.description("관심 종목 리스트 이름"),
						fieldWithPath("data.watchStocks").type(JsonFieldType.ARRAY)
							.description("관심 종목 배열"),
						fieldWithPath("data.watchStocks[].id").type(JsonFieldType.NUMBER)
							.description("관심 종목 등록 번호"),
						fieldWithPath("data.watchStocks[].companyName").type(JsonFieldType.STRING)
							.description("관심 종목명"),
						fieldWithPath("data.watchStocks[].tickerSymbol").type(JsonFieldType.STRING)
							.description("관심 종목 티커 심볼"),
						fieldWithPath("data.watchStocks[].currentPrice").type(JsonFieldType.NUMBER)
							.description("관심 종목 현재가"),
						fieldWithPath("data.watchStocks[].dailyChange").type(JsonFieldType.NUMBER)
							.description("관심 종목 당일 변동 금액"),
						fieldWithPath("data.watchStocks[].dailyChangeRate").type(JsonFieldType.NUMBER)
							.description("관심 종목 당일 변동율"),
						fieldWithPath("data.watchStocks[].annualDividendYield").type(JsonFieldType.NUMBER)
							.description("관심 종목 연배당율"),
						fieldWithPath("data.watchStocks[].sector").type(JsonFieldType.STRING)
							.description("관심 종목 섹터"),
						fieldWithPath("data.watchStocks[].dateAdded").type(JsonFieldType.STRING)
							.description("관심 종목 추가 일자")
					)
				)
			);

	}

	@DisplayName("Watchlist 이름 변경 API")
	@Test
	void changeWatchListName() throws Exception {
		// given
		Long watchlistId = 1L;

		Map<String, Object> body = Map.of(
			"name", "My WatchList"
		);

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.put("/api/watchlists/{watchlistId}", watchlistId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심종목 목록 이름이 변경되었습니다")))
			.andDo(
				document(
					"watchlist_name-update",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					pathParameters(
						parameterWithName("watchlistId").description("관심종목 리스트 등록번호")
					),
					requestFields(
						fieldWithPath("name").type(JsonFieldType.STRING).description("관심 종목 리스트 이름")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.NULL)
							.description("응답 데이터")
					)
				)
			);

	}

	@DisplayName("Watchlist 종목 다수 추가 API")
	@Test
	void createWatchStocks() throws Exception {
		// given
		Long watchlistId = 1L;

		Map<String, Object> body = Map.of(
			"tickerSymbols", List.of("005930")
		);

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/watchlists/{watchlistId}/stock", watchlistId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심종목 목록에 종목이 추가되었습니다")))
			.andDo(
				document(
					"watchlist-stock-multiple-create",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					pathParameters(
						parameterWithName("watchlistId").description("관심종목 리스트 등록번호")
					),
					requestFields(
						fieldWithPath("tickerSymbols").type(JsonFieldType.ARRAY).description("종목 티커 심볼 리스트")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.NULL)
							.description("응답 데이터")
					)
				)
			);

	}

	@DisplayName("Watchlist 종목 다수 삭제 API")
	@Test
	void deleteWatchStocks() throws Exception {
		// given
		Long watchlistId = 1L;

		Map<String, Object> body = Map.of(
			"tickerSymbols", List.of("005930")
		);

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/watchlists/{watchlistId}/stock", watchlistId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심목록 종목이 삭제되었습니다")))
			.andDo(
				document(
					"watchlist-stock-multiple-delete",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					pathParameters(
						parameterWithName("watchlistId").description("관심종목 리스트 등록번호")
					),
					requestFields(
						fieldWithPath("tickerSymbols").type(JsonFieldType.ARRAY).description("종목 티커 심볼 리스트")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.NULL)
							.description("응답 데이터")
					)
				)
			);

	}

	@DisplayName("Watchlist 종목 단일 삭제 API")
	@Test
	void deleteWatchStock() throws Exception {
		// given
		Long watchlistId = 1L;
		String tickerSymbol = "005930";

		// when & then
		mockMvc.perform(
				RestDocumentationRequestBuilders.delete("/api/watchlists/{watchlistId}/stock/{tickerSymbol}", watchlistId,
						tickerSymbol)
					.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심목록 종목이 삭제되었습니다")))
			.andDo(
				document(
					"watchlist-stock-one-delete",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					pathParameters(
						parameterWithName("watchlistId").description("관심종목 리스트 등록번호"),
						parameterWithName("tickerSymbol").description("관심종목 티커 심볼")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.NULL)
							.description("응답 데이터")
					)
				)
			);

	}

	@DisplayName("Watchlist 종목 포함 여부 API")
	@Test
	void watchListHasStock() throws Exception {
		// given
		String tickerSymbol = "005930";

		given(service.hasStock(ArgumentMatchers.any(AuthMember.class), anyString()))
			.willReturn(List.of(
				WatchListHasStockResponse.create(1L, "My WatchList 1", true),
				WatchListHasStockResponse.create(2L, "My WatchList 2", false)
			));

		// when & then
		mockMvc.perform(
				RestDocumentationRequestBuilders.get("/api/watchlists/stockExists/{tickerSymbol}", tickerSymbol)
					.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심목록의 주식 포함 여부 조회가 완료되었습니다")))
			.andDo(
				document(
					"watchlist-stock-contain-get",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					pathParameters(
						parameterWithName("tickerSymbol").description("관심종목 티커 심볼")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.ARRAY)
							.description("응답 데이터"),
						fieldWithPath("data[].id").type(JsonFieldType.NUMBER)
							.description("관심 종목 등록 번호"),
						fieldWithPath("data[].name").type(JsonFieldType.STRING)
							.description("관심 종목 이름"),
						fieldWithPath("data[].hasStock").type(JsonFieldType.BOOLEAN)
							.description("관심 종목 포함 여부")
					)
				)
			);

	}
}
