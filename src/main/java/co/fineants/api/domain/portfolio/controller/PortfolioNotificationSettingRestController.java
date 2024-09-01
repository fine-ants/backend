package co.fineants.api.domain.portfolio.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.fineants.api.domain.portfolio.domain.dto.response.PortfolioNotificationSettingSearchResponse;
import co.fineants.api.domain.portfolio.service.PortfolioNotificationSettingService;
import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import co.fineants.api.global.security.oauth.resolver.MemberAuthenticationPrincipal;
import co.fineants.api.global.success.PortfolioSuccessCode;
import lombok.RequiredArgsConstructor;

@RequestMapping("/api/portfolios/notification/settings")
@RequiredArgsConstructor
@RestController
public class PortfolioNotificationSettingRestController {

	private final PortfolioNotificationSettingService service;

	@GetMapping
	public ApiResponse<PortfolioNotificationSettingSearchResponse> searchPortfolioNotificationSetting(
		@MemberAuthenticationPrincipal MemberAuthentication authentication
	) {
		PortfolioNotificationSettingSearchResponse response = service.searchPortfolioNotificationSetting(
			authentication.getId());
		return ApiResponse.success(PortfolioSuccessCode.OK_SEARCH_PORTFOLIO_NOTIFICATION_SETTINGS, response);
	}
}
