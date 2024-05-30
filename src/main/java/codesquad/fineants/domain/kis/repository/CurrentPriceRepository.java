package codesquad.fineants.domain.kis.repository;

import static codesquad.fineants.domain.kis.service.KisService.*;

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
	private static final String format = "cp:%s";
	private final RedisTemplate<String, String> redisTemplate;
	private final KisAccessTokenRepository accessTokenManager;
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
		return Optional.of(Money.won(currentPrice));
	}

	private void handleCurrentPrice(String tickerSymbol) {
		kisClient.fetchCurrentPrice(tickerSymbol, accessTokenManager.createAuthorization())
			.blockOptional(TIMEOUT)
			.ifPresent(this::addCurrentPrice);
	}
}
