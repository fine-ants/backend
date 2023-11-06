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

	@Pointcut("execution(* codesquad.fineants.spring.api.kis.KisService.refreshCurrentPrice())")
	public void refreshCurrentPrice() {
	}

	@Pointcut("execution(* codesquad.fineants.spring.api.kis.KisService.readRealTimeCurrentPrice())")
	public void readRealTimeCurrentPrice() {
	}

	@Before(value = "refreshCurrentPrice(), readRealTimeCurrentPrice()")
	public void checkAccessTokenExpiration() {
		if (manager.isAccessTokenExpired(LocalDateTime.now())) {
			final Optional<Map<String, Object>> optionalMap = redisService.getAccessTokenMap();
			optionalMap.ifPresentOrElse(accessTokenMap -> {
				log.info("기존 accessToken 존재로 인한 재사용 : {}", accessTokenMap);
				manager.refreshAccessToken(accessTokenMap);
			}, () -> {
				log.info("kis accessToken 만료로 인한 새로운 accessToken 갱신");
				final Map<String, Object> newAccessTokenMap = client.accessToken();
				redisService.setAccessTokenMap(newAccessTokenMap);
				manager.refreshAccessToken(newAccessTokenMap);
			});
		}
	}
}
