package codesquad.fineants.spring.api.kis.aop;

import java.time.LocalDateTime;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import codesquad.fineants.spring.api.kis.client.KisClient;
import codesquad.fineants.spring.api.kis.manager.KisAccessTokenManager;
import codesquad.fineants.spring.api.kis.service.KisRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class AccessTokenAspect {

	private final KisAccessTokenManager manager;
	private final KisClient client;
	private final KisRedisService redisService;

	@Pointcut("execution(* codesquad.fineants.spring.api.kis.service.KisService.refreshStockCurrentPrice())")
	public void refreshStockPrice() {
	}

	@Pointcut("execution(* codesquad.fineants.spring.api.kis.service.KisService.refreshLastDayClosingPrice())")
	public void refreshLastDayClosingPrice() {
	}

	@Before(value = "refreshStockPrice() || refreshLastDayClosingPrice()")
	public void checkAccessTokenExpiration() {
		LocalDateTime now = LocalDateTime.now();
		if (manager.isAccessTokenExpired(now)) {
			redisService.getAccessTokenMap()
				.ifPresentOrElse(manager::refreshAccessToken, handleNewAccessToken(now));
		}
	}

	private Runnable handleNewAccessToken(LocalDateTime now) {
		return () -> client.accessToken()
			.subscribe(accessToken -> {
				redisService.setAccessTokenMap(accessToken, now);
				manager.refreshAccessToken(accessToken);
				log.info("새로운 액세스 토큰 갱신 완료");
			});
	}
}
