package codesquad.fineants.domain.kis.repository;

import static codesquad.fineants.domain.kis.service.KisService.*;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.kis.client.KisClient;
import codesquad.fineants.domain.kis.client.KisCurrentPrice;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class CurrentPriceRedisRepository implements PriceRepository {
	private static final String CURRENT_PRICE_FORMAT = "cp:%s";
	private final RedisTemplate<String, String> redisTemplate;
	private final KisClient kisClient;
	
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
			.blockOptional(TIMEOUT)
			.map(this::savePrice);
	}
}
