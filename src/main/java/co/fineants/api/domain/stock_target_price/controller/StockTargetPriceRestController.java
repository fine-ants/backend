package co.fineants.api.domain.stock_target_price.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.fineants.api.domain.stock_target_price.domain.dto.request.TargetPriceNotificationCreateRequest;
import co.fineants.api.domain.stock_target_price.domain.dto.request.TargetPriceNotificationUpdateRequest;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceNotificationCreateResponse;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceNotificationSearchResponse;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceNotificationSpecifiedSearchResponse;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceNotificationUpdateResponse;
import co.fineants.api.domain.stock_target_price.service.StockTargetPriceService;
import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import co.fineants.api.global.security.oauth.resolver.MemberAuthenticationPrincipal;
import co.fineants.api.global.success.StockSuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StockTargetPriceRestController {

	private final StockTargetPriceService service;

	// 종목 지정가 알림 데이터 생성
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/api/stocks/target-price/notifications")
	public ApiResponse<TargetPriceNotificationCreateResponse> createStockTargetPriceNotification(
		@Valid @RequestBody TargetPriceNotificationCreateRequest request,
		@MemberAuthenticationPrincipal MemberAuthentication authentication) {
		TargetPriceNotificationCreateResponse response = service.createStockTargetPrice(request,
			authentication.getId());
		log.info("종목 지정가 알림 추가 결과 : {}", response);
		return ApiResponse.success(StockSuccessCode.OK_CREATE_TARGET_PRICE_NOTIFICATION, response);
	}

	// 종목 지정가 알림 조회
	@GetMapping("/api/stocks/target-price/notifications")
	public ApiResponse<TargetPriceNotificationSearchResponse> searchStockTargetPriceNotification(
		@MemberAuthenticationPrincipal MemberAuthentication authentication
	) {
		TargetPriceNotificationSearchResponse response = service.searchStockTargetPrices(
			authentication.getId());
		log.info("종목 지정가 알림 검색 결과 : {}", response);
		return ApiResponse.success(StockSuccessCode.OK_SEARCH_TARGET_PRICE_NOTIFICATIONS, response);
	}

	// 특정 종목 지정가 알림 목록 조회
	@GetMapping("/api/stocks/{tickerSymbol}/target-price/notifications")
	public ApiResponse<TargetPriceNotificationSpecifiedSearchResponse> searchTargetPriceNotifications(
		@PathVariable String tickerSymbol,
		@MemberAuthenticationPrincipal MemberAuthentication authentication
	) {
		TargetPriceNotificationSpecifiedSearchResponse response = service.searchStockTargetPrice(tickerSymbol,
			authentication.getId());
		log.info("특정 종목 지정가 알림 리스트 조회 결과 : {}", response);
		return ApiResponse.success(StockSuccessCode.OK_SEARCH_SPECIFIC_TARGET_PRICE_NOTIFICATIONS, response);
	}

	// 종목 지정가 알림 수정
	@PutMapping("/api/stocks/target-price/notifications")
	public ApiResponse<Void> updateStockTargetPriceNotification(
		@Valid @RequestBody TargetPriceNotificationUpdateRequest request,
		@MemberAuthenticationPrincipal MemberAuthentication authentication
	) {
		TargetPriceNotificationUpdateResponse response = service.updateStockTargetPrice(request,
			authentication.getId());
		log.info("종목 지정가 알림 수정 결과 : {}", response);
		StockSuccessCode successCode =
			Boolean.TRUE.equals(response.getIsActive()) ? StockSuccessCode.OK_UPDATE_TARGET_PRICE_NOTIFICATION_ACTIVE :
				StockSuccessCode.OK_UPDATE_TARGET_PRICE_NOTIFICATION_INACTIVE;
		return ApiResponse.success(successCode);
	}

	// 종목 지정가 단일 제거
	@DeleteMapping("/api/stocks/target-price/{stockTargetPriceId}")
	public ApiResponse<Void> deleteStockTargetPrice(
		@PathVariable Long stockTargetPriceId
	) {
		service.deleteStockTargetPrice(stockTargetPriceId);
		return ApiResponse.success(StockSuccessCode.OK_DELETE_STOCK_TARGET_PRICE);
	}
}
