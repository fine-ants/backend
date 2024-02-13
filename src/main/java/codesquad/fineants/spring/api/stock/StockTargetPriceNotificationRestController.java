package codesquad.fineants.spring.api.stock;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalMember;
import codesquad.fineants.spring.api.response.ApiResponse;
import codesquad.fineants.spring.api.stock.request.TargetPriceNotificationCreateRequest;
import codesquad.fineants.spring.api.stock.request.TargetPriceNotificationDeleteRequest;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationCreateResponse;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationDeleteResponse;
import codesquad.fineants.spring.api.success.code.StockSuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/stocks/{tickerSymbol}/target-price/notifications")
@RequiredArgsConstructor
public class StockTargetPriceNotificationRestController {

	private final StockTargetPriceNotificationService service;

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	public ApiResponse<TargetPriceNotificationCreateResponse> createStockTargetPriceNotification(
		@PathVariable String tickerSymbol,
		@Valid @RequestBody TargetPriceNotificationCreateRequest request,
		@AuthPrincipalMember AuthMember authMember) {
		TargetPriceNotificationCreateResponse response = service.createStockTargetPriceNotification(
			tickerSymbol,
			request,
			authMember.getMemberId()
		);
		log.info("종목 지정가 알림 추가 결과 : {}", response);
		return ApiResponse.success(StockSuccessCode.OK_CREATE_TARGET_PRICE_NOTIFICATION, response);
	}

	@DeleteMapping("/{targetPriceNotificationId}")
	public ApiResponse<Void> deleteStockTargetPriceNotification(
		@PathVariable String tickerSymbol,
		@PathVariable Long targetPriceNotificationId,
		@AuthPrincipalMember AuthMember authMember
	) {
		TargetPriceNotificationDeleteResponse response = service.deleteStockTargetPriceNotification(
			tickerSymbol,
			List.of(targetPriceNotificationId),
			authMember.getMemberId()
		);
		log.info("종목 지정가 알림 제거 결과 : {}", response);
		return ApiResponse.success(StockSuccessCode.OK_DELETE_TARGET_PRICE_NOTIFICATIONS);
	}

	@DeleteMapping
	public ApiResponse<Void> deleteAllStockTargetPriceNotification(
		@PathVariable String tickerSymbol,
		@Valid @RequestBody TargetPriceNotificationDeleteRequest request,
		@AuthPrincipalMember AuthMember authMember) {
		TargetPriceNotificationDeleteResponse response = service.deleteStockTargetPriceNotification(
			tickerSymbol,
			request.getTargetPriceNotificationIds(),
			authMember.getMemberId());
		log.info("종목 지정가 알림 제거 결과 : {}", response);
		return ApiResponse.success(StockSuccessCode.OK_DELETE_TARGET_PRICE_NOTIFICATIONS);
	}
}
