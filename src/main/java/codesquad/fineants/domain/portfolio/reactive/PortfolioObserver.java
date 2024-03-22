package codesquad.fineants.domain.portfolio.reactive;

import static codesquad.fineants.spring.api.portfolio_stock.event.listener.HoldingSseEventListener.*;

import java.io.IOException;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingsRealTimeResponse;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PortfolioObserver implements Observer<PortfolioHoldingsRealTimeResponse> {

	private final SseEmitter emitter;

	@Override
	public void onSubscribe(@NonNull Disposable d) {
	}

	@Override
	public void onNext(@NonNull PortfolioHoldingsRealTimeResponse info) {
		try {
			emitter.send(SseEmitter.event()
				.data(info)
				.name(EVENT_NAME));
		} catch (IOException e) {
			onError(e);
		}
	}

	@Override
	public void onError(@NonNull Throwable e) {
		log.error(e.getMessage());
		emitter.completeWithError(e);
	}

	@Override
	public void onComplete() {
		log.info("sseEmitter {} complete", emitter);
		emitter.complete();
	}
}
