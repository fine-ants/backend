package co.fineants.api.domain.kis.controller;

import java.net.URISyntaxException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.fineants.api.domain.kis.service.KisWebSocketService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/kis/websocket")
@RequiredArgsConstructor
public class KisWebSocketRestController {
	private final KisWebSocketService service;

	@GetMapping("/current-price")
	public String sendMessage(@RequestParam String ticker) throws URISyntaxException {
		service.fetchCurrentPrice(ticker);
		return "Message sent to WebSocket server: " + ticker;
	}
}
