package codesquad.fineants.spring.api.notification.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class PurchaseHistoryEvent extends ApplicationEvent {
	private final Long portfolioId;
	private final Long memberId;

	public PurchaseHistoryEvent(Long portfolioId, Long memberId) {
		super(System.currentTimeMillis());
		this.portfolioId = portfolioId;
		this.memberId = memberId;
	}
}
