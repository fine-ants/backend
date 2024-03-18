package codesquad.fineants.spring.docs.stock.stock_target_price;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.request.RequestDocumentation;

import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.spring.api.stock_target_price.controller.StockTargetPriceNotificationRestController;
import codesquad.fineants.spring.api.stock_target_price.request.TargetPriceNotificationCreateRequest;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceItem;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotificationCreateResponse;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotificationSearchItem;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotificationSearchResponse;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotificationSpecificItem;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotificationSpecifiedSearchResponse;
import codesquad.fineants.spring.api.stock_target_price.service.StockTargetPriceNotificationService;
import codesquad.fineants.spring.docs.RestDocsSupport;
import codesquad.fineants.spring.util.ObjectMapperUtil;

public class StockTargetPriceNotificationRestControllerDocsTest extends RestDocsSupport {

	private final StockTargetPriceNotificationService service = Mockito.mock(StockTargetPriceNotificationService.class);

	@Override
	protected Object initController() {
		return new StockTargetPriceNotificationRestController(service);
	}

	@DisplayName("종목 지정가 알림 추가 API")
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
				.content(ObjectMapperUtil.serialize(body))
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo("해당 종목 지정가 알림을 추가했습니다")))
			.andExpect(jsonPath("data.targetPriceNotificationId").value(equalTo(1)))
			.andExpect(jsonPath("data.tickerSymbol").value(equalTo("005930")))
			.andExpect(jsonPath("data.targetPrice").value(equalTo(60000)))
			.andDo(
				document(
					"stock_target_price-create",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					requestFields(
						fieldWithPath("tickerSymbol").type(JsonFieldType.STRING)
							.description("종목 티커 심볼"),
						fieldWithPath("targetPrice").type(JsonFieldType.NUMBER)
							.description("종목 지정가")
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
						fieldWithPath("data.targetPriceNotificationId").type(JsonFieldType.NUMBER)
							.description("지정가 알림 등록번호"),
						fieldWithPath("data.tickerSymbol").type(JsonFieldType.STRING)
							.description("종목 티커 심볼"),
						fieldWithPath("data.targetPrice").type(JsonFieldType.NUMBER)
							.description("종목 지정가")
					)
				)
			);
	}

	@DisplayName("종목 지정가 알림 목록 조회 API")
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
		mockMvc.perform(get("/api/stocks/target-price/notifications")
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("모든 알림 조회를 성공했습니다")))
			.andExpect(jsonPath("data.stocks[0].companyName").value(equalTo(stock.getCompanyName())))
			.andExpect(jsonPath("data.stocks[0].tickerSymbol").value(equalTo(stock.getTickerSymbol())))
			.andExpect(jsonPath("data.stocks[0].lastPrice").value(equalTo(50000)))
			.andExpect(jsonPath("data.stocks[0].targetPrices[0].notificationId").value(equalTo(1)))
			.andExpect(jsonPath("data.stocks[0].targetPrices[0].targetPrice").value(equalTo(60000)))
			.andExpect(jsonPath("data.stocks[0].targetPrices[0].dateAdded").isNotEmpty())
			.andExpect(jsonPath("data.stocks[0].targetPrices[1].notificationId").value(equalTo(2)))
			.andExpect(jsonPath("data.stocks[0].targetPrices[1].targetPrice").value(equalTo(70000)))
			.andExpect(jsonPath("data.stocks[0].targetPrices[1].dateAdded").isNotEmpty())
			.andExpect(jsonPath("data.stocks[0].isActive").value(equalTo(true)))
			.andExpect(jsonPath("data.stocks[0].lastUpdated").isNotEmpty())
			.andDo(
				document(
					"stock_target_price-list-search",
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
						fieldWithPath("data").type(JsonFieldType.OBJECT)
							.description("응답 데이터"),
						fieldWithPath("data.stocks[]").type(JsonFieldType.ARRAY)
							.description("종목 지정가 알림 데이터"),
						fieldWithPath("data.stocks[].companyName").type(JsonFieldType.STRING)
							.description("종목 이름"),
						fieldWithPath("data.stocks[].tickerSymbol").type(JsonFieldType.STRING)
							.description("티커 심볼"),
						fieldWithPath("data.stocks[].lastPrice").type(JsonFieldType.NUMBER)
							.description("종가"),
						fieldWithPath("data.stocks[].isActive").type(JsonFieldType.BOOLEAN)
							.description("알림 활성화 여부"),
						fieldWithPath("data.stocks[].lastUpdated").type(JsonFieldType.STRING)
							.description("최근 갱신 일자"),
						fieldWithPath("data.stocks[].targetPrices[]").type(JsonFieldType.ARRAY)
							.description("종목 지정가 데이터"),
						fieldWithPath("data.stocks[].targetPrices[].notificationId").type(JsonFieldType.NUMBER)
							.description("알림 등록번호"),
						fieldWithPath("data.stocks[].targetPrices[].targetPrice").type(JsonFieldType.NUMBER)
							.description("지정가"),
						fieldWithPath("data.stocks[].targetPrices[].dateAdded").type(JsonFieldType.STRING)
							.description("생성일자")
					)
				)
			);
	}

	@DisplayName("종목 지정가 알림 특정 조회 API")
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
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/stocks/{tickerSymbol}/target-price/notifications",
					stock.getTickerSymbol())
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("종목 지정가 알림 특정 조회를 성공했습니다")))
			.andExpect(jsonPath("data.targetPrices[0].notificationId").value(equalTo(1)))
			.andExpect(jsonPath("data.targetPrices[0].targetPrice").value(equalTo(60000)))
			.andExpect(jsonPath("data.targetPrices[0].dateAdded").isNotEmpty())
			.andExpect(jsonPath("data.targetPrices[1].notificationId").value(equalTo(2)))
			.andExpect(jsonPath("data.targetPrices[1].targetPrice").value(equalTo(70000)))
			.andExpect(jsonPath("data.targetPrices[1].dateAdded").isNotEmpty())
			.andDo(
				document(
					"stock_target_price-one-search",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					RequestDocumentation.pathParameters(
						RequestDocumentation.parameterWithName("tickerSymbol").description("종목 티커 심볼")
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
						fieldWithPath("data.targetPrices[].notificationId").type(JsonFieldType.NUMBER)
							.description("지정가 알림 등록번호"),
						fieldWithPath("data.targetPrices[].targetPrice").type(JsonFieldType.NUMBER)
							.description("지정가"),
						fieldWithPath("data.targetPrices[].dateAdded").type(JsonFieldType.STRING)
							.description("생성 일자")
					)
				)
			);
	}
}
