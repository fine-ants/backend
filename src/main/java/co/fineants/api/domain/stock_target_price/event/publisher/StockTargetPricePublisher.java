package co.fineants.api.domain.stock_target_price.event.publisher;

import java.util.Collection;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import co.fineants.api.domain.stock_target_price.event.domain.StockTargetPriceEventSendableParameter;
import co.fineants.api.domain.stock_target_price.event.domain.StockTargetPriceNotificationEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StockTargetPricePublisher {
	private final ApplicationEventPublisher publisher;

	public void publishEvent(Collection<String> tickerSymbols) {
		List<String> tickerList = tickerSymbols.stream().toList();
		StockTargetPriceEventSendableParameter sendableParameter =
			StockTargetPriceEventSendableParameter.create(tickerList);
		publisher.publishEvent(new StockTargetPriceNotificationEvent(sendableParameter));
	}
}
