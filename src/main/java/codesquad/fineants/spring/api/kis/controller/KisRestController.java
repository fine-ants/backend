package codesquad.fineants.spring.api.kis.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.spring.api.kis.request.StockPriceRefreshRequest;
import codesquad.fineants.spring.api.kis.response.CurrentPriceResponse;
import codesquad.fineants.spring.api.kis.response.LastDayClosingPriceResponse;
import codesquad.fineants.spring.api.kis.service.KisService;
import codesquad.fineants.spring.api.response.ApiResponse;
import codesquad.fineants.spring.api.success.code.KisSuccessCode;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/kis")
@RequiredArgsConstructor
public class KisRestController {

	private final KisService service;

	// 종목 현재가 갱신
	@PostMapping("/current-price/all/refresh")
	public ApiResponse<List<CurrentPriceResponse>> refreshAllStockCurrentPrice() {
		List<CurrentPriceResponse> responses = service.refreshAllStockCurrentPrice();
		return ApiResponse.success(KisSuccessCode.OK_REFRESH_CURRENT_PRICE_STOCKS, responses);
	}

	// 특정 종목 현재가 갱신
	@PostMapping("/current-price/refresh")
	public ApiResponse<List<CurrentPriceResponse>> refreshStockCurrentPrice(
		@RequestBody StockPriceRefreshRequest request
	) {
		List<CurrentPriceResponse> response = service.refreshStockCurrentPrice(request.getTickerSymbols());
		return ApiResponse.success(KisSuccessCode.OK_REFRESH_CURRENT_PRICE_STOCKS, response);
	}

	// 모든 종목 종가 갱신
	@PostMapping("/closing-price/all/refresh")
	public ApiResponse<List<LastDayClosingPriceResponse>> refreshAllLastDayClosingPrice() {
		List<LastDayClosingPriceResponse> responses = service.refreshAllLastDayClosingPrice();
		return ApiResponse.success(KisSuccessCode.OK_REFRESH_LAST_DAY_CLOSING_PRICE, responses);
	}

	// 특정 종목 종가 갱신
	@PostMapping("/closing-price/refresh")
	public ApiResponse<List<LastDayClosingPriceResponse>> refreshLastDayClosingPrice(
		@RequestBody StockPriceRefreshRequest request
	) {
		List<LastDayClosingPriceResponse> responses = service.refreshLastDayClosingPrice(request.getTickerSymbols());
		return ApiResponse.success(KisSuccessCode.OK_REFRESH_LAST_DAY_CLOSING_PRICE, responses);
	}
}
