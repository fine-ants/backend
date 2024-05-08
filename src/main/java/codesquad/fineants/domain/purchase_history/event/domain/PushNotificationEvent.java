package codesquad.fineants.domain.purchase_history.event.domain;

import codesquad.fineants.domain.purchase_history.event.aop.PurchaseHistoryEventSendableParameter;
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
