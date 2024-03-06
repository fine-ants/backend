package codesquad.fineants.spring.docs.dashboard;

import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.JsonFieldType;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.spring.api.dashboard.controller.DashboardRestController;
import codesquad.fineants.spring.api.dashboard.response.OverviewResponse;
import codesquad.fineants.spring.api.dashboard.service.DashboardService;
import codesquad.fineants.spring.docs.RestDocsSupport;

public class DashboardRestControllerDocsTest extends RestDocsSupport {

	private final DashboardService service = Mockito.mock(DashboardService.class);

	@Override
	protected Object initController() {
		return new DashboardRestController(service);
	}

	@DisplayName("오버뷰 조회 API")
	@Test
	void readOverview() throws Exception {
		// given

		BDDMockito.given(service.getOverview(ArgumentMatchers.any(AuthMember.class)))
			.willReturn(OverviewResponse.builder()
				.username("일개미1234")
				.totalValuation(1000000L)
				.totalInvestment(0L)
				.totalGain(0L)
				.totalGainRate(0.0)
				.totalAnnualDividend(0L)
				.totalAnnualDividendYield(0.0)
				.build());

		// when & then
		mockMvc.perform(get("/api/dashboard/overview")
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("오버뷰 데이터 조회가 완료되었습니다.")))
			.andExpect(jsonPath("data.username").value(equalTo("일개미1234")))
			.andExpect(jsonPath("data.totalValuation").value(equalTo(1000000)))
			.andExpect(jsonPath("data.totalInvestment").value(equalTo(0)))
			.andExpect(jsonPath("data.totalGain").value(equalTo(0)))
			.andExpect(jsonPath("data.totalGainRate").value(equalTo(0)))
			.andExpect(jsonPath("data.totalAnnualDividend").value(equalTo(0)))
			.andExpect(jsonPath("data.totalAnnualDividendYield").value(equalTo(0.0)))
			.andDo(
				document(
					"dashboard_overview-search",
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
						fieldWithPath("data.username").type(JsonFieldType.STRING)
							.description("닉네임"),
						fieldWithPath("data.totalValuation").type(JsonFieldType.NUMBER)
							.description("총 금액"),
						fieldWithPath("data.totalInvestment").type(JsonFieldType.NUMBER)
							.description("총 투자 금액"),
						fieldWithPath("data.totalGain").type(JsonFieldType.NUMBER)
							.description("총 손익"),
						fieldWithPath("data.totalGainRate").type(JsonFieldType.NUMBER)
							.description("총 손익율"),
						fieldWithPath("data.totalAnnualDividend").type(JsonFieldType.NUMBER)
							.description("총 연배당금"),
						fieldWithPath("data.totalAnnualDividendYield").type(JsonFieldType.NUMBER)
							.description("총 연배당율")
					)
				)
			);

	}
}
