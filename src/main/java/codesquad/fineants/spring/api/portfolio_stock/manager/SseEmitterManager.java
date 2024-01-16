package codesquad.fineants.spring.api.portfolio_stock.manager;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseEmitterManager {
	private final Map<SseEmitterKey, SseEmitter> clients = new ConcurrentHashMap<>();

	public void add(SseEmitterKey key, SseEmitter emitter) {
		clients.put(key, emitter);
	}

	public void remove(SseEmitterKey key) {
		clients.remove(key);
	}

	public int size() {
		return clients.size();
	}

	public SseEmitter get(SseEmitterKey key) {
		return clients.get(key);
	}

	public Set<SseEmitterKey> keys() {
		return clients.keySet();
	}

	public void clear() {
		clients.clear();
	}
}
