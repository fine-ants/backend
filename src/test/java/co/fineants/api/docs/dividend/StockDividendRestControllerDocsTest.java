package co.fineants.api.docs.dividend;

import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.restdocs.payload.JsonFieldType;

import co.fineants.api.docs.RestDocsSupport;
import co.fineants.api.domain.dividend.controller.StockDividendRestController;
import co.fineants.api.domain.dividend.service.StockDividendService;
import co.fineants.api.global.success.StockDividendSuccessCode;
import co.fineants.api.infra.s3.service.AmazonS3DividendService;

class StockDividendRestControllerDocsTest extends RestDocsSupport {

	@Override
	protected Object initController() {
		StockDividendService service = Mockito.mock(StockDividendService.class);
		AmazonS3DividendService s3DividendService = Mockito.mock(AmazonS3DividendService.class);
		return new StockDividendRestController(service, s3DividendService);
	}

	@DisplayName("관리자는 배당일정을 초기화한다")
	@Test
	void initializeStockDividend() throws Exception {
		// given

		// when & then
		mockMvc.perform(post("/api/dividends/init")
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo(StockDividendSuccessCode.OK_INIT_DIVIDENDS.getMessage())))
			.andExpect(jsonPath("data").value(equalTo(null)))
			.andDo(
				document(
					"dividend-schedule-init",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
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

	@DisplayName("관리자는 배당일정을 최신화한다")
	@Test
	void refreshStockDividend() throws Exception {
		// given

		// when & then
		mockMvc.perform(post("/api/dividends/refresh")
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo(StockDividendSuccessCode.OK_REFRESH_DIVIDENDS.getMessage())))
			.andExpect(jsonPath("data").value(equalTo(null)))
			.andDo(
				document(
					"dividend-schedule-refresh",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
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

	@DisplayName("관리자는 배당일정을 S3에 작성한다")
	@Test
	void writeDividendCsvToS3() throws Exception {
		// given

		// when & then
		mockMvc.perform(post("/api/dividends/write/csv")
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo(StockDividendSuccessCode.OK_WRITE_DIVIDENDS_CSV.getMessage())))
			.andExpect(jsonPath("data").value(equalTo(null)))
			.andDo(
				document(
					"dividend-schedule-write-csv",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
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
