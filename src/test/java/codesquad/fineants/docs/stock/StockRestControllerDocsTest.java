package codesquad.fineants.docs.stock;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import codesquad.fineants.docs.RestDocsSupport;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.kis.domain.dto.response.DividendItem;
import codesquad.fineants.domain.kis.repository.ClosingPriceRepository;
import codesquad.fineants.domain.kis.repository.CurrentPriceRedisRepository;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.stock.controller.StockRestController;
import codesquad.fineants.domain.stock.domain.dto.request.StockSearchRequest;
import codesquad.fineants.domain.stock.domain.dto.response.StockReloadResponse;
import codesquad.fineants.domain.stock.domain.dto.response.StockResponse;
import codesquad.fineants.domain.stock.domain.dto.response.StockSearchItem;
import codesquad.fineants.domain.stock.domain.entity.Market;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.service.StockService;
import codesquad.fineants.global.util.ObjectMapperUtil;

class StockRestControllerDocsTest extends RestDocsSupport {

	private final StockService service = Mockito.mock(StockService.class);

	@Override
	protected Object initController() {
		return new StockRestController(service);
	}

	@DisplayName("종목 검색 API")
	@Test
	void search() throws Exception {
		// given
		Stock stock = createSamsungStock();
		given(service.search(ArgumentMatchers.any(StockSearchRequest.class)))
			.willReturn(List.of(
				StockSearchItem.from(stock)
			));

		Map<String, Object> body = Map.of(
			"searchTerm", "삼성"
		);

		// when & then
		mockMvc.perform(post("/api/stocks/search")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("종목 검색이 완료되었습니다")))
			.andExpect(jsonPath("data[0].stockCode").value(equalTo(stock.getStockCode())))
			.andExpect(jsonPath("data[0].tickerSymbol").value(equalTo(stock.getTickerSymbol())))
			.andExpect(jsonPath("data[0].companyName").value(equalTo(stock.getCompanyName())))
			.andExpect(jsonPath("data[0].companyNameEng").value(equalTo(stock.getCompanyNameEng())))
			.andExpect(jsonPath("data[0].market").value(equalTo(stock.getMarket().name())))
			.andDo(
				document(
					"stock-search",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestFields(
						fieldWithPath("searchTerm").type(JsonFieldType.STRING).description("검색 키워드")
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
						fieldWithPath("data[].stockCode").type(JsonFieldType.STRING)
							.description("종목 코드"),
						fieldWithPath("data[].tickerSymbol").type(JsonFieldType.STRING)
							.description("종목 티커 심볼"),
						fieldWithPath("data[].companyName").type(JsonFieldType.STRING)
							.description("종목 이름"),
						fieldWithPath("data[].companyNameEng").type(JsonFieldType.STRING)
							.description("종목 이름 영문"),
						fieldWithPath("data[].market").type(JsonFieldType.STRING)
							.description("시장 종류")
					)
				)
			);
	}

	@DisplayName("종목 스크롤 검색 API")
	@Test
	void scrollSearch() throws Exception {
		// given
		String tickerSymbol = "";
		int size = 10;
		String keyword = "삼성";
		List<StockSearchItem> stockSearchItemList = createStockSearchItemList();
		given(service.search(tickerSymbol, size, keyword))
			.willReturn(stockSearchItemList);

		// when & then
		ResultActions resultActions = mockMvc.perform(get("/api/stocks/search")
			.queryParam("tickerSymbol", tickerSymbol)
			.queryParam("size", "10")
			.queryParam("keyword", keyword)
		);
		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("종목 검색이 완료되었습니다")))
			.andExpect(jsonPath("data").isArray());

		// Validate the data array content
		for (int i = 0; i < stockSearchItemList.size(); i++) {
			StockSearchItem expectedItem = stockSearchItemList.get(i);
			resultActions.andExpect(jsonPath("data[" + i + "].stockCode").value(equalTo(expectedItem.getStockCode())))
				.andExpect(jsonPath("data[" + i + "].tickerSymbol").value(equalTo(expectedItem.getTickerSymbol())))
				.andExpect(jsonPath("data[" + i + "].companyName").value(equalTo(expectedItem.getCompanyName())))
				.andExpect(jsonPath("data[" + i + "].companyNameEng").value(equalTo(expectedItem.getCompanyNameEng())))
				.andExpect(jsonPath("data[" + i + "].market").value(equalTo(expectedItem.getMarket().name())));
		}

		resultActions.andDo(
			document(
				"stock-scroll-search",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				queryParameters(
					parameterWithName("tickerSymbol").description("종목 리스트 중 가장 마지막 종목의 tickerSymbol").optional(),
					parameterWithName("size").description("종목 검색시 검색할 최대 종목 개수").optional(),
					parameterWithName("keyword").description("종목 검색 키워드").optional()
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
					fieldWithPath("data[].stockCode").type(JsonFieldType.STRING)
						.description("종목 코드"),
					fieldWithPath("data[].tickerSymbol").type(JsonFieldType.STRING)
						.description("종목 티커 심볼"),
					fieldWithPath("data[].companyName").type(JsonFieldType.STRING)
						.description("종목 이름"),
					fieldWithPath("data[].companyNameEng").type(JsonFieldType.STRING)
						.description("종목 이름 영문"),
					fieldWithPath("data[].market").type(JsonFieldType.STRING)
						.description("시장 종류")
				)
			)
		);
	}

	private List<StockSearchItem> createStockSearchItemList() {
		List<Stock> stocks = List.of(
			Stock.of("468510", "삼성기업인수목적9호", "SAMSUNG SPECIAL PURPOSE ACQUISITION 9 COMPANY", "KR7468510003", "금융",
				Market.KOSDAQ),
			Stock.of("448740", "삼성기업인수목적8호", "SAMSUNG SPECIAL PURPOSE ACQUISITION 8 COMPANY", "KR7448740001", "금융",
				Market.KOSDAQ),
			Stock.of("448730", "삼성FN리츠보통주", "SamsungFN REIT", "KR7448730002", "서비스업", Market.KOSPI),
			Stock.of("439250", "삼성기업인수목적7호", "SAMSUNG SPECIAL PURPOSE ACQUISITION 7 COMPANY", "KR7448730002", "금융",
				Market.KOSDAQ),
			Stock.of("425290", "삼성기업인수목적6호", "SAMSUNG SPECIAL PURPOSE ACQUISITION 6 COMPANY", "KR7425290004", "금융",
				Market.KOSDAQ),
			Stock.of("207940", "삼성바이오로직스보통주", "SAMSUNG BIOLOGICS", "KR7207940008", "의약품", Market.KOSPI),
			Stock.of("068290", "삼성출판사보통주", "SAMSUNG PUBLISHING", "KR7068290006", "서비스업", Market.KOSPI),
			Stock.of("032830", "삼성생명보험보통주", "Samsung Life Insurance", "KR7032830002", "보험", Market.KOSPI),
			Stock.of("029780", "삼성카드 보통주", "\\\"SAMSUNG CARD CO.", "KR7029780004", "기타금융", Market.NONE),
			Stock.of("02826K", "삼성물산1우선주(신형)", "SAMSUNG C&T CORPORATION(1PB)", "KR702826K016", "유통업", Market.KOSPI)
		);
		return stocks.stream()
			.map(StockSearchItem::from)
			.toList();
	}

	@DisplayName("종목 최신화 API")
	@Test
	void refreshStocks() throws Exception {
		// given
		Set<String> addedStocks = Set.of("123456", "234567");
		Set<String> deletedStocks = Set.of("222222", "333333");
		Set<DividendItem> addedDividends = Set.of(
			DividendItem.create(
				1L,
				"123456",
				Money.won(300),
				LocalDate.of(2024, 2, 1),
				LocalDate.of(2024, 1, 31),
				LocalDate.of(2024, 5, 1)
			)
		);
		given(service.reloadStocks())
			.willReturn(StockReloadResponse.create(addedStocks, deletedStocks, addedDividends));

		// when & then
		mockMvc.perform(post("/api/stocks/refresh")
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("종목 최신화가 완료되었습니다")))
			.andExpect(jsonPath("data.addedStocks").value(hasItem("123456")))
			.andExpect(jsonPath("data.addedStocks").value(hasItem("234567")))
			.andExpect(jsonPath("data.deletedStocks").value(hasItem("222222")))
			.andExpect(jsonPath("data.deletedStocks").value(hasItem("333333")))
			.andExpect(jsonPath("data.addedDividends[0].id").value(equalTo(1)))
			.andExpect(jsonPath("data.addedDividends[0].tickerSymbol").value(equalTo("123456")))
			.andExpect(jsonPath("data.addedDividends[0].dividend").value(equalTo(300)))
			.andExpect(jsonPath("data.addedDividends[0].recordDate").value(equalTo("2024-02-01")))
			.andExpect(jsonPath("data.addedDividends[0].exDividendDate").value(equalTo("2024-01-31")))
			.andExpect(jsonPath("data.addedDividends[0].paymentDate").value(equalTo("2024-05-01")))
			.andDo(
				document(
					"stock-refresh",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.OBJECT)
							.description("응답 데이터"),
						fieldWithPath("data.addedStocks").type(JsonFieldType.ARRAY)
							.description("상장된 종목 티커 심볼 리스트"),
						fieldWithPath("data.deletedStocks").type(JsonFieldType.ARRAY)
							.description("폐지된 종목 티커 심볼 리스트"),
						fieldWithPath("data.addedDividends").type(JsonFieldType.ARRAY)
							.description("신규 배당 일정"),
						fieldWithPath("data.addedDividends[].id").type(JsonFieldType.NUMBER)
							.description("배당 일정 등록 번호"),
						fieldWithPath("data.addedDividends[].tickerSymbol").type(JsonFieldType.STRING)
							.description("배당 일정 종목의 티커 심볼"),
						fieldWithPath("data.addedDividends[].dividend").type(JsonFieldType.NUMBER)
							.description("배당 금액"),
						fieldWithPath("data.addedDividends[].recordDate").type(JsonFieldType.STRING)
							.description("배정 일자"),
						fieldWithPath("data.addedDividends[].exDividendDate").type(JsonFieldType.STRING)
							.description("배당락 일자"),
						fieldWithPath("data.addedDividends[].paymentDate").type(JsonFieldType.STRING)
							.description("현금 지급 일자")
					)
				)
			);
	}

	@DisplayName("종목 상세 정보 조회 API")
	@Test
	void getStock() throws Exception {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		holding.addPurchaseHistory(createPurchaseHistory(holding, LocalDateTime.now()));
		portfolio.addHolding(holding);

		CurrentPriceRedisRepository currentPriceRedisRepository = Mockito.mock(CurrentPriceRedisRepository.class);
		ClosingPriceRepository closingPriceRepository = Mockito.mock(ClosingPriceRepository.class);

		Money currentPrice = Money.won(68000L);
		Money closingPrice = Money.won(80000L);
		given(currentPriceRedisRepository.fetchPriceBy(stock.getTickerSymbol()))
			.willReturn(Optional.of(currentPrice));
		given(closingPriceRepository.fetchPrice(stock.getTickerSymbol()))
			.willReturn(Optional.of(closingPrice));
		given(service.getDetailedStock(stock.getTickerSymbol()))
			.willReturn(StockResponse.create(
				stock.getStockCode(),
				stock.getTickerSymbol(),
				stock.getCompanyName(),
				stock.getCompanyNameEng(),
				stock.getMarket(),
				currentPrice,
				Money.won(12000),
				Percentage.from(0.2045),
				stock.getSector(),
				Money.won(6000),
				Percentage.from(0.10),
				List.of(1, 4)
			));

		String tickerSymbol = stock.getTickerSymbol();

		// when & then
		mockMvc.perform(get("/api/stocks/{tickerSymbol}", tickerSymbol))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("종목 상세정보 조회가 완료되었습니다")))
			.andExpect(jsonPath("data.stockCode").value(equalTo(stock.getStockCode())))
			.andExpect(jsonPath("data.tickerSymbol").value(equalTo(stock.getTickerSymbol())))
			.andExpect(jsonPath("data.companyName").value(equalTo(stock.getCompanyName())))
			.andExpect(jsonPath("data.companyNameEng").value(equalTo(stock.getCompanyNameEng())))
			.andExpect(jsonPath("data.market").value(equalTo(stock.getMarket().name())))
			.andExpect(jsonPath("data.currentPrice").value(equalTo(68000)))
			.andExpect(jsonPath("data.dailyChange").value(equalTo(12000)))
			.andExpect(jsonPath("data.dailyChangeRate").value(closeTo(20.45, 0.1)))
			.andExpect(jsonPath("data.sector").value(equalTo(stock.getSector())))
			.andExpect(jsonPath("data.annualDividend").value(equalTo(6000)))
			.andExpect(jsonPath("data.annualDividendYield").value(closeTo(10.00, 0.1)))
			.andExpect(jsonPath("data.dividendMonths").value(equalTo(List.of(1, 4))))
			.andDo(
				document(
					"stock-detail-search",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					pathParameters(
						parameterWithName("tickerSymbol").description("종목 티커 심볼")
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
						fieldWithPath("data.stockCode").type(JsonFieldType.STRING)
							.description("종목 코드"),
						fieldWithPath("data.tickerSymbol").type(JsonFieldType.STRING)
							.description("종목 티커 심볼"),
						fieldWithPath("data.companyName").type(JsonFieldType.STRING)
							.description("종목 이름"),
						fieldWithPath("data.companyNameEng").type(JsonFieldType.STRING)
							.description("종목 이름 영문"),
						fieldWithPath("data.market").type(JsonFieldType.STRING)
							.description("시장 종류"),
						fieldWithPath("data.currentPrice").type(JsonFieldType.NUMBER)
							.description("종목 현재가"),
						fieldWithPath("data.dailyChange").type(JsonFieldType.NUMBER)
							.description("당일 변동 금액"),
						fieldWithPath("data.dailyChangeRate").type(JsonFieldType.NUMBER)
							.description("당일 변동율"),
						fieldWithPath("data.sector").type(JsonFieldType.STRING)
							.description("종목 섹터"),
						fieldWithPath("data.annualDividend").type(JsonFieldType.NUMBER)
							.description("연배당금"),
						fieldWithPath("data.annualDividendYield").type(JsonFieldType.NUMBER)
							.description("연배당율"),
						fieldWithPath("data.dividendMonths").type(JsonFieldType.ARRAY)
							.description("배당월")
					)
				)
			);
	}
}
