package codesquad.fineants.spring.api.dashboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalMember;
import codesquad.fineants.spring.api.dashboard.response.OverviewResponse;
import codesquad.fineants.spring.api.dashboard.service.DashboardService;
import codesquad.fineants.spring.api.response.ApiResponse;
import codesquad.fineants.spring.api.success.code.DashboardSuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@RestController
public class DashboardRestController {
	private final DashboardService dashboardService;

	@GetMapping("/overview")
	public ApiResponse<OverviewResponse> readDashboard(@AuthPrincipalMember AuthMember authMember) {
		return ApiResponse.success(DashboardSuccessCode.OK_OVERVIEW, dashboardService.getOverview(authMember));
	}
}
