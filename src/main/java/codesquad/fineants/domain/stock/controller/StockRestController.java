package codesquad.fineants.domain.stock.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.stock.domain.dto.request.StockSearchRequest;
import codesquad.fineants.domain.stock.domain.dto.response.StockResponse;
import codesquad.fineants.domain.stock.domain.dto.response.StockSearchItem;
import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.success.StockSuccessCode;
import codesquad.fineants.domain.stock.domain.dto.response.StockRefreshResponse;
import codesquad.fineants.domain.stock.service.StockService;
import lombok.RequiredArgsConstructor;

@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@RestController
public class StockRestController {

	private final StockService stockService;

	@PostMapping("/search")
	public ApiResponse<List<StockSearchItem>> search(@RequestBody final StockSearchRequest request) {
		return ApiResponse.success(StockSuccessCode.OK_SEARCH_STOCKS, stockService.search(request));
	}

	@PostMapping("/refresh")
	public ApiResponse<StockRefreshResponse> refreshStocks() {
		return ApiResponse.success(StockSuccessCode.OK_REFRESH_STOCKS, stockService.refreshStocks());
	}

	@GetMapping("/{tickerSymbol}")
	public ApiResponse<StockResponse> getStock(@PathVariable String tickerSymbol) {
		return ApiResponse.success(StockSuccessCode.OK_SEARCH_DETAIL_STOCK, stockService.getStock(tickerSymbol));
	}
}