package codesquad.fineants.spring.api.kis.manager;

import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import codesquad.fineants.spring.api.kis.response.CurrentPriceResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class CurrentPriceManager {
	private static final String format = "cp:%s";
	private final RedisTemplate<String, String> redisTemplate;

	public void addCurrentPrice(CurrentPriceResponse response) {
		redisTemplate.opsForValue()
			.set(String.format(format, response.getTickerSymbol()),
				String.valueOf(response.getCurrentPrice()));
	}

	public Optional<Long> getCurrentPrice(String tickerSymbol) {
		String currentPrice = redisTemplate.opsForValue().get(String.format(format, tickerSymbol));
		if (currentPrice == null) {
			return Optional.empty();
		}
		return Optional.of(Long.valueOf(currentPrice));
	}

	public boolean hasCurrentPrice(String tickerSymbol) {
		String currentPrice = redisTemplate.opsForValue().get(String.format(format, tickerSymbol));
		return currentPrice != null;
	}
}
