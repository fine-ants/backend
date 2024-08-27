package codesquad.fineants.domain.kis.scheduler;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import codesquad.fineants.domain.kis.client.KisClient;
import codesquad.fineants.domain.kis.repository.KisAccessTokenRepository;
import codesquad.fineants.domain.kis.service.KisAccessTokenRedisService;
import codesquad.fineants.global.common.delay.DelayManager;
import codesquad.fineants.global.common.time.LocalDateTimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
@RequiredArgsConstructor
@Slf4j
public class KisAccessTokenScheduler {

	private final KisAccessTokenRepository manager;
	private final KisAccessTokenRedisService redisService;
	private final LocalDateTimeService localDateTimeService;
	private final DelayManager delayManager;
	private final KisClient kisClient;

	@Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
	public void checkAndReissueAccessToken() {
		LocalDateTime now = localDateTimeService.getLocalDateTimeWithNow();
		if (!manager.isTokenExpiringSoon(now)) {
			return;
		}
		kisClient.fetchAccessToken()
			.doOnSuccess(kisAccessToken -> log.debug("success the kis access token issue : {}", kisAccessToken))
			.retryWhen(Retry.fixedDelay(5, delayManager.fixedAccessTokenDelay()))
			.onErrorResume(Exceptions::isRetryExhausted, throwable -> {
				log.error("fail the retry, error message is {}", throwable.getMessage());
				return Mono.empty();
			})
			.onErrorResume(throwable -> {
				log.error("fail the fetch accessToken, error message is {}", throwable.getMessage());
				return Mono.empty();
			})
			.blockOptional(delayManager.timeout())
			.ifPresent(newKisAccessToken -> {
				redisService.setAccessTokenMap(newKisAccessToken, now);
				manager.refreshAccessToken(newKisAccessToken);
				log.info("Reissue access tokens 1 hour prior to expiration {}", newKisAccessToken);
			});
	}
}
