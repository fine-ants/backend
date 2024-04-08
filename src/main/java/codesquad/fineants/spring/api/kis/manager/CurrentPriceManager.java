package codesquad.fineants.spring.api.kis.manager;

import static codesquad.fineants.spring.api.kis.service.KisService.*;

import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.spring.api.kis.aop.AccessTokenAspect;
import codesquad.fineants.spring.api.kis.client.KisClient;
import codesquad.fineants.spring.api.kis.client.KisCurrentPrice;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class CurrentPriceManager {
	private static final String format = "cp:%s";
	private final RedisTemplate<String, String> redisTemplate;
	private final KisAccessTokenManager accessTokenManager;
	private final KisClient kisClient;
	private final AccessTokenAspect accessTokenAspect;

	public void addCurrentPrice(KisCurrentPrice currentPrice) {
		redisTemplate.opsForValue()
			.set(String.format(format, currentPrice.getTickerSymbol()),
				String.valueOf(currentPrice.getPrice()));
	}

	public Optional<Money> getCurrentPrice(String tickerSymbol) {
		accessTokenAspect.checkAccessTokenExpiration();
		String currentPrice = redisTemplate.opsForValue().get(String.format(format, tickerSymbol));
		if (currentPrice == null) {
			handleCurrentPrice(tickerSymbol);
			return getCurrentPrice(tickerSymbol);
		}
		return Optional.of(Money.from(currentPrice));
	}

	private void handleCurrentPrice(String tickerSymbol) {
		kisClient.fetchCurrentPrice(tickerSymbol, accessTokenManager.createAuthorization())
			.blockOptional(TIMEOUT)
			.ifPresent(this::addCurrentPrice);
	}

	public boolean hasCurrentPrice(String tickerSymbol) {
		String currentPrice = redisTemplate.opsForValue().get(String.format(format, tickerSymbol));
		return currentPrice != null;
	}
}
