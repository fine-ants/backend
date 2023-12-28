package codesquad.fineants.spring.api.portfolio_stock.event.listener;

import java.io.IOException;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import codesquad.fineants.spring.api.portfolio_stock.event.PortfolioEvent;
import codesquad.fineants.spring.api.portfolio_stock.manager.SseEmitterManager;
import codesquad.fineants.spring.api.portfolio_stock.service.StockMarketChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioEventListener {

	private static final String EVENT_NAME = "portfolioDetails";
	private static final String COMPLETE_NAME = "complete";

	private final StockMarketChecker stockMarketChecker;
	private final SseEmitterManager manager;

	@Async
	@EventListener
	public void handleMessage(PortfolioEvent event) {
		SseEmitter emitter = manager.get(event.getPortfolioId());
		try {
			emitter.send(SseEmitter.event()
				.data(event.getResponse())
				.name(EVENT_NAME));

			if (!stockMarketChecker.isMarketOpen(event.getEventDateTime())) {
				Thread.sleep(2000L);
				emitter.send(SseEmitter.event()
					.data("sse complete")
					.name(COMPLETE_NAME));
				emitter.complete();
			}
		} catch (IOException | InterruptedException e) {
			log.info(e.getMessage(), e);
			emitter.completeWithError(e);
		}
	}

}
