package co.fineants.price.domain.stockprice.service.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import co.fineants.api.domain.kis.properties.KisProperties;
import co.fineants.api.domain.kis.repository.WebSocketApprovalKeyRedisRepository;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.price.domain.stockprice.client.StockPriceWebSocketClient;
import co.fineants.price.domain.stockprice.domain.StockPrice;
import co.fineants.price.domain.stockprice.domain.event.StockPriceDeleteEvent;
import co.fineants.price.domain.stockprice.domain.event.StockPriceSubscribeEvent;
import co.fineants.price.domain.stockprice.domain.event.WebSocketApprovalKeyReissueEvent;
import co.fineants.price.domain.stockprice.domain.event.WebSocketSessionConnectEvent;
import co.fineants.price.domain.stockprice.repository.StockPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockPriceEventListener {

	private final StockPriceWebSocketClient client;
	private final StockPriceRepository stockPriceRepository;
	private final KisProperties kisProperties;
	private final KisService kisService;
	private final WebSocketApprovalKeyRedisRepository webSocketApprovalKeyRedisRepository;

	@Async
	@EventListener
	public void onWebSocketSessionConnectEvent(WebSocketSessionConnectEvent event) {
		log.info("WebSocketSessionEvent : {}", event);
		client.connect(kisProperties.getWebsocketCurrentPriceUrl());
	}

	@Async
	@EventListener
	public void onStockPriceDeleteEvent(StockPriceDeleteEvent event) {
		log.info("StockPriceDeleteEvent : {}", event);
		stockPriceRepository.remove(StockPrice.newInstance(event.getTicker()));
	}

	@EventListener
	public void onStockPriceSubscribeEvent(StockPriceSubscribeEvent event) {
		log.info("StockPriceSubscribeEvent : {}", event);
		String ticker = event.getTicker();
		boolean result = client.sendSubscribeMessage(ticker);
		log.info("Subscribe result: {}, ticker: {}", result, ticker);
	}

	@EventListener
	public void onWebSocketApprovalKeyReissueEvent(WebSocketApprovalKeyReissueEvent event) {
		log.info("WebSocketApprovalKeyReissueEvent : {}", event);
		kisService.fetchApprovalKey()
			.ifPresent(webSocketApprovalKeyRedisRepository::saveApprovalKey);
		log.info("reissue websocket approval key");
	}
}
