package codesquad.fineants.spring.api.portfolio_stock.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalArgumentResolver;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock_dividend.StockDividend;
import codesquad.fineants.spring.api.errors.errorcode.PortfolioErrorCode;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.errors.handler.GlobalExceptionHandler;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.portfolio.PortFolioService;
import codesquad.fineants.spring.api.portfolio_stock.chart.DividendChart;
import codesquad.fineants.spring.api.portfolio_stock.chart.PieChart;
import codesquad.fineants.spring.api.portfolio_stock.chart.SectorChart;
import codesquad.fineants.spring.api.portfolio_stock.manager.SseEmitterManager;
import codesquad.fineants.spring.api.portfolio_stock.request.PortfolioStockCreateRequest;
import codesquad.fineants.spring.api.portfolio_stock.request.PortfolioStocksDeleteRequest;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioChartResponse;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioDividendChartItem;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingsResponse;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioPieChartItem;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioSectorChartItem;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioStockCreateResponse;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioStockDeletesResponse;
import codesquad.fineants.spring.api.portfolio_stock.service.PortfolioStockService;
import codesquad.fineants.spring.api.portfolio_stock.service.StockMarketChecker;
import codesquad.fineants.spring.auth.HasPortfolioAuthorizationAspect;
import codesquad.fineants.spring.config.JacksonConfig;
import codesquad.fineants.spring.config.JpaAuditingConfiguration;
import codesquad.fineants.spring.config.SpringConfig;
import codesquad.fineants.spring.util.ObjectMapperUtil;

@ActiveProfiles("test")
@WebMvcTest(controllers = PortfolioStockRestController.class)
@Import(value = {SpringConfig.class, HasPortfolioAuthorizationAspect.class, JacksonConfig.class})
@MockBean(JpaAuditingConfiguration.class)
class PortfolioStockRestControllerTest {
	private MockMvc mockMvc;

	@Autowired
	private PortfolioStockRestController portfolioStockRestController;

	@Autowired
	private GlobalExceptionHandler globalExceptionHandler;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AuthPrincipalArgumentResolver authPrincipalArgumentResolver;

	@MockBean
	private PortfolioStockService portfolioStockService;

	@MockBean
	private StockMarketChecker stockMarketChecker;

	@MockBean
	private PortFolioService portFolioService;

	@MockBean
	private SseEmitterManager manager;

	@MockBean
	private CurrentPriceManager currentPriceManager;

	private PieChart pieChart;

	private DividendChart dividendChart;

	private SectorChart sectorChart;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(portfolioStockRestController)
			.setControllerAdvice(globalExceptionHandler)
			.setCustomArgumentResolvers(authPrincipalArgumentResolver)
			.setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
			.defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
			.alwaysDo(print())
			.build();

		pieChart = new PieChart(currentPriceManager);
		dividendChart = new DividendChart(currentPriceManager);
		sectorChart = new SectorChart(currentPriceManager);

		AuthMember authMember = AuthMember.from(createMember());
		given(authPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(authMember);
		given(authPrincipalArgumentResolver.supportsParameter(any())).willReturn(true);
		given(stockMarketChecker.isMarketOpen(any())).willReturn(false);
		given(portFolioService.hasAuthorizationBy(anyLong(), anyLong())).willReturn(true);
	}

	@DisplayName("사용자의 포트폴리오 상세 정보를 가져온다")
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

