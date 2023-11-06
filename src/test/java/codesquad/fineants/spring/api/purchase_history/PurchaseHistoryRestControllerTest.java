package codesquad.fineants.spring.api.purchase_history;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalArgumentResolver;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.spring.api.errors.handler.GlobalExceptionHandler;
import codesquad.fineants.spring.config.JpaAuditingConfiguration;

@ActiveProfiles("test")
@WebMvcTest(controllers = PurchaseHistoryRestController.class)
@MockBean(JpaAuditingConfiguration.class)
class PurchaseHistoryRestControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private PurchaseHistoryRestController purchaseHistoryRestController;

	@Autowired
	private GlobalExceptionHandler globalExceptionHandler;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AuthPrincipalArgumentResolver authPrincipalArgumentResolver;

	@MockBean
	private PurchaseHistoryService purchaseHistoryService;

	private Member member;
	private Portfolio portfolio;
	private Stock stock;

	private PortfolioHolding portfolioHolding;

	private PurchaseHistory purchaseHistory;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(purchaseHistoryRestController)
			.setControllerAdvice(globalExceptionHandler)
			.setCustomArgumentResolvers(authPrincipalArgumentResolver)
			.alwaysDo(print())
			.build();

		given(authPrincipalArgumentResolver.supportsParameter(any())).willReturn(true);

		member = Member.builder()
			.id(1L)
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.provider("local")
			.password("kim1234@")
			.profileUrl("profileValue")
			.build();

		AuthMember authMember = AuthMember.from(member);

		given(authPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(authMember);

		portfolio = Portfolio.builder()
			.id(1L)
			.name("내꿈은 워렌버핏")
			.securitiesFirm("토스")
			.budget(1000000L)
			.targetGain(1500000L)
			.maximumLoss(900000L)
			.member(member)
			.build();

		stock = Stock.builder()
			.tickerSymbol("005930")
			.companyName("삼성전자보통주")
			.companyNameEng("SamsungElectronics")
			.stockCode("KR7005930003")
			.market(Market.KOSPI)
			.build();

		portfolioHolding = PortfolioHolding.builder()
			.id(1L)
			.currentPrice(null)
			.portfolio(portfolio)
			.stock(stock)
			.build();
		purchaseHistory = PurchaseHistory.builder()
			.id(1L)
			.purchaseDate(LocalDateTime.now())
			.purchasePricePerShare(50000.0)
			.numShares(3L)
			.memo("첫구매")
			.build();
	}

	@DisplayName("사용자가 매입 이력을 추가한다")
	@Test
	void addPurchaseHistory() throws Exception {
		// given
		String url = String.format("/api/portfolio/%d/holdings/%d/purchaseHistory", portfolio.getId(),
			portfolioHolding.getId());
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("purchaseDate", LocalDateTime.now().toString());
		requestBody.put("numShares", 3);
		requestBody.put("purchasePricePerShare", 50000);
		requestBody.put("memo", "첫구매");

		String body = objectMapper.writeValueAsString(requestBody);

		// when & then
		mockMvc.perform(post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(body))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo("매입 이력이 추가되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자가 매입 이력 추가시 유효하지 않은 입력으로 추가할 수 없다")
	@Test
	void addPurchaseHistoryWithInvalidInput() throws Exception {
		// given
		String url = String.format("/api/portfolio/%d/holdings/%d/purchaseHistory", portfolio.getId(),
			portfolioHolding.getId());
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("purchaseDate", null);
		requestBody.put("numShares", 0);
		requestBody.put("purchasePricePerShare", 0);
		requestBody.put("memo", "첫구매");

		String body = objectMapper.writeValueAsString(requestBody);

		// when & then
		mockMvc.perform(post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(body))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data").isArray());
	}

	@DisplayName("사용자가 매입 이력을 수정한다")
	@Test
	void modifyPurchaseHistory() throws Exception {
		// given
		String url = String.format("/api/portfolio/%d/holdings/%d/purchaseHistory/%d", portfolio.getId(),
			portfolioHolding.getId(), purchaseHistory.getId());
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("purchaseDate", LocalDateTime.now().toString());
		requestBody.put("numShares", 4);
		requestBody.put("purchasePricePerShare", 50000);
		requestBody.put("memo", "첫구매");

		String body = objectMapper.writeValueAsString(requestBody);

		// when & then
		mockMvc.perform(put(url)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("매입 이력이 수정되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자가 매입 이력을 삭제한다")
	@Test
	void deletePurchaseHistory() throws Exception {
		// given
		String url = String.format("/api/portfolio/%d/holdings/%d/purchaseHistory/%d", portfolio.getId(),
			portfolioHolding.getId(), purchaseHistory.getId());

		// when & then
		mockMvc.perform(delete(url))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("매입 이력이 삭제되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}
}
