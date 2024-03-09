package codesquad.fineants.spring.api.stock.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.spring.api.common.response.ApiResponse;
import codesquad.fineants.spring.api.common.success.StockSuccessCode;
import codesquad.fineants.spring.api.stock.request.StockSearchRequest;
import codesquad.fineants.spring.api.stock.response.StockResponse;
import codesquad.fineants.spring.api.stock.response.StockSearchItem;
import codesquad.fineants.spring.api.stock.service.StockService;
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

	@GetMapping("/{tickerSymbol}")
	public ApiResponse<StockResponse> getStock(@PathVariable String tickerSymbol) {
		return ApiResponse.success(StockSuccessCode.OK_SEARCH_DETAIL_STOCK, stockService.getStock(tickerSymbol));
	}
}
