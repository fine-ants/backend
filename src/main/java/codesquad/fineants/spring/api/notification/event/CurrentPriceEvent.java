package codesquad.fineants.spring.api.notification.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class CurrentPriceEvent extends ApplicationEvent {

	public CurrentPriceEvent() {
		super(System.currentTimeMillis());
	}
}
