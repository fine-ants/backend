package codesquad.fineants.docs.purchase_history;

import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import codesquad.fineants.docs.RestDocsSupport;
import codesquad.fineants.domain.holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.purchasehistory.controller.PurchaseHistoryRestController;
import codesquad.fineants.domain.purchasehistory.domain.entity.PurchaseHistory;
import codesquad.fineants.domain.purchasehistory.service.PurchaseHistoryService;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.global.util.ObjectMapperUtil;

public class PurchaseHistoryRestControllerDocsTest extends RestDocsSupport {

	private final PurchaseHistoryService service = Mockito.mock(PurchaseHistoryService.class);

	@Override
	protected Object initController() {
		return new PurchaseHistoryRestController(service);
	}

	@DisplayName("매입 이력 생성 API")
	@Test
	void createPurchaseHistory() throws Exception {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);

		Map<String, Object> body = Map.of(
			"purchaseDate", "2023-10-23T13:00:00",
			"numShares", 3,
			"purchasePricePerShare", 50000,
			"memo", "첫구매"
		);
		// when & then
		mockMvc.perform(
				post("/api/portfolio/{portfolioId}/holdings/{portfolioHoldingId}/purchaseHistory", portfolio.getId(),
					holding.getId())
					.cookie(createTokenCookies())
					.contentType(MediaType.APPLICATION_JSON)
					.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo("매입 이력이 추가되었습니다")))
			.andDo(
				document(
					"purchase_history-create",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					pathParameters(
						parameterWithName("portfolioId").description("포트폴리오 등록번호"),
						parameterWithName("portfolioHoldingId").description("포트폴리오 종목 등록번호")
					),
					requestFields(
						fieldWithPath("purchaseDate").type(JsonFieldType.STRING).description("매입 일자"),
						fieldWithPath("numShares").type(JsonFieldType.NUMBER).description("매입 개수"),
						fieldWithPath("purchasePricePerShare").type(JsonFieldType.NUMBER).description("평균 매입가"),
						fieldWithPath("memo").type(JsonFieldType.STRING).description("메모").optional()
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

	@DisplayName("매입 이력 수정 API")
	@Test
	void updatePurchaseHistory() throws Exception {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		PurchaseHistory history = createPurchaseHistory(holding, LocalDateTime.of(2023, 10, 23, 13, 0, 0));

		Map<String, Object> body = Map.of(
			"purchaseDate", "2023-10-23T13:00:00",
			"numShares", 3,
			"purchasePricePerShare", 50000,
			"memo", "첫구매"
		);
		// when & then
		mockMvc.perform(
				put("/api/portfolio/{portfolioId}/holdings/{portfolioHoldingId}/purchaseHistory/{purchaseHistoryId}",
					portfolio.getId(),
					holding.getId(),
					history.getId())
					.cookie(createTokenCookies())
					.contentType(MediaType.APPLICATION_JSON)
					.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("매입 이력이 수정되었습니다")))
			.andDo(
				document(
					"purchase_history-update",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					pathParameters(
						parameterWithName("portfolioId").description("포트폴리오 등록번호"),
						parameterWithName("portfolioHoldingId").description("포트폴리오 종목 등록번호"),
						parameterWithName("purchaseHistoryId").description("매입이력 등록번호")
					),
					requestFields(
						fieldWithPath("purchaseDate").type(JsonFieldType.STRING).description("매입 일자"),
						fieldWithPath("numShares").type(JsonFieldType.NUMBER).description("매입 개수"),
						fieldWithPath("purchasePricePerShare").type(JsonFieldType.NUMBER).description("평균 매입가"),
						fieldWithPath("memo").type(JsonFieldType.STRING).description("메모").optional()
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

	@DisplayName("매입 이력 삭제 API")
	@Test
	void deletePurchaseHistory() throws Exception {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		PurchaseHistory history = createPurchaseHistory(holding, LocalDateTime.of(2023, 10, 23, 13, 0, 0));

		// when & then
		mockMvc.perform(
				delete("/api/portfolio/{portfolioId}/holdings/{portfolioHoldingId}/purchaseHistory/{purchaseHistoryId}",
					portfolio.getId(),
					holding.getId(),
					history.getId())
					.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("매입 이력이 삭제되었습니다")))
			.andDo(
				document(
					"purchase_history-delete",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					pathParameters(
						parameterWithName("portfolioId").description("포트폴리오 등록번호"),
						parameterWithName("portfolioHoldingId").description("포트폴리오 종목 등록번호"),
						parameterWithName("purchaseHistoryId").description("매입이력 등록번호")
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
