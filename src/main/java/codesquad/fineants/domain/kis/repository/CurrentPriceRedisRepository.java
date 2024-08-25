package codesquad.fineants.domain.kis.repository;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.kis.client.KisClient;
import codesquad.fineants.domain.kis.client.KisCurrentPrice;
import codesquad.fineants.global.common.delay.DelayManager;
import lombok.RequiredArgsConstructor;
import reactor.util.retry.Retry;

@RequiredArgsConstructor
@Repository
public class CurrentPriceRedisRepository implements PriceRepository {
	private static final String CURRENT_PRICE_FORMAT = "cp:%s";
	private final RedisTemplate<String, String> redisTemplate;
	private final KisClient kisClient;
	private final DelayManager delayManager;

	@Override
	public void savePrice(KisCurrentPrice... currentPrices) {
		Arrays.stream(currentPrices).forEach(this::savePrice);
	}

	private KisCurrentPrice savePrice(KisCurrentPrice currentPrice) {
		redisTemplate.opsForValue().set(currentPrice.toRedisKey(CURRENT_PRICE_FORMAT), currentPrice.toRedisValue());
		return currentPrice;
	}

	@Override
	public Optional<Money> fetchPriceBy(String tickerSymbol) {
		Optional<String> currentPrice = getCachedPrice(tickerSymbol);
		if (currentPrice.isEmpty()) {
			Optional<KisCurrentPrice> kisCurrentPrice = fetchAndCachePriceFromKis(tickerSymbol);
			return kisCurrentPrice
				.map(KisCurrentPrice::getPrice)
				.map(Money::won);
		}
		return currentPrice.map(Money::won);
	}

	private Optional<String> getCachedPrice(String tickerSymbol) {
		return Optional.ofNullable(redisTemplate.opsForValue().get(String.format(CURRENT_PRICE_FORMAT, tickerSymbol)));
	}

	private Optional<KisCurrentPrice> fetchAndCachePriceFromKis(String tickerSymbol) {
		return kisClient.fetchCurrentPrice(tickerSymbol)
			.retryWhen(Retry.fixedDelay(5, delayManager.fixedDelay()))
			.blockOptional(delayManager.timeout())
			.map(this::savePrice);
	}
}
