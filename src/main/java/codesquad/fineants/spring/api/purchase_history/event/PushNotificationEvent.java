package codesquad.fineants.spring.api.purchase_history.event;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PushNotificationEvent implements EventHoldingValue<SendableParameter> {

	private SendableParameter value;

	public PushNotificationEvent(SendableParameter value) {
		this.value = value;
	}
}
