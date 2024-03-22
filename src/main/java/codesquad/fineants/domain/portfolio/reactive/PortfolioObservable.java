package codesquad.fineants.domain.portfolio.reactive;

import java.util.concurrent.TimeUnit;

import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingsRealTimeResponse;
import codesquad.fineants.spring.api.portfolio_stock.service.PortfolioHoldingService;
import io.reactivex.rxjava3.core.Observable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PortfolioObservable {

	private final PortfolioHoldingService service;

	public Observable<PortfolioHoldingsRealTimeResponse> getPortfolioInfo(Long portfolioId) {
		return Observable.create(emitter ->
			Observable.interval(5, TimeUnit.SECONDS)
				.subscribe(i -> {
					if (!emitter.isDisposed()) {
						log.debug("PortfolioObservable {}", i);
						if (i >= 6) {
							emitter.onComplete();
						} else {
							try {
								emitter.onNext(service.readMyPortfolioStocksInRealTime(portfolioId));
							} catch (Exception e) {
								emitter.onError(e);
							}
						}
					}
				}));
	}

	public Observable<String> getCloseStockMarket() {
		return Observable.create(emitter -> {
			if (!emitter.isDisposed()) {
				try {
					emitter.onNext("sse complete");
					emitter.onComplete();
				} catch (Exception e) {
					emitter.onError(e);
				}
			}
		});
	}
}
