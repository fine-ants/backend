package codesquad.fineants.spring.api.stock_target_price.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import codesquad.fineants.spring.api.notification.service.NotificationService;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotifyMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockTargetPriceEventListener {

	private final NotificationService service;

	@Async
	@EventListener
	public void notifyStockTargetPriceMessages(StockTargetPriceNotificationEvent event) {
		StockTargetPriceEventSendableParameter value = event.getValue();
		TargetPriceNotifyMessageResponse response = service.notifyTargetPriceBy(
			value.getTickerSymbols());
		if (!response.getNotifications().isEmpty()) {
			log.info("종목 지정가 메시지 전송 결과 : response={}", response);
		}
	}
}
