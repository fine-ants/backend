package codesquad.fineants.domain.portfolio.reactive;

import java.io.IOException;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import codesquad.fineants.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioObserver implements Observer<PortfolioHoldingsRealTimeResponse> {

	public static final String EVENT_NAME = "portfolioDetails";
	private final SseEmitter emitter;

	public static PortfolioObserver create(SseEmitter emitter) {
		return new PortfolioObserver(emitter);
	}

	@Override
	public void onSubscribe(@NonNull Disposable d) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void onNext(@NonNull PortfolioHoldingsRealTimeResponse data) {
		try {
			emitter.send(SseEmitter.event()
				.data(data)
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
