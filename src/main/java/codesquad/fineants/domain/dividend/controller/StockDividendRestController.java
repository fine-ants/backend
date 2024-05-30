package codesquad.fineants.domain.dividend.controller;

import java.time.LocalDate;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.dividend.service.StockDividendService;
import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.success.StockDividendSuccessCode;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dividends")
@RequiredArgsConstructor
public class StockDividendRestController {

	private final StockDividendService service;

	@PostMapping("/init")
	@Secured("ROLE_ADMIN")
	public ApiResponse<Void> initializeStockDividend() {
		service.initializeStockDividend();
		return ApiResponse.success(StockDividendSuccessCode.OK_INIT_DIVIDENDS);
	}

	@PostMapping("/refresh")
	@Secured("ROLE_ADMIN")
	public ApiResponse<Void> refreshStockDividend() {
		service.refreshStockDividend(LocalDate.now());
		return ApiResponse.success(StockDividendSuccessCode.OK_REFRESH_DIVIDENDS);
	}

	@PostMapping("/write/csv")
	@Secured("ROLE_ADMIN")
	public ApiResponse<Void> writeDividendCsvToS3() {
		service.writeDividendCsvToS3();
		return ApiResponse.success(StockDividendSuccessCode.OK_WRITE_DIVIDENDS_CSV);
	}
}
