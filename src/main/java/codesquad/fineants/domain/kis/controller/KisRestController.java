package codesquad.fineants.domain.kis.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.kis.client.KisAccessToken;
import codesquad.fineants.domain.kis.client.KisCurrentPrice;
import codesquad.fineants.domain.kis.domain.dto.request.StockPriceRefreshRequest;
import codesquad.fineants.domain.kis.domain.dto.response.KisClosingPrice;
import codesquad.fineants.domain.kis.domain.dto.response.KisDividend;
import codesquad.fineants.domain.kis.service.KisService;
import codesquad.fineants.domain.stock.domain.dto.response.StockDataResponse;
import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.success.KisSuccessCode;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/kis")
@RequiredArgsConstructor
public class KisRestController {

	private final KisService service;

	// 종목 현재가 갱신
	@PostMapping("/current-price/all/refresh")
	@Secured(value = {"ROLE_MANAGER", "ROLE_ADMIN"})
	public ApiResponse<List<KisCurrentPrice>> refreshAllStockCurrentPrice() {
		List<KisCurrentPrice> responses = service.refreshAllStockCurrentPrice();
		return ApiResponse.success(KisSuccessCode.OK_REFRESH_CURRENT_PRICE_STOCKS, responses);
	}

	// 특정 종목 현재가 갱신
	@PostMapping("/current-price/refresh")
	@Secured(value = {"ROLE_MANAGER", "ROLE_ADMIN"})
	public ApiResponse<List<KisCurrentPrice>> refreshStockCurrentPrice(
		@RequestBody StockPriceRefreshRequest request
	) {
		List<KisCurrentPrice> response = service.refreshStockCurrentPrice(request.getTickerSymbols());
		return ApiResponse.success(KisSuccessCode.OK_REFRESH_CURRENT_PRICE_STOCKS, response);
	}

	// 한 종목 현재가 조회
	@GetMapping("/current-price/{tickerSymbol}")
	@Secured(value = {"ROLE_MANAGER", "ROLE_ADMIN"})
	public Mono<ApiResponse<KisCurrentPrice>> fetchCurrentPrice(
		@PathVariable String tickerSymbol
	) {
		return service.fetchCurrentPrice(tickerSymbol)
			.map(currentPrice -> ApiResponse.success(KisSuccessCode.OK_FETCH_CURRENT_PRICE, currentPrice));
	}

	// 모든 종목 종가 갱신
	@PostMapping("/closing-price/all/refresh")
	@Secured(value = {"ROLE_MANAGER", "ROLE_ADMIN"})
	public ApiResponse<List<KisClosingPrice>> refreshAllLastDayClosingPrice() {
		List<KisClosingPrice> responses = service.refreshAllClosingPrice();
		return ApiResponse.success(KisSuccessCode.OK_REFRESH_LAST_DAY_CLOSING_PRICE, responses);
	}

	// 특정 종목 종가 갱신
	@PostMapping("/closing-price/refresh")
	@Secured(value = {"ROLE_MANAGER", "ROLE_ADMIN"})
	public ApiResponse<List<KisClosingPrice>> refreshLastDayClosingPrice(
		@RequestBody StockPriceRefreshRequest request
	) {
		List<KisClosingPrice> responses = service.refreshClosingPrice(request.getTickerSymbols());
		return ApiResponse.success(KisSuccessCode.OK_REFRESH_LAST_DAY_CLOSING_PRICE, responses);
	}

	// 상장된 종목 정보 조회
	@GetMapping("/ipo/search-stock-info")
	@Secured(value = {"ROLE_MANAGER", "ROLE_ADMIN"})
	public Mono<ApiResponse<Set<StockDataResponse.StockIntegrationInfo>>> fetchStockInfoInRangedIpo() {
		Mono<Set<StockDataResponse.StockIntegrationInfo>> result = service.fetchStockInfoInRangedIpo()
			.collect(Collectors.toUnmodifiableSet());
		return result.map(data -> ApiResponse.success(KisSuccessCode.OK_FETCH_IPO_SEARCh_STOCK_INFO, data));
	}

	// 배당 일정 조회
	@GetMapping("/dividend/{tickerSymbol}")
	@Secured(value = {"ROLE_MANAGER", "ROLE_ADMIN"})
	public Mono<ApiResponse<List<KisDividend>>> fetchDividend(@PathVariable String tickerSymbol) {
		Mono<List<KisDividend>> result = service.fetchDividend(tickerSymbol)
			.collect(Collectors.toList());
		return result.map(data -> ApiResponse.success(KisSuccessCode.OK_FETCH_DIVIDEND, data));
	}

	@DeleteMapping("/access-token")
	public ApiResponse<KisAccessToken> deleteAccessToken() {
		return ApiResponse.ok("액세스 토큰을 삭제하였습니다.", service.deleteAccessToken());
	}
}
