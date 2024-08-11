package codesquad.fineants.domain.kis.aop;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import codesquad.fineants.domain.kis.client.KisAccessToken;
import codesquad.fineants.domain.kis.client.KisClient;
import codesquad.fineants.domain.kis.repository.KisAccessTokenRepository;
import codesquad.fineants.domain.kis.service.KisAccessTokenRedisService;
import codesquad.fineants.global.errors.errorcode.KisErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AccessTokenAspect {

	private final KisAccessTokenRepository manager;
	private final KisClient client;
	private final KisAccessTokenRedisService redisService;

	@Pointcut("execution(* codesquad.fineants.domain.kis.service.KisService.refreshCurrentPrice())")
	public void refreshCurrentPrice() {
	}

	@Pointcut("execution(* codesquad.fineants.domain.kis.service.KisService.refreshClosingPrice())")
	public void refreshClosingPrice() {
	}

	@Pointcut("execution(* codesquad.fineants.domain.kis.service.KisService.fetchDividend())")
	public void fetchDividend() {
	}

	@Before(value = "refreshCurrentPrice() || refreshClosingPrice() || fetchDividend()")
	public void checkAccessTokenExpiration() {
		LocalDateTime now = LocalDateTime.now();
		if (manager.isAccessTokenExpired(now)) {
			Optional<KisAccessToken> optionalKisAccessToken = redisService.getAccessTokenMap();
			if (optionalKisAccessToken.isPresent()) {
				manager.refreshAccessToken(optionalKisAccessToken.get());
			} else {
				try {
					handleNewAccessToken(now);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private void handleNewAccessToken(LocalDateTime now) throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);
		client.fetchAccessToken()
			.subscribe(accessToken -> {
				redisService.setAccessTokenMap(accessToken, now);
				manager.refreshAccessToken(accessToken);
				log.info("새로운 액세스 토큰 갱신 완료");
			}, error -> {
				log.error("새로운 액세스 토큰 발급 에러", error);
				latch.countDown();
			}, latch::countDown);
		try {
			latch.await();
		} catch (InterruptedException e) {
			log.error(e.getMessage());
			throw new InterruptedException(e.getMessage());
		}
	}

	@Before(value = "@annotation(CheckedKisAccessToken) && args(..)")
	public void zzCheckAccessTokenExpiration() {
		LocalDateTime now = LocalDateTime.now();
		if (manager.isAccessTokenExpired(now)) {
			redisService.getAccessTokenMap()
				.ifPresentOrElse(manager::refreshAccessToken, () ->
					handleNewAccessToken().ifPresentOrElse(newKisAccessToken -> {
						redisService.setAccessTokenMap(newKisAccessToken, now);
						manager.refreshAccessToken(newKisAccessToken);
						log.info("complete the newKisAccessToken");
					}, () -> {
						throw new FineAntsException(KisErrorCode.ACCESS_TOKEN_ISSUE_FAIL);
					}));
		}
	}

	private Optional<KisAccessToken> handleNewAccessToken() {
		return client.fetchAccessToken()
			.blockOptional(Duration.ofMinutes(10));
	}
}
