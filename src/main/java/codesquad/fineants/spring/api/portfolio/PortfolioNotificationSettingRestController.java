package codesquad.fineants.spring.api.portfolio;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalMember;
import codesquad.fineants.spring.api.portfolio.response.PortfolioNotificationSettingSearchResponse;
import codesquad.fineants.spring.api.response.ApiResponse;
import codesquad.fineants.spring.api.success.code.PortfolioSuccessCode;
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
