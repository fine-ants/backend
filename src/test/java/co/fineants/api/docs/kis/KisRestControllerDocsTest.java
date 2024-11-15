package co.fineants.api.docs.kis;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import co.fineants.api.docs.RestDocsSupport;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.controller.KisRestController;
import co.fineants.api.domain.kis.domain.dto.response.KisClosingPrice;
import co.fineants.api.domain.kis.domain.dto.response.KisSearchStockInfo;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.global.util.ObjectMapperUtil;
import reactor.core.publisher.Mono;

class KisRestControllerDocsTest extends RestDocsSupport {

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
				KisCurrentPrice.create("005930", 60000L)
			));

		// when
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/kis/current-price/all/refresh")
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("종목 현재가가 갱신되었습니다")))
			.andExpect(jsonPath("data").isArray())
			.andExpect(jsonPath("data[0].tickerSymbol").value(equalTo("005930")))
			.andExpect(jsonPath("data[0].price").value(equalTo(60000)))
			.andDo(
				document(
					"kis_current_price-all-refresh",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
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
						fieldWithPath("data[].price").type(JsonFieldType.NUMBER)
							.description("종목 현재가")
					)
				)
			);
	}

	@DisplayName("특정 종목 현재가 갱신 API")
	@Test
	void refreshStockCurrentPrice() throws Exception {
		// given
		List<String> tickerSymbols = List.of("005930");
		given(service.refreshStockCurrentPrice(tickerSymbols))
			.willReturn(List.of(
				KisCurrentPrice.create("005930", 60000L)
			));

		Map<String, Object> body = Map.of(
			"tickerSymbols", tickerSymbols
		);

		// when
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/kis/current-price/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body))
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("종목 현재가가 갱신되었습니다")))
			.andExpect(jsonPath("data").isArray())
			.andExpect(jsonPath("data[0].tickerSymbol").value(equalTo("005930")))
			.andExpect(jsonPath("data[0].price").value(equalTo(60000)))
			.andDo(
				document(
					"kis_current_price-refresh",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestFields(
						fieldWithPath("tickerSymbols").type(JsonFieldType.ARRAY).description("티커 심볼 리스트")
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
						fieldWithPath("data[].price").type(JsonFieldType.NUMBER)
							.description("종목 현재가")
					)
				)
			);
	}

	@DisplayName("특정 종목 현재가 조회 API")
	@Test
	void fetchCurrentPrice() throws Exception {
		// given
		String tickerSymbol = "005930";
		given(service.fetchCurrentPrice(tickerSymbol))
			.willReturn(Mono.just(KisCurrentPrice.create("005930", 60000L)));

		// when
		MvcResult mvcResult = mockMvc.perform(
				RestDocumentationRequestBuilders.get("/api/kis/current-price/{tickerSymbol}", tickerSymbol)
					.cookie(createTokenCookies()))
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(mvcResult))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("종목 현재가가 조회되었습니다")))
			.andExpect(jsonPath("data.tickerSymbol").value(equalTo("005930")))
			.andExpect(jsonPath("data.price").value(equalTo(60000)))
			.andDo(
				document(
					"kis_current_price-search",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					pathParameters(
						parameterWithName("tickerSymbol").description("티커 심볼")
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
						fieldWithPath("data.tickerSymbol").type(JsonFieldType.STRING)
							.description("티커 심볼"),
						fieldWithPath("data.price").type(JsonFieldType.NUMBER)
							.description("종목 현재가")
					)
				)
			);
	}

	@DisplayName("종목 기본 정보 조회 API")
	@Test
	void fetchStockInfo() throws Exception {
		// given
		String tickerSymbol = "005930";
		Stock samsung = createSamsungStock();
		KisSearchStockInfo kisSearchStockInfo = KisSearchStockInfo.delistedStock(
			samsung.getStockCode(),
			samsung.getTickerSymbol(),
			samsung.getCompanyName(),
			samsung.getCompanyNameEng(),
			"STK",
			"시가총액규모대",
			"전기전자",
			samsung.getSector(),
			LocalDate.of(2024, 11, 15)
		);
		given(service.fetchSearchStockInfo(tickerSymbol))
			.willReturn(Mono.just(kisSearchStockInfo));

		// when
		MvcResult mvcResult = mockMvc.perform(
				RestDocumentationRequestBuilders.get("/api/kis/stock-info/{tickerSymbol}", tickerSymbol)
					.cookie(createTokenCookies()))
			.andExpect(request().asyncStarted())
			.andReturn();

		mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(mvcResult))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("종목 기본 정보가 조회되었습니다")))
			.andExpect(jsonPath("data.delisted").value(equalTo(true)))
			.andExpect(jsonPath("data.stockCode").value(equalTo("KR7005930003")))
			.andExpect(jsonPath("data.tickerSymbol").value(equalTo("005930")))
			.andExpect(jsonPath("data.companyName").value(equalTo("삼성전자보통주")))
			.andExpect(jsonPath("data.companyEngName").value(equalTo("SamsungElectronics")))
			.andExpect(jsonPath("data.marketIdCode").value(equalTo("STK")))
			.andExpect(jsonPath("data.majorSector").value(equalTo("시가총액규모대")))
			.andExpect(jsonPath("data.midSector").value(equalTo("전기전자")))
			.andExpect(jsonPath("data.subSector").value(equalTo("전기전자")))
			.andExpect(jsonPath("data.delistedDate").value(equalTo("2024-11-15")))
			.andDo(
				document(
					"kis_stock_info-search",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					pathParameters(
						parameterWithName("tickerSymbol").description("티커 심볼")
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
						fieldWithPath("data.delisted").type(JsonFieldType.BOOLEAN)
							.description("상장 폐지 여부"),
						fieldWithPath("data.stockCode").type(JsonFieldType.STRING)
							.description("종목 코드"),
						fieldWithPath("data.tickerSymbol").type(JsonFieldType.STRING)
							.description("티커 심볼"),
						fieldWithPath("data.companyName").type(JsonFieldType.STRING)
							.description("종목명"),
						fieldWithPath("data.companyEngName").type(JsonFieldType.STRING)
							.description("종목 영문명"),
						fieldWithPath("data.marketIdCode").type(JsonFieldType.STRING)
							.description("시장 코드"),
						fieldWithPath("data.majorSector").type(JsonFieldType.STRING)
							.description("메이저 섹터"),
						fieldWithPath("data.midSector").type(JsonFieldType.STRING)
							.description("미들 섹터"),
						fieldWithPath("data.subSector").type(JsonFieldType.STRING)
							.description("서브 섹터"),
						fieldWithPath("data.delistedDate").type(JsonFieldType.STRING)
							.description("상장 폐지 일자")
					)
				)
			);
	}

	@DisplayName("모든 종목 종가 갱신 API")
	@Test
	void refreshAllLastDayClosingPrice() throws Exception {
		// given
		given(service.refreshAllClosingPrice())
			.willReturn(List.of(
				KisClosingPrice.create("005930", 60000L)
			));

		// when
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/kis/closing-price/all/refresh")
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("종목 종가가 갱신되었습니다")))
			.andExpect(jsonPath("data").isArray())
			.andExpect(jsonPath("data[0].tickerSymbol").value(equalTo("005930")))
			.andExpect(jsonPath("data[0].price").value(equalTo(60000)))
			.andDo(
				document(
					"kis_closing_price-all-refresh",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.ARRAY)
							.description("종목 종가 배열"),
						fieldWithPath("data[].tickerSymbol").type(JsonFieldType.STRING)
							.description("티커 심볼"),
						fieldWithPath("data[].price").type(JsonFieldType.NUMBER)
							.description("종목 종가")
					)
				)
			);
	}

	@DisplayName("일부 종목 종가 갱신 API")
	@Test
	void refreshLastDayClosingPrice() throws Exception {
		// given
		List<String> tickerSymbols = List.of("005930");

		given(service.refreshClosingPrice(tickerSymbols))
			.willReturn(List.of(
				KisClosingPrice.create("005930", 60000L)
			));

		Map<String, Object> body = Map.of(
			"tickerSymbols", tickerSymbols
		);
		// when
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/kis/closing-price/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body))
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("종목 종가가 갱신되었습니다")))
			.andExpect(jsonPath("data").isArray())
			.andExpect(jsonPath("data[0].tickerSymbol").value(equalTo("005930")))
			.andExpect(jsonPath("data[0].price").value(equalTo(60000)))
			.andDo(
				document(
					"kis_closing_price-refresh",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestFields(
						fieldWithPath("tickerSymbols").type(JsonFieldType.ARRAY).description("티커 심볼 리스트")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.ARRAY)
							.description("종목 종가 배열"),
						fieldWithPath("data[].tickerSymbol").type(JsonFieldType.STRING)
							.description("티커 심볼"),
						fieldWithPath("data[].price").type(JsonFieldType.NUMBER)
							.description("종목 종가")
					)
				)
			);
	}
}
