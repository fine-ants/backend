package codesquad.fineants.spring.api.notification.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalMember;
import codesquad.fineants.spring.api.common.response.ApiResponse;
import codesquad.fineants.spring.api.common.success.NotificationSuccessCode;
import codesquad.fineants.spring.api.common.success.StockSuccessCode;
import codesquad.fineants.spring.api.notification.response.PortfolioNotifyMessagesResponse;
import codesquad.fineants.spring.api.notification.service.NotificationService;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotifyMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@RestController
public class NotificationRestController {

	private final NotificationService service;

	// 한 포트폴리오의 목표 수익률 도달 알림 발송
	@PostMapping("/api/notifications/portfolios/{portfolioId}/notify/target-gain")
	public ApiResponse<PortfolioNotifyMessagesResponse> notifyPortfolioTargetGainMessages(
		@PathVariable Long portfolioId
	) {
		PortfolioNotifyMessagesResponse response = service.notifyTargetGainBy(portfolioId);
		log.info("포트폴리오 목표 수익률 알림 결과 : response={}", response);
		return ApiResponse.success(NotificationSuccessCode.OK_NOTIFY_PORTFOLIO_TARGET_GAIN_MESSAGES, response);
	}

	// 한 포트폴리오의 최대 손실율 도달 알림 발송
	@PostMapping("/api/notifications/portfolios/{portfolioId}/notify/max-loss")
	public ApiResponse<PortfolioNotifyMessagesResponse> notifyPortfolioMaxLossMessages(
		@PathVariable Long portfolioId
	) {
		PortfolioNotifyMessagesResponse response = service.notifyMaxLoss(portfolioId);
		log.info("포트폴리오 최대 손실율 알림 결과 : response={}", response);
		return ApiResponse.success(NotificationSuccessCode.OK_NOTIFY_PORTFOLIO_MAX_LOSS_MESSAGES, response);
	}

	// 종목 지정가 알림 발송
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/api/stocks/target-price/notifications/send")
	public ApiResponse<TargetPriceNotifyMessageResponse> sendStockTargetPriceNotification(
		@AuthPrincipalMember AuthMember authMember) {
		TargetPriceNotifyMessageResponse response = service.notifyTargetPriceBy(
			authMember.getMemberId());
		log.info("종목 지정가 알림 전송 결과 : {}", response);
		return ApiResponse.success(StockSuccessCode.OK_CREATE_TARGET_PRICE_SEND_NOTIFICATION, response);
	}
}

