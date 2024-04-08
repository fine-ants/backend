package codesquad.fineants.spring.api.kis.manager;

import static codesquad.fineants.spring.api.kis.service.KisService.*;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.spring.api.kis.aop.AccessTokenAspect;
import codesquad.fineants.spring.api.kis.client.KisClient;
import codesquad.fineants.spring.api.kis.response.KisClosingPrice;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class LastDayClosingPriceManager {

	private static final String format = "lastDayClosingPrice:%s";
	private final RedisTemplate<String, String> redisTemplate;
	private final KisAccessTokenManager accessTokenManager;
	private final KisClient kisClient;
	private final AccessTokenAspect accessTokenAspect;

	public void addPrice(String tickerSymbol, long price) {
		redisTemplate.opsForValue().set(String.format(format, tickerSymbol), String.valueOf(price), Duration.ofDays(2));
	}

	public void addPrice(KisClosingPrice price) {
		redisTemplate.opsForValue()
			.set(String.format(format, price.getTickerSymbol()), String.valueOf(price.getPrice()), Duration.ofDays(2));
	}

	public Optional<Money> getClosingPrice(String tickerSymbol) {
		accessTokenAspect.checkAccessTokenExpiration();
		
		String closingPrice = redisTemplate.opsForValue().get(String.format(format, tickerSymbol));
		if (closingPrice == null) {
			handleClosingPrice(tickerSymbol);
			return getClosingPrice(tickerSymbol);
		}
		return Optional.of(Money.from(closingPrice));
	}

	private void handleClosingPrice(String tickerSymbol) {
		kisClient.fetchClosingPrice(tickerSymbol, accessTokenManager.createAuthorization())
			.blockOptional(TIMEOUT)
			.ifPresent(this::addPrice);
	}
}
