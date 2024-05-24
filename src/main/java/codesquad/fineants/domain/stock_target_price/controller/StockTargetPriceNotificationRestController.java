package codesquad.fineants.domain.stock_target_price.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.stock_target_price.domain.dto.request.TargetPriceNotificationCreateRequest;
import codesquad.fineants.domain.stock_target_price.domain.dto.request.TargetPriceNotificationDeleteRequest;
import codesquad.fineants.domain.stock_target_price.domain.dto.request.TargetPriceNotificationUpdateRequest;
import codesquad.fineants.domain.stock_target_price.domain.dto.response.TargetPriceNotificationCreateResponse;
import codesquad.fineants.domain.stock_target_price.domain.dto.response.TargetPriceNotificationDeleteResponse;
import codesquad.fineants.domain.stock_target_price.domain.dto.response.TargetPriceNotificationSearchResponse;
import codesquad.fineants.domain.stock_target_price.domain.dto.response.TargetPriceNotificationSpecifiedSearchResponse;
import codesquad.fineants.domain.stock_target_price.domain.dto.response.TargetPriceNotificationUpdateResponse;
import codesquad.fineants.domain.stock_target_price.service.StockTargetPriceNotificationService;
import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.security.oauth.dto.MemberAuthentication;
import codesquad.fineants.global.security.oauth.resolver.MemberAuthenticationPrincipal;
import codesquad.fineants.global.success.StockSuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StockTargetPriceNotificationRestController {

	private final StockTargetPriceNotificationService service;

	// 종목 지정가 알림 데이터 생성
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/api/stocks/target-price/notifications")
	@Secured("ROLE_USER")
	public ApiResponse<TargetPriceNotificationCreateResponse> createStockTargetPriceNotification(
		@Valid @RequestBody TargetPriceNotificationCreateRequest request,
		@MemberAuthenticationPrincipal MemberAuthentication authentication) {
		TargetPriceNotificationCreateResponse response = service.createStockTargetPriceNotification(request,
			authentication.getId());
		log.info("종목 지정가 알림 추가 결과 : {}", response);
		return ApiResponse.success(StockSuccessCode.OK_CREATE_TARGET_PRICE_NOTIFICATION, response);
	}

	// 종목 지정가 알림 조회
	@GetMapping("/api/stocks/target-price/notifications")
	@Secured("ROLE_USER")
	public ApiResponse<TargetPriceNotificationSearchResponse> searchStockTargetPriceNotification(
		@MemberAuthenticationPrincipal MemberAuthentication authentication
	) {
		TargetPriceNotificationSearchResponse response = service.searchStockTargetPriceNotification(
			authentication.getId());
		log.info("종목 지정가 알림 검색 결과 : {}", response);
		return ApiResponse.success(StockSuccessCode.OK_SEARCH_TARGET_PRICE_NOTIFICATIONS, response);
	}

	// 특정 종목 지정가 알림 목록 조회
	@GetMapping("/api/stocks/{tickerSymbol}/target-price/notifications")
	@Secured("ROLE_USER")
	public ApiResponse<TargetPriceNotificationSpecifiedSearchResponse> searchTargetPriceNotifications(
		@PathVariable String tickerSymbol,
		@MemberAuthenticationPrincipal MemberAuthentication authentication
	) {
		TargetPriceNotificationSpecifiedSearchResponse response = service.searchTargetPriceNotifications(tickerSymbol,
			authentication.getId());
		log.info("특정 종목 지정가 알림 리스트 조회 결과 : {}", response);
		return ApiResponse.success(StockSuccessCode.OK_SEARCH_SPECIFIC_TARGET_PRICE_NOTIFICATIONS, response);
	}

	// 종목 지정가 알림 수정
	@PutMapping("/api/stocks/target-price/notifications")
	@Secured("ROLE_USER")
	public ApiResponse<Void> updateStockTargetPriceNotification(
		@Valid @RequestBody TargetPriceNotificationUpdateRequest request,
		@MemberAuthenticationPrincipal MemberAuthentication authentication
	) {
		TargetPriceNotificationUpdateResponse response = service.updateStockTargetPriceNotification(request,
			authentication.getId());
		log.info("종목 지정가 알림 수정 결과 : {}", response);
		StockSuccessCode successCode =
			response.getIsActive() ? StockSuccessCode.OK_UPDATE_TARGET_PRICE_NOTIFICATION_ACTIVE :
				StockSuccessCode.OK_UPDATE_TARGET_PRICE_NOTIFICATION_INACTIVE;
		return ApiResponse.success(successCode);
	}

	// 종목 지정가 알림 전체 삭제
	@DeleteMapping("/api/stocks/target-price/notifications")
	@Secured("ROLE_USER")
	public ApiResponse<Void> deleteAllStockTargetPriceNotification(
		@Valid @RequestBody TargetPriceNotificationDeleteRequest request,
		@MemberAuthenticationPrincipal MemberAuthentication authentication) {
		TargetPriceNotificationDeleteResponse response = service.deleteAllStockTargetPriceNotification(
			request.getTargetPriceNotificationIds(),
			request.getTickerSymbol(),
			authentication.getId());
		log.info("종목 지정가 알림 제거 결과 : {}", response);
		return ApiResponse.success(StockSuccessCode.OK_DELETE_TARGET_PRICE_NOTIFICATIONS);
	}

	// 종목 지정가 알림 특정 삭제
	@DeleteMapping("/api/stocks/target-price/notifications/{targetPriceNotificationId}")
	@Secured("ROLE_USER")
	public ApiResponse<Void> deleteStockTargetPriceNotification(
		@PathVariable Long targetPriceNotificationId,
		@MemberAuthenticationPrincipal MemberAuthentication authentication
	) {
		TargetPriceNotificationDeleteResponse response = service.deleteStockTargetPriceNotification(
			targetPriceNotificationId,
			authentication.getId());
		log.info("종목 지정가 알림 제거 결과 : {}", response);
		return ApiResponse.success(StockSuccessCode.OK_DELETE_TARGET_PRICE_NOTIFICATIONS);
	}
}
