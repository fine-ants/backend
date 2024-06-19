package codesquad.fineants.docs.portfolio;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.restdocs.payload.JsonFieldType;

import codesquad.fineants.docs.RestDocsSupport;
import codesquad.fineants.domain.common.money.Bank;
import codesquad.fineants.domain.common.money.Currency;
import codesquad.fineants.domain.common.money.Expression;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.portfolio.controller.DashboardRestController;
import codesquad.fineants.domain.portfolio.domain.dto.response.DashboardLineChartResponse;
import codesquad.fineants.domain.portfolio.domain.dto.response.DashboardPieChartResponse;
import codesquad.fineants.domain.portfolio.domain.dto.response.OverviewResponse;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.service.DashboardService;
import codesquad.fineants.domain.stock.domain.entity.Stock;

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

		given(service.getOverview(anyLong()))
			.willReturn(OverviewResponse.builder()
				.username("일개미1234")
				.totalValuation(Money.won(1000000L))
				.totalInvestment(Money.zero())
				.totalGain(Money.zero())
				.totalGainRate(Percentage.zero())
				.totalAnnualDividend(Money.zero())
				.totalAnnualDividendYield(Percentage.zero())
				.build());

		// when & then
		mockMvc.perform(get("/api/dashboard/overview")
				.cookie(createTokenCookies()))
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
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);
		holding.addPurchaseHistory(createPurchaseHistory(holding, LocalDateTime.now()));
		portfolio.addHolding(holding);

		Expression valuation = portfolio.calculateTotalAsset();
		given(service.getPieChart(anyLong()))
			.willReturn(List.of(
				DashboardPieChartResponse.create(
					1L,
					"포트폴리오1",
					valuation,
					portfolio.calculateTotalAsset()
						.divide(valuation)
						.toPercentage(Bank.getInstance(), Currency.KRW),
					portfolio.calculateTotalGain(),
					portfolio.calculateTotalGainRate().toPercentage(Bank.getInstance(), Currency.KRW)
				)
			));

		// when & then
		mockMvc.perform(get("/api/dashboard/pieChart")
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 파이 차트 데이터 조회가 완료되었습니다")))
			.andExpect(jsonPath("data[0].id").value(equalTo(1)))
			.andExpect(jsonPath("data[0].name").value(equalTo("포트폴리오1")))
			.andExpect(jsonPath("data[0].valuation").value(equalTo(1030000)))
			.andExpect(jsonPath("data[0].totalGain").value(equalTo(30000)))
			.andExpect(jsonPath("data[0].totalGainRate").value(equalTo(20.0)))
			.andExpect(jsonPath("data[0].weight").value(equalTo(100.0)))
			.andDo(
				document(
					"dashboard_portfolio_pie_chart-search",
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
		given(service.getLineChart(anyLong()))
			.willReturn(List.of(
				DashboardLineChartResponse.of("2018-10-19", Money.won(5012346L)),
				DashboardLineChartResponse.of("2018-10-22", Money.won(4678901L))
			));

		// when & then
		mockMvc.perform(get("/api/dashboard/lineChart")
				.cookie(createTokenCookies()))
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
