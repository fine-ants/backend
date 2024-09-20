package co.fineants.price.domain.stockprice.client;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketSessionListener {

	private final StockPriceWebSocketClient client;

	@Async
	@EventListener
	public void onMessage(WebSocketSessionEvent event) {
		log.info("WebSocketSessionEvent : {}", event);
		client.connect();
	}
}
