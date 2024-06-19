package codesquad.fineants.docs.stock;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import codesquad.fineants.docs.RestDocsSupport;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.kis.repository.ClosingPriceRepository;
import codesquad.fineants.domain.kis.repository.CurrentPriceRepository;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.stock.controller.StockRestController;
import codesquad.fineants.domain.stock.domain.dto.request.StockSearchRequest;
import codesquad.fineants.domain.stock.domain.dto.response.StockRefreshResponse;
import codesquad.fineants.domain.stock.domain.dto.response.StockResponse;
import codesquad.fineants.domain.stock.domain.dto.response.StockSearchItem;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.service.StockService;
import codesquad.fineants.global.util.ObjectMapperUtil;

public class StockRestControllerDocsTest extends RestDocsSupport {

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

	@DisplayName("종목 최신화 API")
	@Test
	void refreshStocks() throws Exception {
		// given
		Stock stock = createSamsungStock();
		given(service.refreshStocks())
			.willReturn(StockRefreshResponse.create(
				List.of("123456", "234567"),
				List.of("345678", "456789")
			));

		// when & then
		mockMvc.perform(post("/api/stocks/refresh")
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("종목 최신화가 완료되었습니다")))
			.andExpect(jsonPath("data.addedStocks").value(equalTo(List.of("123456", "234567"))))
			.andExpect(jsonPath("data.deletedStocks").value(equalTo(List.of("345678", "456789"))))
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
							.description("폐지된 종목 티커 심볼 리스트")
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

		CurrentPriceRepository currentPriceRepository = Mockito.mock(CurrentPriceRepository.class);
		ClosingPriceRepository closingPriceRepository = Mockito.mock(ClosingPriceRepository.class);

		Money currentPrice = Money.won(68000L);
		Money closingPrice = Money.won(80000L);
		given(currentPriceRepository.getCurrentPrice(stock.getTickerSymbol()))
			.willReturn(Optional.of(currentPrice));
		given(closingPriceRepository.getClosingPrice(stock.getTickerSymbol()))
			.willReturn(Optional.of(closingPrice));
		given(service.getStock(stock.getTickerSymbol()))
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
