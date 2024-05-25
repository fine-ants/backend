package codesquad.fineants.domain.holding.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import codesquad.fineants.domain.portfolio.reactive.PortfolioObservable;
import codesquad.fineants.domain.portfolio.reactive.PortfolioObserver;
import codesquad.fineants.domain.portfolio.reactive.StockMarketObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class PortfolioObservableService {
	private static final long TIMEOUT = 1000 * 40;
	private final PortfolioObservable portfolioObservable;
	private final StockMarketChecker stockMarketChecker;

	public SseEmitter observePortfolioHoldings(Long portfolioId) {
		SseEmitter emitter = createSseEmitter(portfolioId);

		if (stockMarketChecker.isMarketOpen(LocalDateTime.now())) {
			portfolioObservable.getPortfolioInfo(portfolioId)
				.subscribe(PortfolioObserver.create(emitter));
			return emitter;
		}
		portfolioObservable.getCloseStockMarket()
			.subscribe(StockMarketObserver.create(emitter));
		return emitter;
	}

	private SseEmitter createSseEmitter(Long portfolioId) {
		SseEmitter emitter = new SseEmitter(TIMEOUT);
		emitter.onTimeout(() -> {
			log.info("emitter{} timeout으로 인한 제거", portfolioId);
			emitter.complete();
		});
		emitter.onCompletion(() -> log.info("emitter{} completion으로 인한 제거", portfolioId));
		emitter.onError(throwable -> {
			log.error(throwable.getMessage());
			emitter.completeWithError(throwable);
		});
		return emitter;
	}
}
