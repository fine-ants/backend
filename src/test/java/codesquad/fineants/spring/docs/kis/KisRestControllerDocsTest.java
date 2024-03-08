package codesquad.fineants.spring.docs.kis;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;

import codesquad.fineants.spring.api.kis.controller.KisRestController;
import codesquad.fineants.spring.api.kis.response.CurrentPriceResponse;
import codesquad.fineants.spring.api.kis.service.KisService;
import codesquad.fineants.spring.docs.RestDocsSupport;

public class KisRestControllerDocsTest extends RestDocsSupport {

	private final KisService service = Mockito.mock(KisService.class);

	@Override
	protected Object initController() {
		return new KisRestController(service);
	}

	@DisplayName("모든 종목 현재가 갱신 API")
	@Test
	void refreshAllStockCurrentPrice() throws Exception {
		// given
		given(service.refreshAllStockCurrentPrice())
			.willReturn(List.of(
				CurrentPriceResponse.create("005930", 60000L)
			));

		// when
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/kis/current-price/all/refresh")
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("종목 현재가가 갱신되었습니다")))
			.andExpect(jsonPath("data").isArray())
			.andExpect(jsonPath("data[0].tickerSymbol").value(equalTo("005930")))
			.andExpect(jsonPath("data[0].currentPrice").value(equalTo(60000)))
			.andDo(
				document(
					"kis_current_price-refresh",
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
						fieldWithPath("data").type(JsonFieldType.ARRAY)
							.description("종목 현재가 배열"),
						fieldWithPath("data[].tickerSymbol").type(JsonFieldType.STRING)
							.description("티커 심볼"),
						fieldWithPath("data[].currentPrice").type(JsonFieldType.NUMBER)
							.description("종목 현재가")
					)
				)
			);

	}
}
