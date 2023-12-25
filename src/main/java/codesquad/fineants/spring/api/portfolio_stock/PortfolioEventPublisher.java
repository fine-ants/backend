package codesquad.fineants.spring.api.portfolio_stock;

import static java.util.concurrent.TimeUnit.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingsRealTimeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioEventPublisher {
	private static final String EVENT_NAME = "portfolioDetails";
	private static final Map<Long, SseEmitter> clients = new ConcurrentHashMap<>();
	private final ApplicationEventPublisher eventPublisher;
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private final PortfolioStockService portfolioStockService;
	private final StockMarketChecker stockMarketChecker;

	@PostConstruct
	public void startProcessing() {
		this.executor.schedule(() -> this.sendEventToPortfolio(LocalDateTime.now()), 1, SECONDS);
	}

	public void sendEventToPortfolio(LocalDateTime now) {
		for (Long id : clients.keySet()) {
			try {
				PortfolioHoldingsRealTimeResponse response = portfolioStockService.readMyPortfolioStocksInRealTime(id);
				PortfolioEvent portfolioEvent = new PortfolioEvent(id, response, now);
				eventPublisher.publishEvent(portfolioEvent);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				clients.get(id).completeWithError(e);
			}
		}
		this.executor.schedule(() -> this.sendEventToPortfolio(LocalDateTime.now()), 5, SECONDS);
	}

	@Async
	@EventListener
	public void handleMessage(PortfolioEvent event) {
		SseEmitter emitter = clients.get(event.getPortfolioId());
		try {
			emitter.send(SseEmitter.event()
				.data(event.getResponse())
				.name(EVENT_NAME));

			if (!stockMarketChecker.isMarketOpen(event.getEventDateTime())) {
				Thread.sleep(2000L);
				emitter.send(SseEmitter.event()
					.data("sse complete")
					.name("complete"));
				emitter.complete();
			}
		} catch (IOException | InterruptedException e) {
			log.info(e.getMessage(), e);
			emitter.completeWithError(e);
		}
	}

	public void add(Long portfolioId, SseEmitter emitter) {
		clients.put(portfolioId, emitter);
	}

	public void remove(Long portfolioId) {
		clients.remove(portfolioId);
	}
}
