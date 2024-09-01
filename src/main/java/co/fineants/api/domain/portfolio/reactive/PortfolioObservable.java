package co.fineants.api.domain.portfolio.reactive;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;
import co.fineants.api.domain.holding.service.PortfolioHoldingService;
import io.reactivex.rxjava3.core.Observable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class PortfolioObservable {

	private final PortfolioHoldingService service;

	public Observable<PortfolioHoldingsRealTimeResponse> getPortfolioInfo(Long portfolioId) {
		return Observable.create(emitter ->
			Observable.interval(5, TimeUnit.SECONDS)
				.take(6)
				.subscribe(i -> {
					if (!emitter.isDisposed()) {
						log.debug("PortfolioObservable {}", i);
						try {
							emitter.onNext(service.readMyPortfolioStocksInRealTime(portfolioId));
						} catch (Exception e) {
							emitter.onError(e);
						}
					}
				}));
	}

	public Observable<String> getCloseStockMarket() {
		String dummyContent = "sse complete";
		return Observable.create(emitter -> {
			if (!emitter.isDisposed()) {
				try {
					emitter.onNext(dummyContent);
					emitter.onComplete();
				} catch (Exception e) {
					emitter.onError(e);
				}
			}
		});
	}
}
