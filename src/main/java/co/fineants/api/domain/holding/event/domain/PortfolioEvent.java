package co.fineants.api.domain.holding.event.domain;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEvent;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;
import lombok.Getter;

@Getter
public class PortfolioEvent extends ApplicationEvent {

	private final SseEmitterKey key;
	private final PortfolioHoldingsRealTimeResponse response;
	private final LocalDateTime eventDateTime;

	public PortfolioEvent(SseEmitterKey key, PortfolioHoldingsRealTimeResponse response, LocalDateTime eventDateTime) {
		super(key.getEventId());
		this.key = key;
		this.response = response;
		this.eventDateTime = eventDateTime;
	}

	@Override
	public String toString() {
		return String.format("%s, %s(key=%s)", "포트폴리오 발행 이벤트",
			this.getClass().getSimpleName(),
			key);
	}
}
