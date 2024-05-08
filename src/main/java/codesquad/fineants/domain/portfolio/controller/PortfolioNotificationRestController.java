package codesquad.fineants.domain.portfolio.controller;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalMember;
import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.success.PortfolioSuccessCode;
import codesquad.fineants.domain.portfolio.aop.HasPortfolioAuthorization;
import codesquad.fineants.domain.portfolio.domain.dto.request.PortfolioNotificationUpdateRequest;
import codesquad.fineants.domain.portfolio.domain.dto.response.PortfolioNotificationUpdateResponse;
import codesquad.fineants.domain.portfolio.service.PortfolioNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/portfolio/{portfolioId}/notification")
@RestController
public class PortfolioNotificationRestController {

	private final PortfolioNotificationService service;

	@HasPortfolioAuthorization
	@PutMapping("/targetGain")
	public ApiResponse<Void> updateNotificationTargetGain(
		@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember,
		@Valid @RequestBody PortfolioNotificationUpdateRequest request) {
		log.info("포트폴리오 알림 설정 : request={}, portfolioId={}", request, portfolioId);
		PortfolioNotificationUpdateResponse response = service.updateNotificationTargetGain(request,
			portfolioId);
		if (response.getIsActive()) {
			return ApiResponse.success(PortfolioSuccessCode.OK_MODIFY_PORTFOLIO_TARGET_GAIN_ACTIVE_NOTIFICATION);
		}
		return ApiResponse.success(PortfolioSuccessCode.OK_MODIFY_PORTFOLIO_TARGET_GAIN_INACTIVE_NOTIFICATION);
	}

	@HasPortfolioAuthorization
	@PutMapping("/maxLoss")
	public ApiResponse<Void> updateNotificationMaximumLoss(
		@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember,
		@Valid @RequestBody PortfolioNotificationUpdateRequest request) {
		log.info("포트폴리오 알림 설정 : request={}, portfolioId={}", request, portfolioId);
		PortfolioNotificationUpdateResponse response = service.updateNotificationMaximumLoss(request,
			portfolioId);
		if (response.getIsActive()) {
			return ApiResponse.success(PortfolioSuccessCode.OK_MODIFY_PORTFOLIO_MAXIMUM_LOSS_ACTIVE_NOTIFICATION);
		}
		return ApiResponse.success(PortfolioSuccessCode.OK_MODIFY_PORTFOLIO_MAXIMUM_LOSS_INACTIVE_NOTIFICATION);
	}
}
