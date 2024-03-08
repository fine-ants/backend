package codesquad.fineants.spring.api.stock.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import codesquad.fineants.spring.api.stock.StockTargetPriceNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockTargetPriceEventListener {

	private StockTargetPriceNotificationService service;

	@Async
	@EventListener
	public void notifyStockTargetPriceMessages(StockTargetPriceNotificationEvent event) {
		StockTargetPriceEventSendableParameter value = event.getValue();
	}
}
