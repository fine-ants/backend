package codesquad.fineants.spring.api.portfolio_stock.event;

import static java.util.concurrent.TimeUnit.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import codesquad.fineants.spring.api.portfolio_stock.manager.SseEmitterManager;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingsRealTimeResponse;
import codesquad.fineants.spring.api.portfolio_stock.service.PortfolioStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioEventPublisher {

	private final ApplicationEventPublisher eventPublisher;
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private final PortfolioStockService portfolioStockService;
	private final SseEmitterManager manager;

	@PostConstruct
	public void startProcessing() {
		this.executor.schedule(() -> this.sendEventToPortfolio(LocalDateTime.now()), 1, SECONDS);
	}

	public void sendEventToPortfolio(LocalDateTime eventDatetime) {
		List<Long> deadEmitters = new ArrayList<>();
		for (Long id : manager.keys()) {
			try {
				PortfolioHoldingsRealTimeResponse response = portfolioStockService.readMyPortfolioStocksInRealTime(id);
				PortfolioEvent portfolioEvent = new PortfolioEvent(id, response, eventDatetime);
				eventPublisher.publishEvent(portfolioEvent);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				manager.get(id).completeWithError(e);
				deadEmitters.add(id);
			}
		}
		deadEmitters.forEach(manager::remove);
		this.executor.schedule(() -> this.sendEventToPortfolio(LocalDateTime.now()), 5, SECONDS);
	}
}
