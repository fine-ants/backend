package codesquad.fineants.spring.api.portfolio_stock.event.publisher;

import static java.util.concurrent.TimeUnit.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import codesquad.fineants.spring.api.portfolio_stock.event.PortfolioEvent;
import codesquad.fineants.spring.api.portfolio_stock.manager.SseEmitterKey;
import codesquad.fineants.spring.api.portfolio_stock.manager.SseEmitterManager;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingsRealTimeResponse;
import codesquad.fineants.spring.api.portfolio_stock.service.PortfolioHoldingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioHoldingSseEventPublisher {

	private final ApplicationEventPublisher eventPublisher;
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private final PortfolioHoldingService portfolioHoldingService;
	private final SseEmitterManager manager;

	@PostConstruct
	public void startProcessing() {
		this.executor.schedule(() -> this.sendEventToPortfolio(LocalDateTime.now()), 1, SECONDS);
	}

	public void sendEventToPortfolio(LocalDateTime eventDatetime) {
		log.info("sseEmitter 개수 : {}", manager.size());
		publishPortfolioEvent(eventDatetime);
		this.executor.schedule(() -> this.sendEventToPortfolio(LocalDateTime.now()), 5, SECONDS);
	}

	public void publishPortfolioEvent(LocalDateTime eventDatetime) {
		List<SseEmitterKey> deadEmitters = new ArrayList<>();
		for (SseEmitterKey key : manager.keys()) {
			try {
				PortfolioHoldingsRealTimeResponse response = portfolioHoldingService.readMyPortfolioStocksInRealTime(
					key.getPortfolioId());
				PortfolioEvent portfolioEvent = new PortfolioEvent(key, response, eventDatetime);
				eventPublisher.publishEvent(portfolioEvent);
				log.info("{}", portfolioEvent);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				manager.get(key).completeWithError(e);
				deadEmitters.add(key);
			}
		}
		deadEmitters.forEach(manager::remove);
	}
}
