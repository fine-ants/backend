package co.fineants.api.domain.stock_target_price.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import co.fineants.api.ControllerTestSupport;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceNotificationDeleteResponse;
import co.fineants.api.domain.stock_target_price.service.TargetPriceNotificationService;
import co.fineants.api.global.util.ObjectMapperUtil;

@WebMvcTest(controllers = TargetPriceNotificationRestController.class)
class TargetPriceNotificationRestControllerTest extends ControllerTestSupport {

	@MockBean
	private TargetPriceNotificationService service;

	@Override
	protected Object initController() {
		return new TargetPriceNotificationRestController(service);
	}

	@DisplayName("사용자는 종목 지정가 알림들을 삭제합니다")
	@Test
	void deleteAllStockTargetPriceNotification() throws Exception {
		// given
		given(service.deleteStockTargetPriceNotification(
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

	public static Stream<Arguments> invalidTargetPriceNotificationIds() {
		return Stream.of(
			Arguments.of(null, Collections.emptyList()),
			Arguments.of(null, null)
		);
	}
}
