package codesquad.fineants.domain.portfolio.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.portfolio.domain.dto.response.DashboardLineChartResponse;
import codesquad.fineants.domain.portfolio.domain.dto.response.DashboardPieChartResponse;
import codesquad.fineants.domain.portfolio.domain.dto.response.OverviewResponse;
import codesquad.fineants.domain.portfolio.service.DashboardService;
import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.security.auth.dto.MemberAuthentication;
import codesquad.fineants.global.security.auth.resolver.MemberAuthenticationPrincipal;
import codesquad.fineants.global.success.DashboardSuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@RestController
public class DashboardRestController {
	private final DashboardService dashboardService;

	@GetMapping("/overview")
	public ApiResponse<OverviewResponse> readOverview(
		@MemberAuthenticationPrincipal MemberAuthentication authentication) {
		return ApiResponse.success(DashboardSuccessCode.OK_OVERVIEW,
			dashboardService.getOverview(authentication.getId()));
	}

	@GetMapping("/pieChart")
	public ApiResponse<List<DashboardPieChartResponse>> readPieChart(
		@MemberAuthenticationPrincipal MemberAuthentication authentication) {
		return ApiResponse.success(DashboardSuccessCode.OK_PORTFOLIO_PIE_CHART,
			dashboardService.getPieChart(authentication.getId()));
	}

	@GetMapping("/lineChart")
	public ApiResponse<List<DashboardLineChartResponse>> readLineChart(
		@MemberAuthenticationPrincipal MemberAuthentication authentication) {
		return ApiResponse.success(DashboardSuccessCode.OK_LINE_CHART,
			dashboardService.getLineChart(authentication.getId()));
	}
}
