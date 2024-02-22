package codesquad.fineants.spring.api.stock;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.anyLong;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalArgumentResolver;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.spring.api.errors.handler.GlobalExceptionHandler;
import codesquad.fineants.spring.api.stock.request.TargetPriceNotificationCreateRequest;
import codesquad.fineants.spring.api.stock.request.TargetPriceNotificationUpdateRequest;
import codesquad.fineants.spring.api.stock.response.TargetPriceItem;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationCreateResponse;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationDeleteResponse;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationSearchItem;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationSearchResponse;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationSpecificItem;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationSpecifiedSearchResponse;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationUpdateResponse;
import codesquad.fineants.spring.config.JpaAuditingConfiguration;
import codesquad.fineants.spring.util.ObjectMapperUtil;

@ActiveProfiles("test")
@WebMvcTest(controllers = StockTargetPriceNotificationRestController.class)
@MockBean(JpaAuditingConfiguration.class)
class StockTargetPriceNotificationRestControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private StockTargetPriceNotificationRestController stockTargetPriceNotificationRestController;

	@Autowired
	private GlobalExceptionHandler globalExceptionHandler;

	@MockBean
	private AuthPrincipalArgumentResolver authPrincipalArgumentResolver;

	@MockBean
	private StockTargetPriceNotificationService service;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(stockTargetPriceNotificationRestController)
			.setControllerAdvice(globalExceptionHandler)
			.setCustomArgumentResolvers(authPrincipalArgumentResolver)
			.setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
			.alwaysDo(print())
			.build();

		given(authPrincipalArgumentResolver.supportsParameter(ArgumentMatchers.any(MethodParameter.class)))
			.willReturn(true);
		given(authPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any()))
			.willReturn(AuthMember.from(createMember()));
	}

	@DisplayName("사용자는 종목 지정가 알림을 추가합니다")
	@Test
	void createStockTargetPriceNotification() throws Exception {
		// given
		given(service.createStockTargetPriceNotification(
			any(TargetPriceNotificationCreateRequest.class),
			anyLong()))
			.willReturn(TargetPriceNotificationCreateResponse.builder()
				.targetPriceNotificationId(1L)
				.tickerSymbol("005930")
				.targetPrice(60000L)
				.build());

		String tickerSymbol = "005930";
		Map<String, Object> body = Map.of(
			"tickerSymbol", tickerSymbol,
			"targetPrice", 60000L);

		// when & then
		mockMvc.perform(post("/api/stocks/target-price/notifications")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo("해당 종목 지정가 알림을 추가했습니다")))
			.andExpect(jsonPath("data.targetPriceNotificationId").value(equalTo(1)))
			.andExpect(jsonPath("data.tickerSymbol").value(equalTo("005930")))
			.andExpect(jsonPath("data.targetPrice").value(equalTo(60000)));
	}

	@DisplayName("사용자는 유효하지 않은 입력으로 종목 지정가 알림을 추가할 수 없습니다")
	@MethodSource(value = "invalidTargetPrice")
	@ParameterizedTest
	void createStockTargetPriceNotification_whenInvalidTargetPrice_thenResponse400Error(String tickerSymbol,
		Long targetPrice) throws
		Exception {
		// given
		Map<String, Object> body = new HashMap<>();
		body.put("tickerSymbol", tickerSymbol);
		body.put("targetPrice", targetPrice);

		// when & then
		mockMvc.perform(post("/api/stocks/target-price/notifications")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data").isArray());
	}

	@DisplayName("사용자는 종목 지정가 알림 목록을 조회합니다")
	@Test
	void searchStockTargetPriceNotification() throws Exception {
		// given
		Stock stock = createStock();
		LocalDateTime now = LocalDateTime.now();
		given(service.searchStockTargetPriceNotification(anyLong()))
			.willReturn(TargetPriceNotificationSearchResponse.builder()
				.stocks(List.of(TargetPriceNotificationSearchItem.builder()
					.companyName(stock.getCompanyName())
					.tickerSymbol(stock.getTickerSymbol())
					.lastPrice(50000L)
					.targetPrices(List.of(
						TargetPriceItem.builder()
							.notificationId(1L)
							.targetPrice(60000L)
							.dateAdded(now)
							.build(),
						TargetPriceItem.builder()
							.notificationId(2L)
							.targetPrice(70000L)
							.dateAdded(now)
							.build()
					))
					.isActive(true)
					.lastUpdated(now)
					.build()))
				.build());

		// when & then
		mockMvc.perform(get("/api/stocks/target-price/notifications"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("모든 알림 조회를 성공했습니다")))
			.andExpect(jsonPath("data.stocks[0].companyName").value(equalTo(stock.getCompanyName())))
			.andExpect(jsonPath("data.stocks[0].tickerSymbol").value(equalTo(stock.getTickerSymbol())))
			.andExpect(jsonPath("data.stocks[0].lastPrice").value(equalTo(50000)))
			.andExpect(jsonPath("data.stocks[0].targetPrices[0].notificationId").value(equalTo(1)))
			.andExpect(jsonPath("data.stocks[0].targetPrices[0].targetPrice").value(equalTo(60000)))
			.andExpect(jsonPath("data.stocks[0].targetPrices[0].dateAdded").value(equalTo(now.toString())))
			.andExpect(jsonPath("data.stocks[0].targetPrices[1].notificationId").value(equalTo(2)))
			.andExpect(jsonPath("data.stocks[0].targetPrices[1].targetPrice").value(equalTo(70000)))
			.andExpect(jsonPath("data.stocks[0].targetPrices[1].dateAdded").value(equalTo(now.toString())))
			.andExpect(jsonPath("data.stocks[0].isActive").value(equalTo(true)))
			.andExpect(jsonPath("data.stocks[0].lastUpdated").value(equalTo(now.toString())));
	}

	@DisplayName("사용자는 특정 종목의 지정 알림가들을 조회합니다")
	@Test
	void searchTargetPriceNotifications() throws Exception {
		// given
		Stock stock = createStock();
		LocalDateTime now = LocalDateTime.now();
		given(service.searchTargetPriceNotifications(anyString(), anyLong()))
			.willReturn(TargetPriceNotificationSpecifiedSearchResponse.builder()
				.targetPrices(List.of(
					TargetPriceNotificationSpecificItem.builder()
						.notificationId(1L)
						.targetPrice(60000L)
						.dateAdded(now)
						.build(),
					TargetPriceNotificationSpecificItem.builder()
						.notificationId(2L)
						.targetPrice(70000L)
						.dateAdded(now)
						.build()
				))
				.build());

		// when & then
		mockMvc.perform(get("/api/stocks/{tickerSymbol}/target-price/notifications", stock.getTickerSymbol()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("종목 지정가 알림 특정 조회를 성공했습니다")))
			.andExpect(jsonPath("data.targetPrices[0].notificationId").value(equalTo(1)))
			.andExpect(jsonPath("data.targetPrices[0].targetPrice").value(equalTo(60000)))
			.andExpect(jsonPath("data.targetPrices[0].dateAdded").value(equalTo(now.toString())))
			.andExpect(jsonPath("data.targetPrices[1].notificationId").value(equalTo(2)))
			.andExpect(jsonPath("data.targetPrices[1].targetPrice").value(equalTo(70000)))
			.andExpect(jsonPath("data.targetPrices[1].dateAdded").value(equalTo(now.toString())));
	}

	@DisplayName("사용자는 종목 지정가 알림의 정보를 수정한다")
	@Test
	void updateStockTargetPriceNotification() throws Exception {
		// given
		Stock stock = createStock();
		Map<String, Object> body = Map.of(
			"tickerSymbol", stock.getTickerSymbol(),
			"isActive", false
		);

		given(service.updateStockTargetPriceNotification(any(TargetPriceNotificationUpdateRequest.class), anyLong()))
			.willReturn(TargetPriceNotificationUpdateResponse.builder()
				.stockTargetPriceId(1L)
				.tickerSymbol(stock.getTickerSymbol())
				.isActive(false)
				.build());

		// when & then
		mockMvc.perform(put("/api/stocks/target-price/notifications")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("종목 지정가 알림을 비 활성화하였습니다")));
	}

	@DisplayName("사용자는 유효하지 않은 입력으로 종목 지정가 알림의 정보를  수정할 수 없다")
	@Test
	void updateStockTargetPriceNotification_whenInvalidInput_thenResponse400Error() throws Exception {
		// given
		Map<String, Object> body = new HashMap<>();
		body.put("tickerSymbol", null);
		body.put("isActive", null);

		// when & then
		mockMvc.perform(put("/api/stocks/target-price/notifications")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data").isArray());
	}

	@DisplayName("사용자는 종목 지정가 알림들을 삭제합니다")
	@Test
	void deleteAllStockTargetPriceNotification() throws Exception {
		// given
		given(service.deleteStockTargetPriceNotification(
			anyLong(),
			anyLong()))
			.willReturn(TargetPriceNotificationDeleteResponse.builder()
				.deletedIds(List.of(1L, 2L))
				.build());

		Map<String, Object> body = new HashMap<>();
		body.put("tickerSymbol", "005930");
		body.put("targetPriceNotificationIds", List.of(1L, 2L));

		// when & then
		mockMvc.perform(delete("/api/stocks/target-price/notifications")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("해당 종목 지정가 알림을 제거했습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자는 유효하지 않은 입력 형식으로 종목 지정가를 삭제할 수 없다")
	@MethodSource(value = "invalidTargetPriceNotificationIds")
	@ParameterizedTest
	void deleteAllStockTargetPriceNotification_whenInvalidTargetPriceNotificationIds_thenResponse400Error(
		String tickerSymbol,
		List<Long> targetPriceNotificationIds)
		throws Exception {
		// given
		Map<String, Object> body = new HashMap<>();
		body.put("tickerSymbol", tickerSymbol);
		body.put("targetPriceNotificationIds", targetPriceNotificationIds);

		// when & then
		mockMvc.perform(delete("/api/stocks/target-price/notifications")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data").isArray());
	}

	@DisplayName("사용자는 종목 지정가 알림을 삭제합니다")
	@Test
	void deleteStockTargetPriceNotification() throws Exception {
		// given
		given(service.deleteStockTargetPriceNotification(
			anyLong(),
			anyLong()))
			.willReturn(TargetPriceNotificationDeleteResponse.builder()
				.deletedIds(List.of(1L))
				.build());

		Long targetPriceNotificationId = 1L;
		Map<String, Object> body = Map.of("tickerSymbol", "005930");
		// when & then
		mockMvc.perform(
				delete("/api/stocks/target-price/notifications/{targetPriceNotificationId}",
					targetPriceNotificationId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("해당 종목 지정가 알림을 제거했습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	private Member createMember() {
		return Member.builder()
			.id(1L)
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.password("kim1234@")
			.provider("local")
			.build();
	}

	private Stock createStock() {
		return Stock.builder()
			.companyName("삼성전자보통주")
			.tickerSymbol("005930")
			.companyNameEng("SamsungElectronics")
			.stockCode("KR7005930003")
			.sector("전기전자")
			.market(Market.KOSPI)
			.build();
	}

	public static Stream<Arguments> invalidTargetPrice() {
		return Stream.of(
			Arguments.of(null, -1L),
			Arguments.of(null, null)
		);
	}

	public static Stream<Arguments> invalidTargetPriceNotificationIds() {
		return Stream.of(
			Arguments.of(null, Collections.emptyList()),
			Arguments.of(null, null)
		);
	}
}
