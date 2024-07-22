package codesquad.fineants.docs.target_price_notification;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import codesquad.fineants.docs.RestDocsSupport;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock_target_price.controller.TargetPriceNotificationRestController;
import codesquad.fineants.domain.stock_target_price.domain.dto.response.TargetPriceNotificationDeleteResponse;
import codesquad.fineants.domain.stock_target_price.domain.entity.StockTargetPrice;
import codesquad.fineants.domain.stock_target_price.domain.entity.TargetPriceNotification;
import codesquad.fineants.domain.stock_target_price.service.TargetPriceNotificationService;
import codesquad.fineants.global.util.ObjectMapperUtil;

public class TargetPriceNotificationRestControllerDocsTest extends RestDocsSupport {

	private final TargetPriceNotificationService service = Mockito.mock(TargetPriceNotificationService.class);

	@Override
	protected Object initController() {
		return new TargetPriceNotificationRestController(service);
	}

	@DisplayName("지정가 알림 다수 제거")
	@Test
	void deleteTargetPriceNotifications() throws Exception {
		// given
		given(service.deleteAllStockTargetPriceNotification(
			anyList(),
			anyString(),
			anyLong()
		)).willReturn(TargetPriceNotificationDeleteResponse.from(
			List.of(1L, 2L)
		));

		Map<String, Object> body = Map.of(
			"tickerSymbol", "005930",
			"targetPriceNotificationIds", List.of(1, 2)
		);

		// when & then
		mockMvc.perform(delete("/api/stocks/target-price/notifications")
				.cookie(createTokenCookies())
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("해당 종목 지정가 알림을 제거했습니다")))
			.andDo(
				document(
					"target_price_notification-multiple-delete",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestFields(
						fieldWithPath("tickerSymbol").type(JsonFieldType.STRING).description("종목 티커 심볼"),
						fieldWithPath("targetPriceNotificationIds").type(JsonFieldType.ARRAY)
							.description("지정가 알림 등록 번호")
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

	@DisplayName("지정가 알림 단일 제거")
	@Test
	void deleteTargetPriceNotification() throws Exception {
		// given
		Member member = createMember();
		Stock stock = createSamsungStock();
		StockTargetPrice stockTargetPrice = createStockTargetPrice(member, stock);
		TargetPriceNotification targetPriceNotification = createTargetPriceNotification(stockTargetPrice);

		given(service.deleteStockTargetPriceNotification(
			anyLong()
		)).willReturn(TargetPriceNotificationDeleteResponse.from(
			List.of(1L)
		));

		// when & then
		mockMvc.perform(delete("/api/stocks/target-price/notifications/{targetPriceNotificationId}",
				targetPriceNotification.getId())
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("해당 종목 지정가 알림을 제거했습니다")))
			.andDo(
				document(
					"target_price_notification-one-delete",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					pathParameters(
						parameterWithName("targetPriceNotificationId").description("지정가 알림 등록번호")
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
