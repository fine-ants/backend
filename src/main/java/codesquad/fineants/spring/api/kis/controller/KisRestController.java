package codesquad.fineants.spring.api.kis.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	public ApiResponse<Void> refreshAllStockCurrentPrice() {
		service.refreshAllStockCurrentPrice();
		return ApiResponse.success(KisSuccessCode.OK_REFRESH_CURRENT_PRICE_STOCKS);
	}

	@PostMapping("/current-price/refresh")
	public ApiResponse<Void> refreshStockCurrentPrice(
		@RequestBody List<String> tickerSymbols
	) {
		service.refreshStockCurrentPrice(tickerSymbols);
		return ApiResponse.success(KisSuccessCode.OK_REFRESH_CURRENT_PRICE_STOCKS);
	}

	@PostMapping("/closing-price/all/refresh")
	public ApiResponse<Void> refreshAllLastDayClosingPrice() {
		service.refreshAllLastDayClosingPrice();
		return ApiResponse.success(KisSuccessCode.OK_REFRESH_CURRENT_PRICE_STOCKS);
	}

	@PostMapping("/closing-price/refresh")
	public ApiResponse<Void> refreshLastDayClosingPrice(
		@RequestBody List<String> tickerSymbols
	) {
		service.refreshLastDayClosingPrice(tickerSymbols);
		return ApiResponse.success(KisSuccessCode.OK_REFRESH_CURRENT_PRICE_STOCKS);
	}
}
