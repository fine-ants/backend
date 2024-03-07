package codesquad.fineants.spring.api.notification.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalMember;
import codesquad.fineants.spring.api.notification.request.PortfolioNotificationCreateRequest;
import codesquad.fineants.spring.api.notification.response.NotificationCreateResponse;
import codesquad.fineants.spring.api.notification.response.NotifyPortfolioMessagesResponse;
import codesquad.fineants.spring.api.notification.service.NotificationService;
import codesquad.fineants.spring.api.response.ApiResponse;
import codesquad.fineants.spring.api.success.code.NotificationSuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/notifications")
@RestController
public class NotificationRestController {

	private final NotificationService service;

	// 알림 저장
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	public ApiResponse<NotificationCreateResponse> createNotification(
		@Valid @RequestBody PortfolioNotificationCreateRequest request,
		@AuthPrincipalMember AuthMember authMember
	) {
		NotificationCreateResponse response = service.createPortfolioNotification(request, authMember.getMemberId());
		log.info("알림 저장 결과 : response={}", response);
		return ApiResponse.success(NotificationSuccessCode.CREATED_NOTIFICATION, response);
	}

	// 한 포트폴리오의 목표 수익률 도달 알림 발송
	@PostMapping("/portfolios/{portfolioId}/notify/target-gain")
	public ApiResponse<NotifyPortfolioMessagesResponse> notifyPortfolioTargetGainMessages(
		@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember
	) {
		NotifyPortfolioMessagesResponse response = service.notifyPortfolioTargetGainMessages(portfolioId,
			authMember.getMemberId());
		log.info("포트폴리오 목표 수익률 알림 결과 : response={}", response);
		return ApiResponse.success(NotificationSuccessCode.OK_NOTIFY_PORTFOLIO_TARGET_GAIN_MESSAGES, response);
	}

	// 한 포트폴리오의 최대 손실율 도달 알림 발송
	@PostMapping("/portfolios/{portfolioId}/notify/max-loss")
	public ApiResponse<NotifyPortfolioMessagesResponse> notifyPortfolioMaxLossMessages(
		@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember
	) {
		NotifyPortfolioMessagesResponse response = service.notifyPortfolioMaxLossMessages(portfolioId,
			authMember.getMemberId());
		log.info("포트폴리오 최대 손실율 알림 결과 : response={}", response);
		return ApiResponse.success(NotificationSuccessCode.OK_NOTIFY_PORTFOLIO_MAX_LOSS_MESSAGES, response);
	}
}

