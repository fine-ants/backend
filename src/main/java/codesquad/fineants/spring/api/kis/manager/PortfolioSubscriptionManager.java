package codesquad.fineants.spring.api.kis.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import codesquad.fineants.spring.api.kis.PortfolioSubscription;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PortfolioSubscriptionManager {
	private static final Map<String, PortfolioSubscription> portfolioSubscriptions = new ConcurrentHashMap<>();

	public void addPortfolioSubscription(String sessionId, PortfolioSubscription subscription) {
		if (sessionId == null) {
			return;
		}
		PortfolioSubscription sub = portfolioSubscriptions.put(sessionId, subscription);
		log.info("포트폴리오 구독 추가 : portfolioSubscription={}, ", sub);
	}

	public void removePortfolioSubscription(String sessionId) {
		PortfolioSubscription delSubscription = portfolioSubscriptions.remove(sessionId);
		log.info("포트폴리오 구독 삭제 : {}", delSubscription);
	}

	public Optional<PortfolioSubscription> getPortfolioSubscription(Long portfolioId) {
		return portfolioSubscriptions.values().stream()
			.filter(subscription -> subscription.getPortfolioId().equals(portfolioId))
			.findAny();
	}

	public List<PortfolioSubscription> values() {
		return new ArrayList<>(portfolioSubscriptions.values());
	}

	public int size() {
		return portfolioSubscriptions.size();
	}
}
