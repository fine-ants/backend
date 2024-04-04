package codesquad.fineants.spring.docs.dashboard;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.JsonFieldType;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.spring.api.dashboard.controller.DashboardRestController;
import codesquad.fineants.spring.api.dashboard.response.DashboardLineChartResponse;
import codesquad.fineants.spring.api.dashboard.response.DashboardPieChartResponse;
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

		given(service.getOverview(ArgumentMatchers.any(AuthMember.class)))
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
			.andExpect(jsonPath("message").value(equalTo("오버뷰 데이터 조회가 완료되었습니다")))
			.andExpect(jsonPath("data.username").value(equalTo("일개미1234")))
			.andExpect(jsonPath("data.totalValuation").value(equalTo(1000000)))
			.andExpect(jsonPath("data.totalInvestment").value(equalTo(0)))
			.andExpect(jsonPath("data.totalGain").value(equalTo(0)))
			.andExpect(jsonPath("data.totalGainRate").value(closeTo(0.0, 0.1)))
			.andExpect(jsonPath("data.totalAnnualDividend").value(equalTo(0)))
			.andExpect(jsonPath("data.totalAnnualDividendYield").value(closeTo(0.0, 0.1)))
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

	@DisplayName("포트폴리오 파이 차트 API")
	@Test
	void readPieChart() throws Exception {
		// given
		given(service.getPieChart(ArgumentMatchers.any(AuthMember.class)))
			.willReturn(List.of(
				DashboardPieChartResponse.create(
					1L,
					"포트폴리오1",
					610888L,
					6.68,
					30022L,
					6.41
				)
			));

		// when & then
		mockMvc.perform(get("/api/dashboard/pieChart")
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 파이 차트 데이터 조회가 완료되었습니다")))
			.andExpect(jsonPath("data[0].id").value(equalTo(1)))
			.andExpect(jsonPath("data[0].name").value(equalTo("포트폴리오1")))
			.andExpect(jsonPath("data[0].valuation").value(equalTo(610888)))
			.andExpect(jsonPath("data[0].totalGain").value(equalTo(30022)))
			.andExpect(jsonPath("data[0].totalGainRate").value(equalTo(6.41)))
			.andExpect(jsonPath("data[0].weight").value(equalTo(6.68)))
			.andDo(
				document(
					"dashboard_portfolio_pie_chart-search",
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
							.description("응답 데이터"),
						fieldWithPath("data[].id").type(JsonFieldType.NUMBER)
							.description("포트폴리오 등록 번호"),
						fieldWithPath("data[].name").type(JsonFieldType.STRING)
							.description("포트폴리오 이름"),
						fieldWithPath("data[].valuation").type(JsonFieldType.NUMBER)
							.description("포트폴리오 가치"),
						fieldWithPath("data[].totalGain").type(JsonFieldType.NUMBER)
							.description("포트폴리오 총 손익"),
						fieldWithPath("data[].totalGainRate").type(JsonFieldType.NUMBER)
							.description("포트폴리오 총 손익율"),
						fieldWithPath("data[].weight").type(JsonFieldType.NUMBER)
							.description("포트폴리오 비중")
					)
				)
			);

	}

	@DisplayName("라인 차트 API")
	@Test
	void readLineChart() throws Exception {
		// given
		given(service.getLineChart(ArgumentMatchers.any(AuthMember.class)))
			.willReturn(List.of(
				DashboardLineChartResponse.of("2018-10-19", Money.from(5012346L)),
				DashboardLineChartResponse.of("2018-10-22", Money.from(4678901L))
			));

		// when & then
		mockMvc.perform(get("/api/dashboard/lineChart")
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("전체 평가액 데이터 조회가 완료되었습니다")))
			.andExpect(jsonPath("data[0].time").value(equalTo("2018-10-19")))
			.andExpect(jsonPath("data[0].value").value(equalTo(5012346)))
			.andExpect(jsonPath("data[1].time").value(equalTo("2018-10-22")))
			.andExpect(jsonPath("data[1].value").value(equalTo(4678901)))
			.andDo(
				document(
					"dashboard_line_chart-search",
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
							.description("응답 데이터"),
						fieldWithPath("data[].time").type(JsonFieldType.STRING)
							.description("일자"),
						fieldWithPath("data[].value").type(JsonFieldType.NUMBER)
							.description("가치")
					)
				)
			);

	}
}
