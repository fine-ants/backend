package codesquad.fineants.spring.docs.holding;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock_dividend.StockDividend;
import codesquad.fineants.spring.api.portfolio_stock.controller.PortfolioStockRestController;
import codesquad.fineants.spring.api.portfolio_stock.manager.SseEmitterManager;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingsResponse;
import codesquad.fineants.spring.api.portfolio_stock.service.PortfolioStockService;
import codesquad.fineants.spring.docs.RestDocsSupport;

public class PortfolioStockRestControllerDocsTest extends RestDocsSupport {

	private PortfolioStockService service = Mockito.mock(PortfolioStockService.class);
	private SseEmitterManager manager = Mockito.mock(SseEmitterManager.class);

	@Override
	protected Object initController() {
		return new PortfolioStockRestController(service, manager);
	}

	@DisplayName("포트폴리오 종목 조회 API")
	@Test
	void readMyPortfolioStocks() throws Exception {
		// given
		Member member = createMember();
		Portfolio portfolio = createPortfolio(member);
		Stock stock = createStock();
		List<StockDividend> stockDividends = createStockDividendWith(stock);
		stockDividends.forEach(stock::addStockDividend);
		PortfolioHolding portfolioHolding = createPortfolioHolding(portfolio, stock);
		portfolioHolding.addPurchaseHistory(
			createPurchaseHistory(portfolioHolding, LocalDateTime.of(2023, 11, 1, 9, 30, 0)));
		portfolio.addPortfolioStock(portfolioHolding);
		PortfolioGainHistory history = createEmptyPortfolioGainHistory();

		Map<String, Long> lastDayClosingPriceMap = Map.of("005930", 50000L);
		PortfolioHoldingsResponse mockResponse = PortfolioHoldingsResponse.of(portfolio, history,
			List.of(portfolioHolding),
			lastDayClosingPriceMap);

		given(service.readMyPortfolioStocks(anyLong())).willReturn(mockResponse);
		// when & then
		ResultActions resultActions = mockMvc.perform(get("/api/portfolio/{portfolioId}/holdings", portfolio.getId()))
			.andExpect(status().isOk());

		resultActions
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 상세 정보 및 포트폴리오 종목 목록 조회가 완료되었습니다")))
			.andExpect(jsonPath("data.portfolioDetails.id").value(equalTo(1)))
			.andExpect(jsonPath("data.portfolioDetails.securitiesFirm").value(equalTo("토스")))
			.andExpect(jsonPath("data.portfolioDetails.name").value(equalTo("내꿈은 워렌버핏")))
			.andExpect(jsonPath("data.portfolioDetails.budget").value(equalTo(1000000)))
			.andExpect(jsonPath("data.portfolioDetails.targetGain").value(equalTo(1500000)))
			.andExpect(jsonPath("data.portfolioDetails.targetReturnRate").value(equalTo(50)))
			.andExpect(jsonPath("data.portfolioDetails.maximumLoss").value(equalTo(900000)))
			.andExpect(jsonPath("data.portfolioDetails.maximumLossRate").value(equalTo(10)))
			.andExpect(jsonPath("data.portfolioDetails.currentValuation").value(equalTo(180000)))
			.andExpect(jsonPath("data.portfolioDetails.investedAmount").value(equalTo(150000)))
			.andExpect(jsonPath("data.portfolioDetails.totalGain").value(equalTo(30000)))
			.andExpect(jsonPath("data.portfolioDetails.totalGainRate").value(equalTo(20)))
			.andExpect(jsonPath("data.portfolioDetails.balance").value(equalTo(850000)))
			.andExpect(jsonPath("data.portfolioDetails.annualDividend").value(equalTo(4332)))
			.andExpect(jsonPath("data.portfolioDetails.annualDividendYield").value(equalTo(2.00)))
			.andExpect(jsonPath("data.portfolioDetails.annualInvestmentDividendYield").value(equalTo(2.00)))
			.andExpect(jsonPath("data.portfolioDetails.provisionalLossBalance").value(equalTo(0)))
			.andExpect(jsonPath("data.portfolioDetails.targetGainNotification").value(equalTo(false)))
			.andExpect(jsonPath("data.portfolioDetails.maxLossNotification").value(equalTo(false)));

		resultActions
			.andExpect(jsonPath("data.portfolioHoldings[0].companyName").value(equalTo("삼성전자보통주")))
			.andExpect(jsonPath("data.portfolioHoldings[0].tickerSymbol").value(equalTo("005930")))
			.andExpect(jsonPath("data.portfolioHoldings[0].portfolioHoldingId").value(equalTo(1)))
			.andExpect(jsonPath("data.portfolioHoldings[0].currentValuation").value(equalTo(180000)))
			.andExpect(jsonPath("data.portfolioHoldings[0].currentPrice").value(equalTo(60000)))
			.andExpect(jsonPath("data.portfolioHoldings[0].averageCostPerShare").value(equalTo(50000.00)))
			.andExpect(jsonPath("data.portfolioHoldings[0].numShares").value(equalTo(3)))
			.andExpect(jsonPath("data.portfolioHoldings[0].dailyChange").value(equalTo(10000)))
			.andExpect(jsonPath("data.portfolioHoldings[0].dailyChangeRate").value(equalTo(20)))
			.andExpect(jsonPath("data.portfolioHoldings[0].totalGain").value(equalTo(30000)))
			.andExpect(jsonPath("data.portfolioHoldings[0].totalReturnRate").value(equalTo(20)))
			.andExpect(jsonPath("data.portfolioHoldings[0].annualDividend").value(equalTo(4332)))
			.andExpect(jsonPath("data.portfolioHoldings[0].annualDividendYield").value(equalTo(2.00)));

		resultActions
			.andExpect(jsonPath("data.portfolioHoldings[0].purchaseHistory[0].purchaseHistoryId").value(equalTo(1)))
			.andExpect(jsonPath("data.portfolioHoldings[0].purchaseHistory[0].purchaseDate").value(
				equalTo("2023-11-01T09:30:00")))
			.andExpect(jsonPath("data.portfolioHoldings[0].purchaseHistory[0].numShares").value(equalTo(3)))
			.andExpect(
				jsonPath("data.portfolioHoldings[0].purchaseHistory[0].purchasePricePerShare").value(equalTo(50000.00)))
			.andExpect(jsonPath("data.portfolioHoldings[0].purchaseHistory[0].memo").value(equalTo("첫구매")));

		resultActions.andDo(
			document(
				"holding-search",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				pathParameters(
					parameterWithName("portfolioId").description("포트폴리오 등록번호")
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
					fieldWithPath("data.portfolioDetails.id").type(JsonFieldType.NUMBER)
						.description("포트폴리오 등록번호"),
					fieldWithPath("data.portfolioDetails.securitiesFirm").type(JsonFieldType.STRING)
						.description("증권사"),
					fieldWithPath("data.portfolioDetails.name").type(JsonFieldType.STRING)
						.description("포트폴리오 이름"),
					fieldWithPath("data.portfolioDetails.budget").type(JsonFieldType.NUMBER)
						.description("예산"),
					fieldWithPath("data.portfolioDetails.targetGain").type(JsonFieldType.NUMBER)
						.description("목표 수익 금액"),
					fieldWithPath("data.portfolioDetails.targetReturnRate").type(JsonFieldType.NUMBER)
						.description("목표 수익율"),
					fieldWithPath("data.portfolioDetails.maximumLoss").type(JsonFieldType.NUMBER)
						.description("최대 손실 금액"),
					fieldWithPath("data.portfolioDetails.maximumLossRate").type(JsonFieldType.NUMBER)
						.description("최대 손실율"),
					fieldWithPath("data.portfolioDetails.currentValuation").type(JsonFieldType.NUMBER)
						.description("현재 평가 금액"),
					fieldWithPath("data.portfolioDetails.investedAmount").type(JsonFieldType.NUMBER)
						.description("총 투자 금액"),
					fieldWithPath("data.portfolioDetails.totalGain").type(JsonFieldType.NUMBER)
						.description("총 손익"),
					fieldWithPath("data.portfolioDetails.totalGainRate").type(JsonFieldType.NUMBER)
						.description("총 손익율"),
					fieldWithPath("data.portfolioDetails.dailyGain").type(JsonFieldType.NUMBER)
						.description("일일 손익 금액"),
					fieldWithPath("data.portfolioDetails.dailyGainRate").type(JsonFieldType.NUMBER)
						.description("일일 손익율"),
					fieldWithPath("data.portfolioDetails.balance").type(JsonFieldType.NUMBER)
						.description("잔고"),
					fieldWithPath("data.portfolioDetails.annualDividend").type(JsonFieldType.NUMBER)
						.description("연배당금"),
					fieldWithPath("data.portfolioDetails.annualDividendYield").type(JsonFieldType.NUMBER)
						.description("연배당율"),
					fieldWithPath("data.portfolioDetails.annualInvestmentDividendYield").type(JsonFieldType.NUMBER)
						.description("투자 대비 연간 배당율"),
					fieldWithPath("data.portfolioDetails.provisionalLossBalance").type(JsonFieldType.NUMBER)
						.description("잠정 손실 잔고"),
					fieldWithPath("data.portfolioDetails.targetGainNotification").type(JsonFieldType.BOOLEAN)
						.description("목표 수익 금액 알림 여부"),
					fieldWithPath("data.portfolioDetails.maxLossNotification").type(JsonFieldType.BOOLEAN)
						.description("최대 손실 금액 알림 여부"),

					fieldWithPath("data.portfolioHoldings[].companyName").type(JsonFieldType.STRING)
						.description("종목명"),
					fieldWithPath("data.portfolioHoldings[].tickerSymbol").type(JsonFieldType.STRING)
						.description("종목 티커 심볼"),
					fieldWithPath("data.portfolioHoldings[].portfolioHoldingId").type(JsonFieldType.NUMBER)
						.description("포트폴리오 종목 등록번호"),
					fieldWithPath("data.portfolioHoldings[].currentValuation").type(JsonFieldType.NUMBER)
						.description("평가금액"),
					fieldWithPath("data.portfolioHoldings[].currentPrice").type(JsonFieldType.NUMBER)
						.description("현재가"),
					fieldWithPath("data.portfolioHoldings[].averageCostPerShare").type(JsonFieldType.NUMBER)
						.description("매입가"),
					fieldWithPath("data.portfolioHoldings[].numShares").type(JsonFieldType.NUMBER)
						.description("종목 개수"),
					fieldWithPath("data.portfolioHoldings[].dailyChange").type(JsonFieldType.NUMBER)
						.description("일일 변동 금액"),
					fieldWithPath("data.portfolioHoldings[].dailyChangeRate").type(JsonFieldType.NUMBER)
						.description("일일 변동율"),
					fieldWithPath("data.portfolioHoldings[].totalGain").type(JsonFieldType.NUMBER)
						.description("총손익"),
					fieldWithPath("data.portfolioHoldings[].totalReturnRate").type(JsonFieldType.NUMBER)
						.description("총손익율"),
					fieldWithPath("data.portfolioHoldings[].annualDividend").type(JsonFieldType.NUMBER)
						.description("연배당금"),
					fieldWithPath("data.portfolioHoldings[].annualDividendYield").type(JsonFieldType.NUMBER)
						.description("연배당율"),

					fieldWithPath("data.portfolioHoldings[].purchaseHistory[].purchaseHistoryId")
						.type(JsonFieldType.NUMBER)
						.description("매입 내역 등록 번호"),
					fieldWithPath("data.portfolioHoldings[].purchaseHistory[].purchaseDate")
						.type(JsonFieldType.STRING)
						.description("구매일자"),
					fieldWithPath("data.portfolioHoldings[].purchaseHistory[].numShares")
						.type(JsonFieldType.NUMBER)
						.description("종목 개수"),
					fieldWithPath("data.portfolioHoldings[].purchaseHistory[].purchasePricePerShare")
						.type(JsonFieldType.NUMBER)
						.description("매입가"),
					fieldWithPath("data.portfolioHoldings[].purchaseHistory[].memo")
						.type(JsonFieldType.STRING)
						.description("메모")
				)
			)
		);

	}
}
