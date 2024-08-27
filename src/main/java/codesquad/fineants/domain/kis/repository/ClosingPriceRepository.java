package codesquad.fineants.domain.kis.repository;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.kis.aop.AccessTokenAspect;
import codesquad.fineants.domain.kis.aop.CheckedKisAccessToken;
import codesquad.fineants.domain.kis.client.KisClient;
import codesquad.fineants.domain.kis.domain.dto.response.KisClosingPrice;
import codesquad.fineants.global.common.delay.DelayManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class ClosingPriceRepository {

	private static final String format = "lastDayClosingPrice:%s";
	private final RedisTemplate<String, String> redisTemplate;
	private final KisClient kisClient;
	private final AccessTokenAspect accessTokenAspect;
	private final DelayManager delayManager;

	public void addPrice(String tickerSymbol, long price) {
		redisTemplate.opsForValue().set(String.format(format, tickerSymbol), String.valueOf(price), Duration.ofDays(2));
	}

	public void addPrice(KisClosingPrice price) {
		redisTemplate.opsForValue()
			.set(String.format(format, price.getTickerSymbol()), String.valueOf(price.getPrice()), Duration.ofDays(2));
	}

	@CheckedKisAccessToken
	public Optional<Money> getClosingPrice(String tickerSymbol) {
		String closingPrice = redisTemplate.opsForValue().get(String.format(format, tickerSymbol));
		if (closingPrice == null) {
			handleClosingPrice(tickerSymbol);
			return getClosingPrice(tickerSymbol);
		}
		return Optional.of(Money.won(closingPrice));
	}

	private void handleClosingPrice(String tickerSymbol) {
		kisClient.fetchClosingPrice(tickerSymbol)
			.blockOptional(delayManager.timeout())
			.ifPresent(this::addPrice);
	}
}
