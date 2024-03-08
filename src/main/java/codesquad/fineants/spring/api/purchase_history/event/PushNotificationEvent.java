package codesquad.fineants.spring.api.purchase_history.event;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PushNotificationEvent implements EventHoldingValue<PurchaseHistoryEventSendableParameter> {

	private PurchaseHistoryEventSendableParameter value;

	public PushNotificationEvent(PurchaseHistoryEventSendableParameter value) {
		this.value = value;
	}
}
