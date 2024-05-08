package codesquad.fineants.domain.notification.event.domain;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class CurrentPriceEvent extends ApplicationEvent {

	public CurrentPriceEvent() {
		super(System.currentTimeMillis());
	}
}
