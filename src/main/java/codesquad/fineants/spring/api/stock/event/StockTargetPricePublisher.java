package codesquad.fineants.spring.api.stock.event;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StockTargetPricePublisher {
	private final ApplicationEventPublisher publisher;

	public void publishEvent(List<String> tickerSymbols) {
		StockTargetPriceEventSendableParameter sendableParameter = StockTargetPriceEventSendableParameter.create(
			tickerSymbols);
		publisher.publishEvent(new StockTargetPriceNotificationEvent(sendableParameter));
	}
}
