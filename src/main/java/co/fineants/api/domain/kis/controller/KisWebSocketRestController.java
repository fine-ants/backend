package co.fineants.api.domain.kis.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.fineants.api.domain.kis.service.KisWebSocketService;
import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.success.KisSuccessCode;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/kis/websocket")
@RequiredArgsConstructor
public class KisWebSocketRestController {
	private final KisWebSocketService service;

	@GetMapping("/current-price")
	public ApiResponse<Void> sendMessage(@RequestParam String ticker) {
		service.fetchCurrentPrice(ticker);
		return ApiResponse.success(KisSuccessCode.OK_SIGNING_PRICE_SEND_MESSAGE);
	}
}
