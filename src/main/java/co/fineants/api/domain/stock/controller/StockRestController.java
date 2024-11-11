package co.fineants.api.domain.stock.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import co.fineants.api.domain.stock.domain.dto.request.StockSearchRequest;
import co.fineants.api.domain.stock.domain.dto.response.StockReloadResponse;
import co.fineants.api.domain.stock.domain.dto.response.StockResponse;
import co.fineants.api.domain.stock.domain.dto.response.StockSearchItem;
import co.fineants.api.domain.stock.service.StockService;
import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.success.StockSuccessCode;
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

	@PostMapping(value = "/init", consumes = {MediaType.APPLICATION_JSON_VALUE,
		MediaType.MULTIPART_FORM_DATA_VALUE})
	@Secured(value = {"ROLE_MANAGER", "ROLE_ADMIN"})
	public ApiResponse<Void> initStocks(@RequestPart(value = "file") MultipartFile file) {
		try {
			stockService.initStocks(file.getInputStream());
		} catch (IOException e) {
			throw new IllegalArgumentException("not found file, ", e);
		}
		return ApiResponse.success(StockSuccessCode.OK_INIT_STOCKS);
	}

	@GetMapping("/{tickerSymbol}")
	@PermitAll
	public ApiResponse<StockResponse> getStock(@PathVariable String tickerSymbol) {
		return ApiResponse.success(StockSuccessCode.OK_SEARCH_DETAIL_STOCK,
			stockService.getDetailedStock(tickerSymbol));
	}
}
