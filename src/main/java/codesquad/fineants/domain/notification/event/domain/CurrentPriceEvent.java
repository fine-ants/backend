package codesquad.fineants.domain.notification.event.domain;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CurrentPriceEvent extends ApplicationEvent {

	public CurrentPriceEvent() {
		super(System.currentTimeMillis());
	}
}
