package codesquad.fineants.spring.docs.stock.stock_target_price;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.request.RequestDocumentation;

import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.spring.api.stock.StockTargetPriceNotificationRestController;
import codesquad.fineants.spring.api.stock.StockTargetPriceNotificationService;
import codesquad.fineants.spring.api.stock.request.TargetPriceNotificationCreateRequest;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationCreateResponse;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationSpecificItem;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationSpecifiedSearchResponse;
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
				.content(ObjectMapperUtil.serialize(body)))
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
		mockMvc.perform(get("/api/stocks/{tickerSymbol}/target-price/notifications",
				stock.getTickerSymbol()))
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
