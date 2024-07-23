package codesquad.fineants.domain.notification.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.notification.domain.dto.response.NotifyMessageResponse;
import codesquad.fineants.domain.notification.domain.dto.response.PortfolioNotifyMessagesResponse;
import codesquad.fineants.domain.notification.service.NotificationService;
import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.success.NotificationSuccessCode;
import codesquad.fineants.global.success.StockSuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@RestController
public class NotificationRestController {

	private final NotificationService service;

	// 한 포트폴리오의 목표 수익률 도달 알림 발송
	@PostMapping("/api/notifications/portfolios/{portfolioId}/notify/target-gain")
	@Secured(value = {"ROLE_MANAGER", "ROLE_ADMIN"})
	public ApiResponse<PortfolioNotifyMessagesResponse> notifyPortfolioTargetGainMessages(
		@PathVariable Long portfolioId
	) {
		PortfolioNotifyMessagesResponse response = (PortfolioNotifyMessagesResponse)service.notifyTargetGain(
			portfolioId);
		log.info("포트폴리오 목표 수익률 알림 결과 : response={}", response);
		return ApiResponse.success(NotificationSuccessCode.OK_NOTIFY_PORTFOLIO_TARGET_GAIN_MESSAGES, response);
	}

	// 한 포트폴리오의 최대 손실율 도달 알림 발송
	@PostMapping("/api/notifications/portfolios/{portfolioId}/notify/max-loss")
	@Secured(value = {"ROLE_MANAGER", "ROLE_ADMIN"})
	public ApiResponse<NotifyMessageResponse> notifyPortfolioMaxLossMessages(
		@PathVariable Long portfolioId
	) {
		NotifyMessageResponse response = service.notifyMaxLoss(portfolioId);
		log.info("포트폴리오 최대 손실율 알림 결과 : response={}", response);
		return ApiResponse.success(NotificationSuccessCode.OK_NOTIFY_PORTFOLIO_MAX_LOSS_MESSAGES, response);
	}

	// 종목 지정가 알림 발송
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/api/stocks/target-price/notifications/send")
	@Secured(value = {"ROLE_MANAGER", "ROLE_ADMIN"})
	public ApiResponse<NotifyMessageResponse> sendStockTargetPriceNotification(
		@RequestParam Long memberId) {
		NotifyMessageResponse response = service.notifyTargetPrice(memberId);
		log.info("종목 지정가 알림 전송 결과 : {}", response);
		return ApiResponse.success(StockSuccessCode.OK_CREATE_TARGET_PRICE_SEND_NOTIFICATION, response);
	}
}

