package codesquad.fineants.spring.api.portfolio_stock.manager;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseEmitterManager {
	private final Map<Long, SseEmitter> clients = new ConcurrentHashMap<>();

	public void add(Long portfolioId, SseEmitter emitter) {
		clients.put(portfolioId, emitter);
	}

	public void remove(Long portfolioId) {
		clients.remove(portfolioId);
	}

	public int size() {
		return clients.size();
	}

	public SseEmitter get(Long id) {
		return clients.get(id);
	}

	public Set<Long> keys() {
		return clients.keySet();
	}

	public void complete(Long portfolioId) {
		clients.get(portfolioId).complete();
	}
}
