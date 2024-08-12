package codesquad.fineants.domain.kis.repository;

import static codesquad.fineants.domain.kis.service.KisService.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.kis.aop.AccessTokenAspect;
import codesquad.fineants.domain.kis.client.KisClient;
import codesquad.fineants.domain.kis.client.KisCurrentPrice;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class CurrentPriceRepository {
	private static final String CURRENT_PRICE_FORMAT = "cp:%s";
	private final RedisTemplate<String, String> redisTemplate;
	private final KisClient kisClient;
	private final AccessTokenAspect accessTokenAspect;

	public void addCurrentPrice(KisCurrentPrice currentPrice) {
		addCurrentPrice(List.of(currentPrice));
	}

	public void addCurrentPrice(List<KisCurrentPrice> currentPrices) {
		currentPrices.forEach(currentPrice ->
			redisTemplate.opsForValue()
				.set(String.format(CURRENT_PRICE_FORMAT, currentPrice.getTickerSymbol()),
					String.valueOf(currentPrice.getPrice())));
	}

	public Optional<Money> getCurrentPrice(String tickerSymbol) {
		String currentPrice = redisTemplate.opsForValue().get(String.format(CURRENT_PRICE_FORMAT, tickerSymbol));
		if (currentPrice == null) {
			handleCurrentPrice(tickerSymbol);
			return getCurrentPrice(tickerSymbol);
		}
		return Optional.of(Money.won(currentPrice));
	}

	private void handleCurrentPrice(String tickerSymbol) {
		kisClient.fetchCurrentPrice(tickerSymbol)
			.blockOptional(TIMEOUT)
			.ifPresent(this::addCurrentPrice);
	}
}
