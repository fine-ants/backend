package codesquad.fineants.spring.api.kis;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KisClientScheduler {
	private final ScheduledExecutorService executorService;
	private final BlockingQueue<Runnable> requestQueue;

	public KisClientScheduler() {
		this.executorService = Executors.newScheduledThreadPool(5);
		this.requestQueue = new ArrayBlockingQueue<>(10);
		startScheduling();
	}

	private void startScheduling() {
		executorService.scheduleAtFixedRate(() -> {
			Runnable request = requestQueue.poll();
			if (request != null) {
				executorService.submit(request);
			}
		}, 0, 200, TimeUnit.MILLISECONDS); // 0.2초(200ms) 간격으로 실행
	}

	public void addRequest(Runnable request) {
		requestQueue.offer(request);
	}

}
