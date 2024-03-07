package codesquad.fineants.spring.docs.portfolio;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.anyLong;
import static org.mockito.BDDMockito.isNull;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.spring.api.portfolio.PortFolioRestController;
import codesquad.fineants.spring.api.portfolio.PortFolioService;
import codesquad.fineants.spring.api.portfolio.request.PortfolioCreateRequest;
import codesquad.fineants.spring.api.portfolio.request.PortfolioModifyRequest;
import codesquad.fineants.spring.api.portfolio.response.PortFolioCreateResponse;
import codesquad.fineants.spring.api.portfolio.response.PortFolioItem;
import codesquad.fineants.spring.api.portfolio.response.PortfolioModifyResponse;
import codesquad.fineants.spring.api.portfolio.response.PortfoliosResponse;
import codesquad.fineants.spring.docs.RestDocsSupport;
import codesquad.fineants.spring.util.ObjectMapperUtil;

public class PortfolioRestControllerDocsTest extends RestDocsSupport {

	private final PortFolioService portFolioService = Mockito.mock(PortFolioService.class);

	@Override
	protected Object initController() {
		return new PortFolioRestController(portFolioService);
	}

	@DisplayName("포트폴리오 생성 API")
	@Test
	void createPortfolio() throws Exception {
		// given
		Member member = createMember();
		given(portFolioService.createPortfolio(
			any(PortfolioCreateRequest.class),
			any(AuthMember.class)))
			.willReturn(PortFolioCreateResponse.from(createPortfolio(member)));

		Map<String, Object> body = new HashMap<>();
		body.put("name", "내꿈은 워렌버핏");
		body.put("securitiesFirm", "토스");
		body.put("budget", 1000000L);
		body.put("targetGain", 1500000L);
		body.put("maximumLoss", 900000L);

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/portfolios")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body))
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오가 추가되었습니다")))
			.andExpect(jsonPath("data.portfolioId").value(equalTo(1)))
			.andDo(
				document(
					"portfolio-create",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					requestFields(
						fieldWithPath("name").type(JsonFieldType.STRING).description("이름"),
						fieldWithPath("securitiesFirm").type(JsonFieldType.STRING).description("증권사"),
						fieldWithPath("budget").type(JsonFieldType.NUMBER).description("예산"),
						fieldWithPath("targetGain").type(JsonFieldType.NUMBER).description("목표 수익 금액"),
						fieldWithPath("maximumLoss").type(JsonFieldType.NUMBER).description("최대 손실 금액")
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
						fieldWithPath("data.portfolioId").type(JsonFieldType.NUMBER)
							.description("포트폴리오 등록번호")
					)
				)
			);
	}

