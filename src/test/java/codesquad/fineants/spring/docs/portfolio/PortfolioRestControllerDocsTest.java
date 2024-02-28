package codesquad.fineants.spring.docs.portfolio;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.restdocs.payload.JsonFieldType;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.spring.api.portfolio.PortFolioRestController;
import codesquad.fineants.spring.api.portfolio.PortFolioService;
import codesquad.fineants.spring.api.portfolio.response.PortFolioItem;
import codesquad.fineants.spring.api.portfolio.response.PortfoliosResponse;
import codesquad.fineants.spring.docs.RestDocsSupport;

public class PortfolioRestControllerDocsTest extends RestDocsSupport {

	private final PortFolioService portFolioService = Mockito.mock(PortFolioService.class);

	@Override
	protected Object initController() {
		return new PortFolioRestController(portFolioService);
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
			.totalGainRate(10)
			.dailyGain(100000L)
			.dailyGainRate(10)
			.currentValuation(100000L)
			.expectedMonthlyDividend(20000L)
			.dateCreated(LocalDateTime.now())
			.build();
		given(portFolioService.readMyAllPortfolio(any(AuthMember.class)))
			.willReturn(PortfoliosResponse.builder()
				.portfolios(List.of(portFolioItem))
				.build());

		// when & then
		mockMvc.perform(get("/api/portfolios"))
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
						fieldWithPath("data.portfolios[].title").type(JsonFieldType.STRING)
							.description("알림 제목"),
						fieldWithPath("data.portfolios[].isRead").type(JsonFieldType.BOOLEAN)
							.description("읽음 여부"),
						fieldWithPath("data.portfolios[].type").type(JsonFieldType.STRING)
							.description("알림 타입"),
						fieldWithPath("data.portfolios[].referenceId").type(JsonFieldType.STRING)
							.description("참조 등록번호"),
						fieldWithPath("data.portfolios[].messageId").type(JsonFieldType.STRING)
							.description("알림 메시지 등록번호")
					)
				)
			);
	}
}
