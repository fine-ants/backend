package codesquad.fineants.spring.api.kis.manager;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import codesquad.fineants.spring.api.kis.response.CurrentPriceResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class CurrentPriceManager {
	private static final String format = "cp:%s";
	private final RedisTemplate<String, String> redisTemplate;

	public void addKey(String tickerSymbol) {
		redisTemplate.opsForValue().set(String.format(format, tickerSymbol), "0");
	}

	public void addCurrentPrice(CurrentPriceResponse response) {
		redisTemplate.opsForValue()
			.set(String.format(format, response.getTickerSymbol()), String.valueOf(response.getCurrentPrice()));
	}

	public Long getCurrentPrice(String tickerSymbol) {
		String currentPrice = redisTemplate.opsForValue().get(String.format(format, tickerSymbol));
		if (currentPrice == null || currentPrice.equals("0")) {
			return 0L;
		}
		return Long.valueOf(currentPrice);
	}

	public boolean hasCurrentPrice(String tickerSymbol) {
		String currentPrice = redisTemplate.opsForValue().get(String.format(format, tickerSymbol));
		return currentPrice != null && !currentPrice.equals("0");
	}

	public boolean hasKey(String tickerSymbol) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(String.format(format, tickerSymbol)));
	}
}
