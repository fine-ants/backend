package codesquad.fineants.spring.api.stock;

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
import codesquad.fineants.spring.api.response.ApiResponse;
import codesquad.fineants.spring.api.stock.request.TargetPriceNotificationCreateRequest;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationCreateResponse;
import codesquad.fineants.spring.api.success.code.StockSuccessCode;
import lombok.RequiredArgsConstructor;

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
		return ApiResponse.success(StockSuccessCode.OK_CREATE_TARGET_PRICE_NOTIFICATION, response);
	}
}
