package codesquad.fineants.spring.api.stock.event;

import codesquad.fineants.spring.api.purchase_history.event.EventHoldingValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StockTargetPriceNotificationEvent implements EventHoldingValue<StockTargetPriceEventSendableParameter> {
	private StockTargetPriceEventSendableParameter value;

	public StockTargetPriceNotificationEvent(StockTargetPriceEventSendableParameter value) {
		this.value = value;
	}
}
