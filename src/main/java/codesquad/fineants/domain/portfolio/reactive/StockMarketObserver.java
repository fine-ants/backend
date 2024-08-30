package codesquad.fineants.domain.portfolio.reactive;

import java.io.IOException;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class StockMarketObserver implements Observer<String> {

	private static final String COMPLETE_NAME = "complete";
	private final SseEmitter emitter;

	public static StockMarketObserver create(SseEmitter emitter) {
		return new StockMarketObserver(emitter);
	}

	@Override
	public void onSubscribe(@NonNull Disposable d) {
		throw new UnsupportedOperationException("Not implemented");
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
