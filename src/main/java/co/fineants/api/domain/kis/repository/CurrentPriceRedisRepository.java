package co.fineants.api.domain.kis.repository;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.global.common.delay.DelayManager;
import co.fineants.api.global.errors.exception.kis.CredentialsTypeKisException;
import co.fineants.api.global.errors.exception.kis.ExpiredAccessTokenKisException;
import co.fineants.api.global.errors.exception.kis.RequestLimitExceededKisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@RequiredArgsConstructor
@Component
@Slf4j
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

	public Optional<String> getCachedPrice(String tickerSymbol) {
		return Optional.ofNullable(redisTemplate.opsForValue().get(String.format(CURRENT_PRICE_FORMAT, tickerSymbol)));
	}

	private Optional<KisCurrentPrice> fetchAndCachePriceFromKis(String tickerSymbol) {
		return kisClient.fetchCurrentPrice(tickerSymbol)
			.doOnSuccess(kisCurrentPrice -> log.debug("reload stock current price {}", kisCurrentPrice))
			.onErrorResume(ExpiredAccessTokenKisException.class::isInstance, throwable -> Mono.empty())
			.onErrorResume(CredentialsTypeKisException.class::isInstance, throwable -> Mono.empty())
			.retryWhen(Retry.fixedDelay(5, delayManager.fixedDelay())
				.filter(RequestLimitExceededKisException.class::isInstance))
			.onErrorResume(Exceptions::isRetryExhausted, throwable -> Mono.empty())
			.blockOptional(delayManager.timeout())
			.map(this::savePrice);
	}
}
