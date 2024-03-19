package codesquad.fineants.spring.api.portfolio_stock.event.listener;

import org.springframework.context.event.EventListener;
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
public class HoldingSseEventListener {

	private static final String EVENT_NAME = "portfolioDetails";
	private static final String COMPLETE_NAME = "complete";

	private final StockMarketChecker stockMarketChecker;
	private final SseEmitterManager manager;

	@EventListener
	public void handleMessage(PortfolioEvent event) {
		SseEmitter emitter = manager.get(event.getKey());
		log.info("emitter 전송준비 : {}", emitter);
		try {
			emitter.send(SseEmitter.event()
				.data(event.getResponse())
				.name(EVENT_NAME));
			log.info("포트폴리오 이벤트 전송 : {}", event);

			if (!stockMarketChecker.isMarketOpen(event.getEventDateTime())) {
				Thread.sleep(2000L);
				emitter.send(SseEmitter.event()
					.data("sse complete")
					.name(COMPLETE_NAME));
				emitter.complete();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			emitter.completeWithError(e);
			manager.remove(event.getKey());
		}
	}
}