package codesquad.fineants.domain.stock.controller;

import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.stock.domain.dto.request.StockSearchRequest;
import codesquad.fineants.domain.stock.domain.dto.response.StockReloadResponse;
import codesquad.fineants.domain.stock.domain.dto.response.StockResponse;
import codesquad.fineants.domain.stock.domain.dto.response.StockSearchItem;
import codesquad.fineants.domain.stock.service.StockService;
import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.success.StockSuccessCode;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;

@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@RestController
public class StockRestController {

	private final StockService stockService;

	@PostMapping("/search")
	@PermitAll
	public ApiResponse<List<StockSearchItem>> search(@RequestBody final StockSearchRequest request) {
		return ApiResponse.success(StockSuccessCode.OK_SEARCH_STOCKS, stockService.search(request));
	}

	@GetMapping("/search")
	@PermitAll
	public ApiResponse<List<StockSearchItem>> search(
		@RequestParam(name = "tickerSymbol", required = false) String tickerSymbol,
		@RequestParam(name = "size", required = false, defaultValue = "10") int size,
		@RequestParam(name = "keyword", required = false) String keyword) {
		return ApiResponse.success(StockSuccessCode.OK_SEARCH_STOCKS, stockService.search(tickerSymbol, size, keyword));
	}

	@PostMapping("/refresh")
	@Secured(value = {"ROLE_MANAGER", "ROLE_ADMIN"})
	public ApiResponse<StockReloadResponse> refreshStocks() {
		return ApiResponse.success(StockSuccessCode.OK_REFRESH_STOCKS, stockService.reloadStocks());
	}

	@GetMapping("/{tickerSymbol}")
	@PermitAll
	public ApiResponse<StockResponse> getStock(@PathVariable String tickerSymbol) {
		return ApiResponse.success(StockSuccessCode.OK_SEARCH_DETAIL_STOCK,
			stockService.getDetailedStock(tickerSymbol));
	}
}
