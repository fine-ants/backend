package codesquad.fineants.docs.portfolio_holding;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import codesquad.fineants.docs.RestDocsSupport;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.dividend.domain.entity.StockDividend;
import codesquad.fineants.domain.holding.controller.PortfolioHoldingRestController;
import codesquad.fineants.domain.holding.domain.dto.request.PortfolioHoldingCreateRequest;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioChartResponse;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioDividendChartItem;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioHoldingsResponse;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioPieChartItem;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioSectorChartItem;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioStockCreateResponse;
import codesquad.fineants.domain.holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.holding.service.PortfolioHoldingService;
import codesquad.fineants.domain.holding.service.PortfolioObservableService;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio_gain_history.domain.entity.PortfolioGainHistory;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.global.util.ObjectMapperUtil;

public class PortfolioHoldingRestControllerDocsTest extends RestDocsSupport {

	private final PortfolioHoldingService service = Mockito.mock(PortfolioHoldingService.class);
	private final PortfolioObservableService portfolioObservableService = Mockito.mock(
		PortfolioObservableService.class);

	@Override
	protected Object initController() {
		return new PortfolioHoldingRestController(service, portfolioObservableService);
	}

	@DisplayName("포트폴리오 종목 생성 API")
	@Test
	void createPortfolioHolding() throws Exception {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		given(service.createPortfolioHolding(
			anyLong(),
			ArgumentMatchers.any(PortfolioHoldingCreateRequest.class)))
			.willReturn(PortfolioStockCreateResponse.from(holding));

		Map<String, Object> body = Map.of(
			"tickerSymbol", "005930",
			"purchaseHistory", Map.of(
				"purchaseDate", "2023-10-23T13:00:00",
				"numShares", 3,
				"purchasePricePerShare", 50000,
				"memo", "첫구매"
			)
		);

		// when & then
		mockMvc.perform(post("/api/portfolio/{portfolioId}/holdings", portfolio.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 종목이 추가되었습니다")))
			.andDo(
				document(
					"holding-create",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					pathParameters(
						parameterWithName("portfolioId").description("포트폴리오 등록번호")
					),
					requestFields(
						fieldWithPath("tickerSymbol").type(JsonFieldType.STRING).description("종목 티커심볼"),
						fieldWithPath("purchaseHistory").type(JsonFieldType.OBJECT).description("매입 이력").optional(),
						fieldWithPath("purchaseHistory.purchaseDate").type(JsonFieldType.STRING).description("매입 일자"),
						fieldWithPath("purchaseHistory.numShares").type(JsonFieldType.NUMBER).description("매입 개수"),
						fieldWithPath("purchaseHistory.purchasePricePerShare").type(JsonFieldType.NUMBER)
							.description("평균 매입가"),
						fieldWithPath("purchaseHistory.memo").type(JsonFieldType.STRING).optional().description("메모")
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

	@DisplayName("포트폴리오 종목 조회 API")
	@Test
	void readPortfolioHoldings() throws Exception {
		// given
		Member member = createMember();
		Portfolio portfolio = createPortfolio(member);
		Stock stock = createSamsungStock();
		List<StockDividend> stockDividends = createStockDividendWith(stock);
		stockDividends.forEach(stock::addStockDividend);
		PortfolioHolding portfolioHolding = createPortfolioHolding(portfolio, stock);
		portfolioHolding.addPurchaseHistory(
			createPurchaseHistory(portfolioHolding, LocalDateTime.of(2023, 11, 1, 9, 30, 0)));
		portfolio.addPortfolioStock(portfolioHolding);
		PortfolioGainHistory history = createEmptyPortfolioGainHistory(portfolio);

		Map<String, Money> lastDayClosingPriceMap = Map.of("005930", Money.won(50000L));
		PortfolioHoldingsResponse mockResponse = PortfolioHoldingsResponse.of(portfolio, history,
			List.of(portfolioHolding),
			lastDayClosingPriceMap);

		given(service.readPortfolioHoldings(anyLong())).willReturn(mockResponse);
		// when & then
		ResultActions resultActions = mockMvc.perform(get("/api/portfolio/{portfolioId}/holdings", portfolio.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk());

		resultActions
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 상세 정보 및 포트폴리오 종목 목록 조회가 완료되었습니다")))
			.andExpect(jsonPath("data.portfolioDetails.id").value(equalTo(1)))
			.andExpect(jsonPath("data.portfolioDetails.securitiesFirm").value(equalTo("토스증권")))
			.andExpect(jsonPath("data.portfolioDetails.name").value(equalTo("내꿈은 워렌버핏")))
			.andExpect(jsonPath("data.portfolioDetails.budget").value(equalTo(1000000)))
			.andExpect(jsonPath("data.portfolioDetails.targetGain").value(equalTo(1500000)))
			.andExpect(jsonPath("data.portfolioDetails.targetReturnRate").value(closeTo(50.0, 0.1)))
			.andExpect(jsonPath("data.portfolioDetails.maximumLoss").value(equalTo(900000)))
			.andExpect(jsonPath("data.portfolioDetails.maximumLossRate").value(closeTo(10.0, 0.1)))
			.andExpect(jsonPath("data.portfolioDetails.currentValuation").value(equalTo(180000)))
			.andExpect(jsonPath("data.portfolioDetails.investedAmount").value(equalTo(150000)))
			.andExpect(jsonPath("data.portfolioDetails.totalGain").value(equalTo(30000)))
			.andExpect(jsonPath("data.portfolioDetails.totalGainRate").value(closeTo(20.0, 0.1)))
			.andExpect(jsonPath("data.portfolioDetails.balance").value(equalTo(850000)))
			.andExpect(jsonPath("data.portfolioDetails.annualDividend").value(equalTo(4332)))
			.andExpect(jsonPath("data.portfolioDetails.annualDividendYield").value(closeTo(2.41, 0.1)))
			.andExpect(jsonPath("data.portfolioDetails.annualInvestmentDividendYield").value(closeTo(2.89, 0.1)))
			.andExpect(jsonPath("data.portfolioDetails.provisionalLossBalance").value(equalTo(0)))
			.andExpect(jsonPath("data.portfolioDetails.targetGainNotify").value(equalTo(true)))
			.andExpect(jsonPath("data.portfolioDetails.maxLossNotify").value(equalTo(true)));

		resultActions
			.andExpect(jsonPath("data.portfolioHoldings[0].companyName").value(equalTo("삼성전자보통주")))
			.andExpect(jsonPath("data.portfolioHoldings[0].tickerSymbol").value(equalTo("005930")))
			.andExpect(jsonPath("data.portfolioHoldings[0].id").value(equalTo(1)))
			.andExpect(jsonPath("data.portfolioHoldings[0].currentValuation").value(equalTo(180000)))
			.andExpect(jsonPath("data.portfolioHoldings[0].currentPrice").value(equalTo(60000)))
			.andExpect(jsonPath("data.portfolioHoldings[0].averageCostPerShare").value(equalTo(50000)))
			.andExpect(jsonPath("data.portfolioHoldings[0].numShares").value(equalTo(3)))
			.andExpect(jsonPath("data.portfolioHoldings[0].dailyChange").value(equalTo(10000)))
			.andExpect(jsonPath("data.portfolioHoldings[0].dailyChangeRate").value(closeTo(20.0, 0.1)))
			.andExpect(jsonPath("data.portfolioHoldings[0].totalGain").value(equalTo(30000)))
			.andExpect(jsonPath("data.portfolioHoldings[0].totalReturnRate").value(closeTo(20.0, 0.1)))
			.andExpect(jsonPath("data.portfolioHoldings[0].annualDividend").value(equalTo(4332)))
			.andExpect(jsonPath("data.portfolioHoldings[0].annualDividendYield").value(closeTo(2.41, 0.1)))
			.andExpect(jsonPath("data.portfolioHoldings[0].dateAdded").isNotEmpty());

		resultActions
			.andExpect(jsonPath("data.portfolioHoldings[0].purchaseHistory[0].purchaseHistoryId").value(equalTo(1)))
			.andExpect(jsonPath("data.portfolioHoldings[0].purchaseHistory[0].purchaseDate").value(
				equalTo("2023-11-01T09:30:00")))
			.andExpect(jsonPath("data.portfolioHoldings[0].purchaseHistory[0].numShares").value(equalTo(3)))
			.andExpect(
				jsonPath("data.portfolioHoldings[0].purchaseHistory[0].purchasePricePerShare").value(equalTo(50000)))
			.andExpect(jsonPath("data.portfolioHoldings[0].purchaseHistory[0].memo").value(equalTo("첫구매")));

		resultActions.andDo(
			document(
				"holding-search",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
				),
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
					fieldWithPath("data.portfolioDetails.targetGainNotify").type(JsonFieldType.BOOLEAN)
						.description("목표 수익 금액 알림 여부"),
					fieldWithPath("data.portfolioDetails.maxLossNotify").type(JsonFieldType.BOOLEAN)
						.description("최대 손실 금액 알림 여부"),

					fieldWithPath("data.portfolioHoldings[].id").type(JsonFieldType.NUMBER)
						.description("포트폴리오 종목 등록번호"),
					fieldWithPath("data.portfolioHoldings[].companyName").type(JsonFieldType.STRING)
						.description("종목명"),
					fieldWithPath("data.portfolioHoldings[].tickerSymbol").type(JsonFieldType.STRING)
						.description("종목 티커 심볼"),
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
					fieldWithPath("data.portfolioHoldings[].dateAdded").type(JsonFieldType.STRING)
						.description("생성일자"),

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
						.description("메모 (NULL 허용)")
				)
			)
		);
	}

	@DisplayName("포트폴리오 종목 실시간 조회 API")
	@Test
	void readPortfolioHoldingsInRealTime() throws Exception {
		// given
		Member member = createMember();
		Portfolio portfolio = createPortfolio(member);

		// when & then
		mockMvc.perform(
				get("/api/portfolio/{portfolioId}/holdings/realtime", portfolio.getId())
					.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andDo(
				document(
					"holding_real_time-search",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					pathParameters(
						parameterWithName("portfolioId").description("포트폴리오 등록번호")
					)
				)
			);
	}

	@DisplayName("포트폴리오 차트 조회 API")
	@Test
	void readPortfolioCharts() throws Exception {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		portfolio.calculateTotalAsset();
		int samsungValuation = 600000;
		int samsungTotalGain = 100000;
		int cash = 500000;
		List<PortfolioPieChartItem> pieChartItems = List.of(
			PortfolioPieChartItem.stock(
				"삼성전자보통주",
				Money.won(samsungValuation),
				Percentage.from(0.5455),
				Money.won(samsungTotalGain),
				Percentage.from(0.10)),
			PortfolioPieChartItem.cash(
				Percentage.from(0.4545),
				Money.won(cash))
		);
		List<PortfolioDividendChartItem> dividendChartItems = List.of(
			PortfolioDividendChartItem.empty(1),
			PortfolioDividendChartItem.empty(2),
			PortfolioDividendChartItem.empty(3),
			PortfolioDividendChartItem.create(4, Money.won(3610L)),
			PortfolioDividendChartItem.create(5, Money.won(3610L)),
			PortfolioDividendChartItem.empty(6),
			PortfolioDividendChartItem.empty(7),
			PortfolioDividendChartItem.create(8, Money.won(3610L)),
			PortfolioDividendChartItem.empty(9),
			PortfolioDividendChartItem.empty(10),
			PortfolioDividendChartItem.create(11, Money.won(3610L)),
			PortfolioDividendChartItem.empty(12)
		);
		List<PortfolioSectorChartItem> sectorChartItems = List.of(
			PortfolioSectorChartItem.create("전기전자", Percentage.from(0.5455)),
			PortfolioSectorChartItem.create("현금",
				Percentage.from(0.4545))
		);

		given(service.readPortfolioCharts(anyLong(), ArgumentMatchers.any(LocalDate.class)))
			.willReturn(PortfolioChartResponse.create(pieChartItems, dividendChartItems, sectorChartItems));

		// when
		mockMvc.perform(
				get("/api/portfolio/{portfolioId}/charts", portfolio.getId())
					.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오에 대한 차트 조회가 완료되었습니다")))
			.andExpect(jsonPath("data.pieChart[0].name").value(equalTo("삼성전자보통주")))
			.andExpect(jsonPath("data.pieChart[0].valuation").value(equalTo(600000)))
			.andExpect(jsonPath("data.pieChart[0].weight").value(closeTo(54.55, 0.1)))
			.andExpect(jsonPath("data.pieChart[0].totalGain").value(equalTo(100000)))
			.andExpect(jsonPath("data.pieChart[0].totalGainRate").value(closeTo(10.00, 0.1)))
			.andExpect(jsonPath("data.pieChart[1].name").value(equalTo("현금")))
			.andExpect(jsonPath("data.pieChart[1].valuation").value(equalTo(500000)))
			.andExpect(jsonPath("data.pieChart[1].weight").value(closeTo(45.45, 0.1)))
			.andExpect(jsonPath("data.pieChart[1].totalGain").value(equalTo(0)))
			.andExpect(jsonPath("data.pieChart[1].totalGainRate").value(closeTo(0.00, 0.1)))
			.andExpect(jsonPath("data.dividendChart[0].month").value(equalTo(1)))
			.andExpect(jsonPath("data.dividendChart[0].amount").value(equalTo(0)))
			.andExpect(jsonPath("data.dividendChart[1].month").value(equalTo(2)))
			.andExpect(jsonPath("data.dividendChart[1].amount").value(equalTo(0)))
			.andExpect(jsonPath("data.dividendChart[2].month").value(equalTo(3)))
			.andExpect(jsonPath("data.dividendChart[2].amount").value(equalTo(0)))
			.andExpect(jsonPath("data.dividendChart[3].month").value(equalTo(4)))
			.andExpect(jsonPath("data.dividendChart[3].amount").value(equalTo(3610)))
			.andExpect(jsonPath("data.dividendChart[4].month").value(equalTo(5)))
			.andExpect(jsonPath("data.dividendChart[4].amount").value(equalTo(3610)))
			.andExpect(jsonPath("data.dividendChart[5].month").value(equalTo(6)))
			.andExpect(jsonPath("data.dividendChart[5].amount").value(equalTo(0)))
			.andExpect(jsonPath("data.dividendChart[6].month").value(equalTo(7)))
			.andExpect(jsonPath("data.dividendChart[6].amount").value(equalTo(0)))
			.andExpect(jsonPath("data.dividendChart[7].month").value(equalTo(8)))
			.andExpect(jsonPath("data.dividendChart[7].amount").value(equalTo(3610)))
			.andExpect(jsonPath("data.dividendChart[8].month").value(equalTo(9)))
			.andExpect(jsonPath("data.dividendChart[8].amount").value(equalTo(0)))
			.andExpect(jsonPath("data.dividendChart[9].month").value(equalTo(10)))
			.andExpect(jsonPath("data.dividendChart[9].amount").value(equalTo(0)))
			.andExpect(jsonPath("data.dividendChart[10].month").value(equalTo(11)))
			.andExpect(jsonPath("data.dividendChart[10].amount").value(equalTo(3610)))
			.andExpect(jsonPath("data.dividendChart[11].month").value(equalTo(12)))
			.andExpect(jsonPath("data.dividendChart[11].amount").value(equalTo(0)))
			.andExpect(jsonPath("data.sectorChart[0].sector").value(equalTo("전기전자")))
			.andExpect(jsonPath("data.sectorChart[0].sectorWeight").value(closeTo(54.55, 0.1)))
			.andExpect(jsonPath("data.sectorChart[1].sector").value(equalTo("현금")))
			.andExpect(jsonPath("data.sectorChart[1].sectorWeight").value(closeTo(45.45, 0.1)))
			.andDo(
				document(
					"portfolio_charts-search",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
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
						fieldWithPath("data.pieChart").type(JsonFieldType.ARRAY)
							.description("파이 차트 리스트"),
						fieldWithPath("data.pieChart[].name").type(JsonFieldType.STRING)
							.description("종목 이름"),
						fieldWithPath("data.pieChart[].valuation").type(JsonFieldType.NUMBER)
							.description("평가 금액"),
						fieldWithPath("data.pieChart[].weight").type(JsonFieldType.NUMBER)
							.description("비중"),
						fieldWithPath("data.pieChart[].totalGain").type(JsonFieldType.NUMBER)
							.description("총 손익"),
						fieldWithPath("data.pieChart[].totalGainRate").type(JsonFieldType.NUMBER)
							.description("총 손익률"),
						fieldWithPath("data.dividendChart").type(JsonFieldType.ARRAY)
							.description("배당금 차트"),
						fieldWithPath("data.dividendChart[].month").type(JsonFieldType.NUMBER)
							.description("배당 월"),
						fieldWithPath("data.dividendChart[].amount").type(JsonFieldType.NUMBER)
							.description("예상 배당금"),
						fieldWithPath("data.sectorChart").type(JsonFieldType.ARRAY)
							.description("섹터 차트"),
						fieldWithPath("data.sectorChart[].sector").type(JsonFieldType.STRING)
							.description("섹터명"),
						fieldWithPath("data.sectorChart[].sectorWeight").type(JsonFieldType.NUMBER)
							.description("섹터 비중")
					)
				)
			);

	}

	@DisplayName("포트폴리오 종목 단일 삭제")
	@Test
	void deletePortfolioHolding() throws Exception {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);

		// when & then
		mockMvc.perform(
				delete("/api/portfolio/{portfolioId}/holdings/{portfolioHoldingId}", portfolio.getId(), holding.getId())
					.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 종목이 삭제되었습니다")))
			.andDo(
				document(
					"holding-one-delete",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					pathParameters(
						parameterWithName("portfolioId").description("포트폴리오 등록번호"),
						parameterWithName("portfolioHoldingId").description("포트폴리오 종목 등록번호")
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

	@DisplayName("포트폴리오 종목 다수 삭제")
	@Test
	void deletePortfolioHoldings() throws Exception {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();

		Map<String, Object> body = Map.of(
			"portfolioHoldingIds", List.of(1, 2)
		);

		// when & then
		mockMvc.perform(
				delete("/api/portfolio/{portfolioId}/holdings", portfolio.getId())
					.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
					.contentType(MediaType.APPLICATION_JSON)
					.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 종목들이 삭제되었습니다")))
			.andDo(
				document(
					"holding-multiple-delete",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
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
						fieldWithPath("data").type(JsonFieldType.NULL)
							.description("응답 데이터")
					)
				)
			);
	}
}
