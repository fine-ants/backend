package codesquad.fineants.domain.portfolio.reactive;

import static codesquad.fineants.spring.api.portfolio_stock.event.listener.HoldingSseEventListener.*;

import java.io.IOException;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class StockMarketObserver implements Observer<String> {

	private final SseEmitter emitter;

	@Override
	public void onSubscribe(@NonNull Disposable d) {

	}

	@Override
	public void onNext(@NonNull String value) {
		try {
			emitter.send(SseEmitter.event()
				.data(value)
				.name(COMPLETE_NAME));
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
		emitter.complete();
	}
}