	@DisplayName("포트폴리오 목록 조회 API")
	@Test
	void searchMyAllPortfolios() throws Exception {
		// given
		PortFolioItem portFolioItem = PortFolioItem.builder()
			.id(1L)
			.securitiesFirm("토스증권")
			.name("내꿈은 워렌버핏")
			.budget(1000000L)
			.totalGain(100000L)
			.totalGainRate(10.00)
			.dailyGain(100000L)
			.dailyGainRate(10.00)
			.currentValuation(100000L)
			.expectedMonthlyDividend(20000L)
			.numShares(9)
			.dateCreated(LocalDateTime.now())
			.build();
		given(portFolioService.readMyAllPortfolio(any(AuthMember.class)))
			.willReturn(PortfoliosResponse.builder()
				.portfolios(List.of(portFolioItem))
				.build());

		// when & then
		mockMvc.perform(get("/api/portfolios")
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 목록 조회가 완료되었습니다")))
			.andExpect(jsonPath("data.portfolios[0].id").value(equalTo(portFolioItem.getId().intValue())))
			.andExpect(jsonPath("data.portfolios[0].securitiesFirm").value(equalTo(portFolioItem.getSecuritiesFirm())))
			.andExpect(jsonPath("data.portfolios[0].name").value(equalTo(portFolioItem.getName())))
			.andExpect(jsonPath("data.portfolios[0].budget").value(equalTo(portFolioItem.getBudget().intValue())))
			.andExpect(jsonPath("data.portfolios[0].totalGain").value(equalTo(portFolioItem.getTotalGain().intValue())))
			.andExpect(jsonPath("data.portfolios[0].totalGainRate").value(equalTo(portFolioItem.getTotalGainRate())))
			.andExpect(jsonPath("data.portfolios[0].dailyGain").value(equalTo(portFolioItem.getDailyGain().intValue())))
			.andExpect(jsonPath("data.portfolios[0].dailyGainRate").value(equalTo(portFolioItem.getDailyGainRate())))
			.andExpect(
				jsonPath("data.portfolios[0].currentValuation").value(
					equalTo(portFolioItem.getCurrentValuation().intValue())))
			.andExpect(jsonPath("data.portfolios[0].expectedMonthlyDividend").value(
				equalTo(portFolioItem.getExpectedMonthlyDividend().intValue())))
			.andExpect(jsonPath("data.portfolios[0].numShares").value(equalTo(portFolioItem.getNumShares())))
			.andExpect(jsonPath("data.portfolios[0].dateCreated").isNotEmpty())
			.andDo(
				document(
					"portfolio-search",
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
						fieldWithPath("data.portfolios[].id").type(JsonFieldType.NUMBER)
							.description("포트폴리오 등록번호"),
						fieldWithPath("data.portfolios[].securitiesFirm").type(JsonFieldType.STRING)
							.description("증권사"),
						fieldWithPath("data.portfolios[].name").type(JsonFieldType.STRING)
							.description("포트폴리오 이름"),
						fieldWithPath("data.portfolios[].budget").type(JsonFieldType.NUMBER)
							.description("예산"),
						fieldWithPath("data.portfolios[].totalGain").type(JsonFieldType.NUMBER)
							.description("총손익"),
						fieldWithPath("data.portfolios[].totalGainRate").type(JsonFieldType.NUMBER)
							.description("총손익율"),
						fieldWithPath("data.portfolios[].dailyGain").type(JsonFieldType.NUMBER)
							.description("일일손익"),
						fieldWithPath("data.portfolios[].dailyGainRate").type(JsonFieldType.NUMBER)
							.description("일일손익율"),
						fieldWithPath("data.portfolios[].currentValuation").type(JsonFieldType.NUMBER)
							.description("현재평가금액"),
						fieldWithPath("data.portfolios[].expectedMonthlyDividend").type(JsonFieldType.NUMBER)
							.description("예상월배당금"),
						fieldWithPath("data.portfolios[].numShares").type(JsonFieldType.NUMBER)
							.description("주식 총개수"),
						fieldWithPath("data.portfolios[].dateCreated").type(JsonFieldType.STRING)
							.description("추가일자")
					)
				)
			);
	}

	@DisplayName("포트폴리오 수정 API")
	@Test
	void updatePortfolio() throws Exception {
		// given
		Member member = createMember();
		Portfolio portfolio = createPortfolio(member);
		given(portFolioService.updatePortfolio(
			any(PortfolioModifyRequest.class),
			anyLong(),
			any(AuthMember.class)))
			.willReturn(PortfolioModifyResponse.from(portfolio));

		Map<String, Object> body = new HashMap<>();
		body.put("name", "내꿈은 워렌버핏");
		body.put("securitiesFirm", "토스");
		body.put("budget", 1000000L);
		body.put("targetGain", 1500000L);
		body.put("maximumLoss", 900000L);

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.put("/api/portfolios/{portfolioId}", portfolio.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body))
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오가 수정되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(isNull())))
			.andDo(
				document(
					"portfolio-update",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					pathParameters(
						parameterWithName("portfolioId").description("포트폴리오 등록번호")
					),
					requestFields(
						fieldWithPath("name").type(JsonFieldType.STRING).description("이름"),
						fieldWithPath("securitiesFirm").type(JsonFieldType.STRING).description("증권사"),
						fieldWithPath("budget").type(JsonFieldType.NUMBER).description("예산"),
						fieldWithPath("targetGain").type(JsonFieldType.NUMBER).description("목표 수익 금액"),
						fieldWithPath("maximumLoss").type(JsonFieldType.NUMBER).description("최대 손실 금액")
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

	@DisplayName("포트폴리오 단일 삭제 API")
	@Test
	void deletePortfolio() throws Exception {
		// given
		Member member = createMember();
		Portfolio portfolio = createPortfolio(member);

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/portfolios/{portfolioId}", portfolio.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 삭제가 완료되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(isNull())))
			.andDo(
				document(
					"portfolio-one-delete",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					pathParameters(
						parameterWithName("portfolioId").description("포트폴리오 등록번호")
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

	@DisplayName("포트폴리오 다수 삭제 API")
	@Test
	void deletePortfolios() throws Exception {
		// given
		Member member = createMember();
		Portfolio portfolio = createPortfolio(member);

		Map<String, Object> body = Map.of(
			"portfolioIds", List.of(portfolio.getId())
		);
		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/portfolios")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body))
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 삭제가 완료되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(isNull())))
			.andDo(
				document(
					"portfolio-multiple-delete",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					requestFields(
						fieldWithPath("portfolioIds").type(JsonFieldType.ARRAY).description("포트폴리오 등록번호 리스트")
					)
					,
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
