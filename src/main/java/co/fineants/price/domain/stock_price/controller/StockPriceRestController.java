package co.fineants.price.domain.stock_price.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import co.fineants.api.global.api.ApiResponse;
import co.fineants.price.domain.stock_price.dto.request.StockPricePushRequest;
import co.fineants.price.domain.stock_price.service.StockPriceService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class StockPriceRestController {

	private final StockPriceService stockPriceService;

	@PostMapping("/api/stocks")
	public ApiResponse<Void> pushStocks(@RequestBody StockPricePushRequest request) {
		stockPriceService.pushStocks(request.getTickerSymbols());
		return ApiResponse.ok("종목들을 큐에 성공적으로 넣었습니다.", null);
	}
}
