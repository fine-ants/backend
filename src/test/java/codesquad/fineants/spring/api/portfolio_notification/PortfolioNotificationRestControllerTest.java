package codesquad.fineants.spring.api.portfolio_notification;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
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
import codesquad.fineants.spring.api.portfolio_notification.request.PortfolioNotificationModifyRequest;
import codesquad.fineants.spring.api.portfolio_notification.response.PortfolioNotificationModifyResponse;
import codesquad.fineants.spring.config.JpaAuditingConfiguration;

@ActiveProfiles("test")
@WebMvcTest(controllers = PortfolioNotificationRestController.class)
@MockBean(JpaAuditingConfiguration.class)
class PortfolioNotificationRestControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private PortfolioNotificationRestController portfolioNotificationRestController;

	@Autowired
	private GlobalExceptionHandler globalExceptionHandler;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AuthPrincipalArgumentResolver authPrincipalArgumentResolver;

	@MockBean
	private PortfolioNotificationService service;

	private Member member;
	private Portfolio portfolio;
	private Stock stock;
	private PortfolioHolding portfolioHolding;
	private PurchaseHistory purchaseHistory;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(portfolioNotificationRestController)
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

	@DisplayName("사용자는 포트폴리오의 목표수익금액 알람을 활성화합니다.")
	@Test
	void modifyNotificationTargetGain() throws Exception {
		// given
		long portfolioId = portfolio.getId();
		Map<String, String> requestBodyMap = new HashMap<>();
		requestBodyMap.put("isActive", "true");

		Map<String, Object> responseBodyMap = new HashMap<>();
		responseBodyMap.put("portfolioId", portfolioId);
		responseBodyMap.put("isActive", true);

		PortfolioNotificationModifyResponse response = objectMapper.readValue(
			objectMapper.writeValueAsString(responseBodyMap), PortfolioNotificationModifyResponse.class);
		given(service.modifyPortfolioTargetGainNotification(
			any(PortfolioNotificationModifyRequest.class),
			anyLong()
		)).willReturn(response);

		// when & then
		mockMvc.perform(put(String.format("/api/portfolio/%d/notification/targetGain", portfolioId))
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(requestBodyMap)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("목표수익금액의 알림이 활성화되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자는 포트폴리오의 목표수익금액 알람을 비활성화합니다.")
	@Test
	void modifyNotificationTargetGainWithInActive() throws Exception {
		// given
		long portfolioId = portfolio.getId();
		Map<String, String> requestBodyMap = new HashMap<>();
		requestBodyMap.put("isActive", "false");

		Map<String, Object> responseBodyMap = new HashMap<>();
		responseBodyMap.put("portfolioId", portfolioId);
		responseBodyMap.put("isActive", false);

		PortfolioNotificationModifyResponse response = objectMapper.readValue(
			objectMapper.writeValueAsString(responseBodyMap), PortfolioNotificationModifyResponse.class);
		given(service.modifyPortfolioTargetGainNotification(
			any(PortfolioNotificationModifyRequest.class),
			anyLong()
		)).willReturn(response);

		// when & then
		mockMvc.perform(put(String.format("/api/portfolio/%d/notification/targetGain", portfolioId))
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(requestBodyMap)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("목표수익금액의 알림이 비활성화되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자는 포트폴리오의 최대손실금액 알람을 활성화합니다.")
	@Test
	void modifyNotificationMaximumLoss() throws Exception {
		// given
		long portfolioId = portfolio.getId();
		Map<String, String> requestBodyMap = new HashMap<>();
		requestBodyMap.put("isActive", "true");

		Map<String, Object> responseBodyMap = new HashMap<>();
		responseBodyMap.put("portfolioId", portfolioId);
		responseBodyMap.put("isActive", true);

		PortfolioNotificationModifyResponse response = objectMapper.readValue(
			objectMapper.writeValueAsString(responseBodyMap), PortfolioNotificationModifyResponse.class);
		given(service.modifyPortfolioMaximumLossNotification(
			any(PortfolioNotificationModifyRequest.class),
			anyLong()
		)).willReturn(response);

		// when & then
		mockMvc.perform(put(String.format("/api/portfolio/%d/notification/maxLoss", portfolioId))
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(requestBodyMap)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("최대손실금액의 알림이 활성화되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자는 포트폴리오의 최대손실금액 알람을 비활성화합니다.")
	@Test
	void modifyNotificationMaximumLossWithInActive() throws Exception {
		// given
		long portfolioId = portfolio.getId();
		Map<String, String> requestBodyMap = new HashMap<>();
		requestBodyMap.put("isActive", "false");

		Map<String, Object> responseBodyMap = new HashMap<>();
		responseBodyMap.put("portfolioId", portfolioId);
		responseBodyMap.put("isActive", false);

		PortfolioNotificationModifyResponse response = objectMapper.readValue(
			objectMapper.writeValueAsString(responseBodyMap), PortfolioNotificationModifyResponse.class);
		given(service.modifyPortfolioMaximumLossNotification(
			any(PortfolioNotificationModifyRequest.class),
			anyLong()
		)).willReturn(response);

		// when & then
		mockMvc.perform(put(String.format("/api/portfolio/%d/notification/maxLoss", portfolioId))
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(requestBodyMap)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("최대손실금액의 알림이 비활성화되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}
}
