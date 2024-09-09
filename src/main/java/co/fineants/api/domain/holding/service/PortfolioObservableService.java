package co.fineants.api.domain.holding.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.reactive.PortfolioObservable;
import co.fineants.api.domain.portfolio.reactive.PortfolioObserver;
import co.fineants.api.domain.portfolio.reactive.StockMarketObserver;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.global.errors.errorcode.PortfolioErrorCode;
import co.fineants.api.global.errors.exception.FineAntsException;
import co.fineants.price.domain.stockprice.service.StockPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class PortfolioObservableService {
	private static final long TIMEOUT = 1000L * 40L;
	private final PortfolioObservable portfolioObservable;
	private final StockMarketChecker stockMarketChecker;
	private final PortfolioRepository portfolioRepository;
	private final LocalDateTimeService localDateTimeService;
	private final StockPriceService stockPriceService;

	@Transactional(readOnly = true)
	public void pushStockTickersBy(Long portfolioId) {
		Portfolio portfolio = portfolioRepository.findByPortfolioIdWithAll(portfolioId)
			.orElseThrow(() -> new FineAntsException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
		Set<String> tickers = portfolio.getPortfolioHoldings().stream()
			.map(PortfolioHolding::getStock)
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toUnmodifiableSet());
		stockPriceService.pushStocks(tickers);
	}

	public SseEmitter observePortfolioHoldings(Long portfolioId) {
		SseEmitter emitter = createSseEmitter(portfolioId);

		if (stockMarketChecker.isMarketOpen(localDateTimeService.getLocalDateTimeWithNow())) {
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
