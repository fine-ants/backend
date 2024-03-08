package codesquad.fineants.spring.api.stock.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StockTargetPricePublisher {
	private final ApplicationEventPublisher publisher;

	public void publishEvent(String tickerSymbol, Long currentPrice) {
		StockTargetPriceEventSendableParameter sendableParameter = StockTargetPriceEventSendableParameter.create(
			tickerSymbol, currentPrice);
		publisher.publishEvent(new StockTargetPriceNotificationEvent(sendableParameter));
	}
}
