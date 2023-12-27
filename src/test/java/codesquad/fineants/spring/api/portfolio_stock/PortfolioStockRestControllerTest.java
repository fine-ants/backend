package codesquad.fineants.spring.api.portfolio_stock;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalArgumentResolver;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.spring.api.errors.errorcode.PortfolioErrorCode;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.errors.handler.GlobalExceptionHandler;
import codesquad.fineants.spring.api.portfolio_stock.controller.PortfolioStockRestController;
import codesquad.fineants.spring.api.portfolio_stock.manager.SseEmitterManager;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingsResponse;
import codesquad.fineants.spring.api.portfolio_stock.service.PortfolioStockService;
import codesquad.fineants.spring.api.portfolio_stock.service.StockMarketChecker;
import codesquad.fineants.spring.auth.HasPortfolioAuthorizationAspect;
import codesquad.fineants.spring.config.JpaAuditingConfiguration;
import codesquad.fineants.spring.config.SpringConfig;

@ActiveProfiles("test")
@WebMvcTest(controllers = PortfolioStockRestController.class)
@Import(value = {SpringConfig.class, HasPortfolioAuthorizationAspect.class})
@MockBean(JpaAuditingConfiguration.class)
class PortfolioStockRestControllerTest {
	private MockMvc mockMvc;

	@Autowired
	private PortfolioStockRestController portfolioStockRestController;

	@Autowired
	private GlobalExceptionHandler globalExceptionHandler;

	@Autowired
	private MappingJackson2HttpMessageConverter converter;

	@MockBean
	private AuthPrincipalArgumentResolver authPrincipalArgumentResolver;

	@MockBean
	private PortfolioStockService portfolioStockService;

	@MockBean
	private StockMarketChecker stockMarketChecker;

	@MockBean
	private PortfolioRepository portfolioRepository; // HasAuthorizationAspect 목빈 객체

	@MockBean
	private SseEmitterManager manager;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(portfolioStockRestController)
			.setControllerAdvice(globalExceptionHandler)
			.setCustomArgumentResolvers(authPrincipalArgumentResolver)
			.setMessageConverters(converter)
			.defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
			.alwaysDo(print())
			.build();

		AuthMember authMember = AuthMember.from(createMember());
		given(authPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(authMember);
		given(authPrincipalArgumentResolver.supportsParameter(any())).willReturn(true);
		given(stockMarketChecker.isMarketOpen(any())).willReturn(false);
	}

	@DisplayName("사용자의 포트폴리오 상세 정보를 가져온다")
	@Test
	void readMyPortfolioStocks() throws Exception {
		// given
		Member member = createMember();
		Portfolio portfolio = createPortfolio(member);
		Stock stock = createStock();
		PortfolioHolding portfolioHolding = createPortfolioHolding(portfolio, stock);
		portfolioHolding.addPurchaseHistory(createPurchaseHistory(portfolioHolding));
		portfolio.addPortfolioStock(portfolioHolding);
		PortfolioGainHistory history = createEmptyPortfolioGainHistory();

		Map<String, Long> lastDayClosingPriceMap = Map.of("005930", 50000L);
		PortfolioHoldingsResponse mockResponse = PortfolioHoldingsResponse.of(portfolio, history,
			List.of(portfolioHolding),
			lastDayClosingPriceMap);

		given(portfolioStockService.readMyPortfolioStocks(anyLong())).willReturn(mockResponse);
		given(portfolioRepository.findById(anyLong())).willReturn(Optional.of(portfolio));
		// when & then
		mockMvc.perform(get("/api/portfolio/{portfolioId}/holdings", portfolio.getId()))
			.andExpect(status().isOk());
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

		// when & then
		mockMvc.perform(get("/api/portfolio/{portfolioId}/holdings/realtime", portfolioId))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("message").value(equalTo("포트폴리오를 찾을 수 없습니다")));
	}

	private Stock createStock() {
		return Stock.builder()
			.tickerSymbol("005930")
			.companyName("삼성전자보통주")
			.companyNameEng("SamsungElectronics")
			.stockCode("KR7005930003")
			.market(Market.KOSPI)
			.build();
	}

	private static Member createMember() {
		return Member.builder()
			.id(1L)
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.provider("local")
			.password("kim1234@")
			.profileUrl("profileValue")
			.build();
	}

	private static Portfolio createPortfolio(Member member) {
		return Portfolio.builder()
			.id(1L)
			.name("내꿈은 워렌버핏")
			.securitiesFirm("토스")
			.budget(1000000L)
			.targetGain(1500000L)
			.maximumLoss(900000L)
			.member(member)
			.build();
	}

	private static PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock) {
		return PortfolioHolding.builder()
			.id(1L)
			.portfolio(portfolio)
			.stock(stock)
			.currentPrice(60000L)
			.build();
	}

	private static PurchaseHistory createPurchaseHistory(PortfolioHolding portfolioHolding) {
		return PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.of(2023, 11, 1, 9, 30, 0))
			.numShares(3L)
			.purchasePricePerShare(50000.0)
			.memo("첫구매")
			.portfolioHolding(portfolioHolding)
			.build();
	}

	private static PortfolioGainHistory createEmptyPortfolioGainHistory() {
		return PortfolioGainHistory.empty();
	}
}
