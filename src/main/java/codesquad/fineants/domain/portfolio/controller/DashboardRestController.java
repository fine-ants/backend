package codesquad.fineants.domain.portfolio.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalMember;
import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.success.DashboardSuccessCode;
import codesquad.fineants.domain.portfolio.domain.dto.response.DashboardLineChartResponse;
import codesquad.fineants.domain.portfolio.domain.dto.response.DashboardPieChartResponse;
import codesquad.fineants.domain.portfolio.domain.dto.response.OverviewResponse;
import codesquad.fineants.domain.portfolio.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@RestController
public class DashboardRestController {
	private final DashboardService dashboardService;

	@GetMapping("/overview")
	public ApiResponse<OverviewResponse> readOverview(@AuthPrincipalMember AuthMember authMember) {
		return ApiResponse.success(DashboardSuccessCode.OK_OVERVIEW, dashboardService.getOverview(authMember));
	}

	@GetMapping("/pieChart")
	public ApiResponse<List<DashboardPieChartResponse>> readPieChart(@AuthPrincipalMember AuthMember authMember) {
		return ApiResponse.success(DashboardSuccessCode.OK_PORTFOLIO_PIE_CHART,
			dashboardService.getPieChart(authMember));
	}

	@GetMapping("/lineChart")
	public ApiResponse<List<DashboardLineChartResponse>> readLineChart(@AuthPrincipalMember AuthMember authMember) {
		return ApiResponse.success(DashboardSuccessCode.OK_LINE_CHART, dashboardService.getLineChart(authMember));
	}
}