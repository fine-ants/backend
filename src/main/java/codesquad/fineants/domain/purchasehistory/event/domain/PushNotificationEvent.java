package codesquad.fineants.domain.purchasehistory.event.domain;

import codesquad.fineants.domain.purchasehistory.event.aop.PurchaseHistoryEventSendableParameter;
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
