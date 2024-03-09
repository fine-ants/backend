package codesquad.fineants.spring.api.kis.aop;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import codesquad.fineants.spring.api.kis.client.KisClient;
import codesquad.fineants.spring.api.kis.manager.KisAccessTokenManager;
import codesquad.fineants.spring.api.kis.properties.OauthKisProperties;
import codesquad.fineants.spring.api.kis.service.KisAccessTokenRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class AccessTokenAspect {

	private final KisAccessTokenManager manager;
	private final KisClient client;
	private final KisAccessTokenRedisService redisService;
	private final OauthKisProperties oauthKisProperties;

	@Pointcut("execution(* codesquad.fineants.spring.api.kis.service.KisService.scheduleRefreshingAllStockCurrentPrice())")
	public void scheduleRefreshingAllStockCurrentPrice() {
	}

	@Pointcut("execution(* codesquad.fineants.spring.api.kis.service.KisService.scheduleRefreshingAllLastDayClosingPrice())")
	public void scheduleRefreshingAllLastDayClosingPrice() {
	}

	@Before(value = "scheduleRefreshingAllStockCurrentPrice() || scheduleRefreshingAllLastDayClosingPrice()")
	public void checkAccessTokenExpiration() {
		LocalDateTime now = LocalDateTime.now();
		if (manager.isAccessTokenExpired(now)) {
			redisService.getAccessTokenMap()
				.ifPresentOrElse(manager::refreshAccessToken, () -> handleNewAccessToken(now));
		}
	}

	private void handleNewAccessToken(LocalDateTime now) {
		CountDownLatch latch = new CountDownLatch(1);
		client.accessToken(oauthKisProperties.getTokenURI())
			.subscribe(accessToken -> {
					redisService.setAccessTokenMap(accessToken, now);
					manager.refreshAccessToken(accessToken);
					log.info("새로운 액세스 토큰 갱신 완료");
				},
				error -> {
					log.error("새로운 액세스 토큰 발급 에러", error);
					latch.countDown();
				}, latch::countDown);
		try {
			latch.await();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
