package co.fineants.price.domain.stockprice.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.success.KisSuccessCode;
import co.fineants.price.domain.stockprice.client.StockPriceWebSocketClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/stock-prices/websocket")
@RequiredArgsConstructor
@Slf4j
public class StockPriceRestController {

	private final StockPriceWebSocketClient client;

	@PostMapping("/current-price")
	@Secured(value = {"ROLE_MANAGER", "ROLE_ADMIN"})
	public ApiResponse<Void> sendMessage(@RequestParam String ticker) {
		boolean result = client.sendMessage(ticker);
		log.info("sendMessage result={}", result);
		return ApiResponse.success(KisSuccessCode.OK_SIGNING_PRICE_SEND_MESSAGE);
	}
}
