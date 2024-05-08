package codesquad.fineants.domain.portfolio.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalMember;
import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.success.PortfolioSuccessCode;
import codesquad.fineants.domain.portfolio.domain.dto.response.PortfolioNotificationSettingSearchResponse;
import codesquad.fineants.domain.portfolio.service.PortfolioNotificationSettingService;
import lombok.RequiredArgsConstructor;

@RequestMapping("/api/portfolios/notification/settings")
@RequiredArgsConstructor
@RestController
public class PortfolioNotificationSettingRestController {

	private final PortfolioNotificationSettingService service;

	@GetMapping
	public ApiResponse<PortfolioNotificationSettingSearchResponse> searchPortfolioNotificationSetting(
		@AuthPrincipalMember AuthMember authMember
	) {
		PortfolioNotificationSettingSearchResponse response = service.searchPortfolioNotificationSetting(
			authMember.getMemberId());
		return ApiResponse.success(PortfolioSuccessCode.OK_SEARCH_PORTFOLIO_NOTIFICATION_SETTINGS, response);
	}
}
