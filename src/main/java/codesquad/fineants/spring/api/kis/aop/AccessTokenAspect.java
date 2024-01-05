package codesquad.fineants.spring.api.kis.aop;

import java.time.LocalDateTime;
import java.util.Optional;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import codesquad.fineants.spring.api.kis.client.KisAccessToken;
import codesquad.fineants.spring.api.kis.client.KisClient;
import codesquad.fineants.spring.api.kis.manager.KisAccessTokenManager;
import codesquad.fineants.spring.api.kis.service.KisRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

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
			Optional<KisAccessToken> optionalAccessToken = redisService.getAccessTokenMap();
			optionalAccessToken.ifPresentOrElse(accessToken -> {
				log.info("기존 accessToken 존재로 인한 재사용 : {}", accessToken);
				manager.refreshAccessToken(accessToken);
				log.info("기존 accessToken으로 갱신한 manager : {}", manager);
			}, () -> {
				Mono<KisAccessToken> accessTokenMono = client.accessToken();
				accessTokenMono.subscribe(accessToken -> {
					log.info("kis accessToken 만료로 인한 새로운 accessToken 갱신, accessToken : {}", accessToken);
					redisService.setAccessTokenMap(accessToken, now);
					manager.refreshAccessToken(accessToken);
					log.info("새로 발급한 accessToken으로 갱신한 manager : {}", manager);
				});
			});
		}
	}
}