		given(portfolioStockService.readMyPortfolioStocks(anyLong())).willReturn(mockResponse);
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
	}

	@DisplayName("존재하지 않는 포트폴리오 번호를 가지고 포트폴리오 상세 정보를 가져올 수 없다")
	@Test
	void readMyPortfolioStocksWithNotExistPortfolioId() throws Exception {
		// given
		long portfolioId = 9999L;
		given(portfolioStockService.readMyPortfolioStocks(anyLong()))
			.willThrow(new NotFoundResourceException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));

		// when & then
		mockMvc.perform(get("/api/portfolio/{portfolioId}/holdings", portfolioId))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("message").value(equalTo("포트폴리오를 찾을 수 없습니다")));
	}

	@DisplayName("존재하지 않은 포트폴리오 등록번호를 가지고 상세 데이터를 조회할 수 없다")
	@Test
	void readMyPortfolioStocksInRealTimeWithNotExistPortfolioId() throws Exception {
		// given
		long portfolioId = 9999L;
		given(portfolioStockService.readMyPortfolioStocksInRealTime(anyLong()))
			.willThrow(new NotFoundResourceException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
		given(portFolioService.hasAuthorizationBy(anyLong(), anyLong()))
			.willThrow(new NotFoundResourceException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
		// when & then
		mockMvc.perform(get("/api/portfolio/{portfolioId}/holdings/realtime", portfolioId))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("message").value(equalTo("포트폴리오를 찾을 수 없습니다")));
	}

	@DisplayName("사용자는 포트폴리오에 종목과 매입이력을 추가한다")
	@Test
	void addPortfolioStock() throws Exception {
		Member member = createMember();
		Portfolio portfolio = createPortfolio(member);
		Stock stock = createStock();

		PortfolioStockCreateResponse response = PortfolioStockCreateResponse.from(
			PortfolioHolding.empty(portfolio, stock));
		given(portfolioStockService.addPortfolioStock(anyLong(), any(PortfolioStockCreateRequest.class),
			any(AuthMember.class))).willReturn(response);

		Map<String, Object> purchaseHistoryMap = new HashMap<>();
		purchaseHistoryMap.put("purchaseDate", LocalDateTime.now().toString());
		purchaseHistoryMap.put("numShares", 10L);
		purchaseHistoryMap.put("purchasePricePerShare", 100.0);
		purchaseHistoryMap.put("memo", null);

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("tickerSymbol", "005930");
		requestBodyMap.put("purchaseHistory", purchaseHistoryMap);

		String body = ObjectMapperUtil.serialize(requestBodyMap);
		Long portfolioId = portfolio.getId();
		// when & then
		mockMvc.perform(post("/api/portfolio/" + portfolioId + "/holdings")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 종목이 추가되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자는 포트폴리오에 종목만 추가한다")
	@Test
	void addPortfolioStockOnly() throws Exception {
		Member member = createMember();
		Portfolio portfolio = createPortfolio(member);
		Stock stock = createStock();

		PortfolioStockCreateResponse response = PortfolioStockCreateResponse.from(
			PortfolioHolding.empty(portfolio, stock));
		given(portfolioStockService.addPortfolioStock(anyLong(), any(PortfolioStockCreateRequest.class),
			any(AuthMember.class))).willReturn(response);

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("tickerSymbol", "005930");

		String body = ObjectMapperUtil.serialize(requestBodyMap);
		Long portfolioId = portfolio.getId();
		// when & then
		mockMvc.perform(post("/api/portfolio/" + portfolioId + "/holdings")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 종목이 추가되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자는 포트폴리오에 종목을 추가할때 stockId를 필수로 같이 전송해야 한다")
	@Test
	void addPortfolioStockWithStockIdIsNull() throws Exception {
		Member member = createMember();
		Portfolio portfolio = createPortfolio(member);

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("tickerSymbol", null);

		String body = ObjectMapperUtil.serialize(requestBodyMap);
		Long portfolioId = portfolio.getId();

		// when & then
		mockMvc.perform(post("/api/portfolio/" + portfolioId + "/holdings")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data").isArray());
	}

	@DisplayName("사용자는 포트폴리오 종목을 삭제한다")
	@Test
	void deletePortfolioStock() throws Exception {
		// given
		Member member = createMember();
		Portfolio portfolio = createPortfolio(member);
		Stock stock = createStock();
		PortfolioHolding portfolioHolding = createPortfolioHolding(portfolio, stock);

		Long portfolioHoldingId = portfolioHolding.getId();
		Long portfolioId = portfolio.getId();

		// when & then
		mockMvc.perform(
				delete("/api/portfolio/{portfolioId}/holdings/{portfolioHoldingId}", portfolioId, portfolioHoldingId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 종목이 삭제되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자는 포트폴리오 종목을 다수 삭제한다")
	@Test
	void deletePortfolioStocks() throws Exception {
		// given
		Member member = createMember();
		Portfolio portfolio = createPortfolio(member);

		List<Long> delPortfolioHoldingIds = List.of(1L, 2L);
		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("portfolioHoldingIds", delPortfolioHoldingIds);
		String body = ObjectMapperUtil.serialize(requestBodyMap);

		PortfolioStockDeletesResponse mockResponse = new PortfolioStockDeletesResponse(delPortfolioHoldingIds);
		given(portfolioStockService.deletePortfolioStocks(anyLong(), any(AuthMember.class), any(
			PortfolioStocksDeleteRequest.class))).willReturn(mockResponse);
		// when & then
		mockMvc.perform(delete("/api/portfolio/{portfolioId}/holdings", portfolio.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 종목들이 삭제되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자는 포트폴리오 종목을 다수 삭제할때 유효하지 않은 입력으로 삭제할 수 없다")
	@MethodSource(value = "provideInvalidPortfolioHoldingIds")
	@ParameterizedTest
	void deletePortfolioStocks_withInvalidItems(List<Long> portfolioHoldingIds) throws Exception {
		// given
		Member member = createMember();
		Portfolio portfolio = createPortfolio(member);

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("portfolioHoldingIds", portfolioHoldingIds);
		String body = ObjectMapperUtil.serialize(requestBodyMap);

		// when & then
		mockMvc.perform(delete("/api/portfolio/{portfolioId}/holdings", portfolio.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data[0].field").value(equalTo("portfolioHoldingIds")))
			.andExpect(jsonPath("data[0].defaultMessage").value(equalTo("삭제할 포트폴리오 종목들이 없습니다")));
	}

	@DisplayName("사용자는 포트폴레오에 대한 차트 정보를 조회한다")
	@Test
	void readMyPortfolioCharts() throws Exception {
		// given
		Member member = createMember();
		Portfolio portfolio = createPortfolio(member);
		Stock stock = createStock();
		List<StockDividend> stockDividends = createStockDividendWith(stock);
		stockDividends.forEach(stock::addStockDividend);
		PortfolioHolding portfolioHolding = createPortfolioHolding(portfolio, stock);
		portfolio.addPortfolioStock(portfolioHolding);
		portfolioHolding.addPurchaseHistory(
			createPurchaseHistory(portfolioHolding, LocalDateTime.of(2024, 1, 16, 9, 30, 0)));

		given(currentPriceManager.getCurrentPrice(stock.getTickerSymbol())).willReturn(
			portfolioHolding.getCurrentPrice());

		List<PortfolioPieChartItem> pieChartItems = this.pieChart.createBy(portfolio);
		List<PortfolioDividendChartItem> dividendChartItems = this.dividendChart.createBy(portfolio,
			LocalDate.of(2024, 1, 16));
		List<PortfolioSectorChartItem> sectorChartItems = this.sectorChart.createBy(portfolio);
		PortfolioChartResponse response = new PortfolioChartResponse(pieChartItems, dividendChartItems,
			sectorChartItems);
		given(portfolioStockService.readMyPortfolioCharts(anyLong(), any(LocalDate.class)))
			.willReturn(response);

		// when & then
		mockMvc.perform(get("/api/portfolio/{portfolioId}/charts", portfolio.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오에 대한 차트 조회가 완료되었습니다")))
			.andExpect(jsonPath("data.pieChart[0].name").value(equalTo("삼성전자보통주")))
			.andExpect(jsonPath("data.pieChart[0].valuation").value(equalTo(180000)))
			.andExpect(jsonPath("data.pieChart[0].weight").value(equalTo(17.48)))
			.andExpect(jsonPath("data.pieChart[0].totalGain").value(equalTo(30000)))
			.andExpect(jsonPath("data.pieChart[0].totalGainRate").value(equalTo(20.0)))
			.andExpect(jsonPath("data.pieChart[1].name").value(equalTo("현금")))
			.andExpect(jsonPath("data.pieChart[1].valuation").value(equalTo(850000)))
			.andExpect(jsonPath("data.pieChart[1].weight").value(equalTo(82.52)))
			.andExpect(jsonPath("data.pieChart[1].totalGain").value(equalTo(0)))
			.andExpect(jsonPath("data.pieChart[1].totalGainRate").value(equalTo(0.00)))
			.andExpect(jsonPath("data.dividendChart[0].month").value(equalTo(1)))
			.andExpect(jsonPath("data.dividendChart[0].amount").value(equalTo(0)))
			.andExpect(jsonPath("data.dividendChart[1].month").value(equalTo(2)))
			.andExpect(jsonPath("data.dividendChart[1].amount").value(equalTo(0)))
			.andExpect(jsonPath("data.dividendChart[3].month").value(equalTo(4)))
			.andExpect(jsonPath("data.dividendChart[3].amount").value(equalTo(1083)))
			.andExpect(jsonPath("data.dividendChart[4].month").value(equalTo(5)))
			.andExpect(jsonPath("data.dividendChart[4].amount").value(equalTo(1083)))
			.andExpect(jsonPath("data.dividendChart[5].month").value(equalTo(6)))
			.andExpect(jsonPath("data.dividendChart[5].amount").value(equalTo(0)))
			.andExpect(jsonPath("data.dividendChart[6].month").value(equalTo(7)))
			.andExpect(jsonPath("data.dividendChart[6].amount").value(equalTo(0)))
			.andExpect(jsonPath("data.dividendChart[7].month").value(equalTo(8)))
			.andExpect(jsonPath("data.dividendChart[7].amount").value(equalTo(1083)))
			.andExpect(jsonPath("data.dividendChart[8].month").value(equalTo(9)))
			.andExpect(jsonPath("data.dividendChart[8].amount").value(equalTo(0)))
			.andExpect(jsonPath("data.dividendChart[9].month").value(equalTo(10)))
			.andExpect(jsonPath("data.dividendChart[9].amount").value(equalTo(0)))
			.andExpect(jsonPath("data.dividendChart[10].month").value(equalTo(11)))
			.andExpect(jsonPath("data.dividendChart[10].amount").value(equalTo(1083)))
			.andExpect(jsonPath("data.dividendChart[11].month").value(equalTo(12)))
			.andExpect(jsonPath("data.dividendChart[11].amount").value(equalTo(0)));
	}

	private Stock createStock() {
		return Stock.builder()
			.tickerSymbol("005930")
			.companyName("삼성전자보통주")
			.companyNameEng("SamsungElectronics")
			.stockCode("KR7005930003")
			.market(Market.KOSPI)
			.sector("전기전자")
			.build();
	}

	private Member createMember() {
		return Member.builder()
			.id(1L)
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.provider("local")
			.password("kim1234@")
			.profileUrl("profileValue")
			.build();
	}

	private Portfolio createPortfolio(Member member) {
		return Portfolio.builder()
			.id(1L)
			.name("내꿈은 워렌버핏")
			.securitiesFirm("토스")
			.budget(1000000L)
			.targetGain(1500000L)
			.maximumLoss(900000L)
			.targetGainIsActive(false)
			.maximumLossIsActive(false)
			.member(member)
			.build();
	}

	private PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock) {
		return PortfolioHolding.builder()
			.id(1L)
			.portfolio(portfolio)
			.stock(stock)
			.currentPrice(60000L)
			.build();
	}

	private PurchaseHistory createPurchaseHistory(PortfolioHolding portfolioHolding, LocalDateTime purchaseDate) {
		return PurchaseHistory.builder()
			.id(1L)
			.purchaseDate(purchaseDate)
			.numShares(3L)
			.purchasePricePerShare(50000.0)
			.memo("첫구매")
			.portfolioHolding(portfolioHolding)
			.build();
	}

	private PortfolioGainHistory createEmptyPortfolioGainHistory() {
		return PortfolioGainHistory.empty();
	}

	public static Stream<Arguments> provideInvalidPortfolioHoldingIds() {
		return Stream.of(
			Arguments.of(Collections.emptyList()),
			Arguments.of((Object)null)
		);
	}

	private StockDividend createStockDividend(LocalDate exDividendDate, LocalDate recordDate, LocalDate paymentDate,
		Stock stock) {
		return StockDividend.builder()
			.dividend(361L)
			.exDividendDate(exDividendDate)
			.recordDate(recordDate)
			.paymentDate(paymentDate)
			.stock(stock)
			.build();
	}

	private List<StockDividend> createStockDividendWith(Stock stock) {
		return List.of(
			createStockDividend(
				LocalDate.of(2022, 3, 30),
				LocalDate.of(2022, 3, 31),
				LocalDate.of(2022, 5, 17),
				stock
			),
			createStockDividend(
				LocalDate.of(2022, 6, 29),
				LocalDate.of(2022, 6, 30),
				LocalDate.of(2022, 8, 16),
				stock
			),
			createStockDividend(
				LocalDate.of(2022, 9, 29),
				LocalDate.of(2022, 9, 30),
				LocalDate.of(2022, 11, 15),
				stock
			),
			createStockDividend(
				LocalDate.of(2022, 12, 30),
				LocalDate.of(2022, 12, 31),
				LocalDate.of(2023, 4, 14),
				stock),
			createStockDividend(
				LocalDate.of(2023, 3, 30),
				LocalDate.of(2023, 3, 31),
				LocalDate.of(2023, 5, 17),
				stock),
			createStockDividend(
				LocalDate.of(2023, 6, 29),
				LocalDate.of(2023, 6, 30),
				LocalDate.of(2023, 8, 16),
				stock),
			createStockDividend(
				LocalDate.of(2023, 9, 27),
				LocalDate.of(2023, 9, 30),
				LocalDate.of(2023, 11, 20),
				stock),
			createStockDividend(
				LocalDate.of(2024, 3, 30),
				LocalDate.of(2024, 3, 31),
				LocalDate.of(2024, 5, 17),
				stock)
		);
	}
}
