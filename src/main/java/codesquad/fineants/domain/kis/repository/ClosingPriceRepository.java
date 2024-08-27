package codesquad.fineants.domain.kis.repository;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.kis.client.KisClient;
import codesquad.fineants.domain.kis.domain.dto.response.KisClosingPrice;
import codesquad.fineants.global.common.delay.DelayManager;
import codesquad.fineants.global.errors.exception.kis.CredentialsTypeKisException;
import codesquad.fineants.global.errors.exception.kis.ExpiredAccessTokenKisException;
import codesquad.fineants.global.errors.exception.kis.RequestLimitExceededKisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClosingPriceRepository {

	private static final String CLOSING_PRICE_FORMAT = "lastDayClosingPrice:%s";
	private final RedisTemplate<String, String> redisTemplate;
	private final KisClient kisClient;
	private final DelayManager delayManager;

	public void addPrice(String tickerSymbol, long price) {
		addPrice(KisClosingPrice.create(tickerSymbol, price));
	}

	public void addPrice(KisClosingPrice price) {
		redisTemplate.opsForValue()
			.set(String.format(CLOSING_PRICE_FORMAT, price.getTickerSymbol()), String.valueOf(price.getPrice()),
				Duration.ofDays(2));
	}

	public Optional<Money> fetchPrice(String tickerSymbol) {
		Optional<String> cachedPrice = getCachedPrice(tickerSymbol);
		if (cachedPrice.isEmpty()) {
			Optional<KisClosingPrice> price = fetchClosingPriceFromKis(tickerSymbol);
			price.ifPresent(this::addPrice);
			return price
				.map(KisClosingPrice::getPrice)
				.map(Money::won);
		}
		return cachedPrice.map(Money::won);
	}

	private Optional<String> getCachedPrice(String tickerSymbol) {
		return Optional.ofNullable(redisTemplate.opsForValue().get(String.format(CLOSING_PRICE_FORMAT, tickerSymbol)));
	}

	private Optional<KisClosingPrice> fetchClosingPriceFromKis(String tickerSymbol) {
		return kisClient.fetchClosingPrice(tickerSymbol)
			.doOnSuccess(price -> log.debug("reload stock current price {}", price))
			.onErrorResume(ExpiredAccessTokenKisException.class::isInstance, throwable -> Mono.empty())
			.onErrorResume(CredentialsTypeKisException.class::isInstance, throwable -> Mono.empty())
			.retryWhen(Retry.fixedDelay(5, delayManager.fixedDelay())
				.filter(RequestLimitExceededKisException.class::isInstance))
			.onErrorResume(Exceptions::isRetryExhausted, throwable -> Mono.empty())
			.blockOptional(delayManager.timeout());
	}
}
