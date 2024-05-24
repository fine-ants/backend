package codesquad.fineants.domain.portfolio.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.portfolio.domain.dto.response.PortfolioNotificationSettingSearchResponse;
import codesquad.fineants.domain.portfolio.service.PortfolioNotificationSettingService;
import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.security.oauth.dto.MemberAuthentication;
import codesquad.fineants.global.security.oauth.resolver.MemberAuthenticationPrincipal;
import codesquad.fineants.global.success.PortfolioSuccessCode;
import lombok.RequiredArgsConstructor;

@RequestMapping("/api/portfolios/notification/settings")
@RequiredArgsConstructor
@RestController
public class PortfolioNotificationSettingRestController {

	private final PortfolioNotificationSettingService service;

	@GetMapping
	@Secured("ROLE_USER")
	public ApiResponse<PortfolioNotificationSettingSearchResponse> searchPortfolioNotificationSetting(
		@MemberAuthenticationPrincipal MemberAuthentication authentication
	) {
		PortfolioNotificationSettingSearchResponse response = service.searchPortfolioNotificationSetting(
			authentication.getId());
		return ApiResponse.success(PortfolioSuccessCode.OK_SEARCH_PORTFOLIO_NOTIFICATION_SETTINGS, response);
	}
}
