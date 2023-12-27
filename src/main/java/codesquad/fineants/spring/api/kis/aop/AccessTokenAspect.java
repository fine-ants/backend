package codesquad.fineants.spring.api.kis.aop;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import codesquad.fineants.spring.api.kis.KisRedisService;
import codesquad.fineants.spring.api.kis.client.KisClient;
import codesquad.fineants.spring.api.kis.manager.KisAccessTokenManager;
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

	@Pointcut("execution(* codesquad.fineants.spring.api.kis.KisService.refreshStockPrice())")
	public void refreshStockPrice() {
	}

	@Before(value = "refreshStockPrice()")
	public void checkAccessTokenExpiration() {
		LocalDateTime now = LocalDateTime.now();
		log.info("액세스 토큰 만료 체크, 현재 시간={}", now);
		if (manager.isAccessTokenExpired(now)) {
			final Optional<Map<String, Object>> optionalMap = redisService.getAccessTokenMap();
			optionalMap.ifPresentOrElse(accessTokenMap -> {
				log.info("기존 accessToken 존재로 인한 재사용 : {}", accessTokenMap);
				manager.refreshAccessToken(accessTokenMap);
				log.info("기존 accessToken으로 갱신한 manager : {}", manager);
			}, () -> {
				final Map<String, Object> newAccessTokenMap = client.accessToken();
				log.info("kis accessToken 만료로 인한 새로운 accessToken 갱신, newAccessTokenMap : {}", newAccessTokenMap);
				redisService.setAccessTokenMap(newAccessTokenMap, now);
				manager.refreshAccessToken(newAccessTokenMap);
				log.info("새로 발급한 accessToken으로 갱신한 manager : {}", manager);
			});
		}
	}
}
