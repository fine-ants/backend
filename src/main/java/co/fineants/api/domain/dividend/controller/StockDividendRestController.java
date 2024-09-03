package co.fineants.api.domain.dividend.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.fineants.api.domain.dividend.service.StockDividendService;
import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.success.StockDividendSuccessCode;
import co.fineants.api.infra.s3.service.AmazonS3DividendService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dividends")
@RequiredArgsConstructor
public class StockDividendRestController {

	private final StockDividendService service;
	private final AmazonS3DividendService s3DividendService;

	@PostMapping("/init")
	@Secured("ROLE_ADMIN")
	public ApiResponse<Void> initializeStockDividend() {
		service.initializeStockDividend();
		return ApiResponse.success(StockDividendSuccessCode.OK_INIT_DIVIDENDS);
	}

	@PostMapping("/refresh")
	@Secured("ROLE_ADMIN")
	public ApiResponse<Void> refreshStockDividend() {
		service.reloadStockDividend();
		return ApiResponse.success(StockDividendSuccessCode.OK_REFRESH_DIVIDENDS);
	}

	@PostMapping("/write/csv")
	@Secured("ROLE_ADMIN")
	public ApiResponse<Void> writeDividendCsvToS3() {
		s3DividendService.writeDividends(service.findAllStockDividends());
		return ApiResponse.success(StockDividendSuccessCode.OK_WRITE_DIVIDENDS_CSV);
	}
}
