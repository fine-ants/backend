package codesquad.fineants.spring.api.dashboard.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalMember;
import codesquad.fineants.spring.api.common.response.ApiResponse;
import codesquad.fineants.spring.api.common.success.DashboardSuccessCode;
import codesquad.fineants.spring.api.dashboard.response.DashboardLineChartResponse;
import codesquad.fineants.spring.api.dashboard.response.DashboardPieChartResponse;
import codesquad.fineants.spring.api.dashboard.response.OverviewResponse;
import codesquad.fineants.spring.api.dashboard.service.DashboardService;
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
