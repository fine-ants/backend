package codesquad.fineants.domain.kis.aop;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import codesquad.fineants.domain.kis.client.KisAccessToken;
import codesquad.fineants.domain.kis.client.KisClient;
import codesquad.fineants.domain.kis.repository.KisAccessTokenRepository;
import codesquad.fineants.domain.kis.service.KisAccessTokenRedisService;
import codesquad.fineants.global.common.time.LocalDateTimeService;
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
	private final LocalDateTimeService localDateTimeService;

	@Before(value = "@annotation(CheckedKisAccessToken) && args(..)")
	public void checkAccessTokenExpiration() {
		LocalDateTime now = localDateTimeService.getLocalDateTimeWithNow();
		// 액세스 토큰이 만료 1시간 이전이라면 토큰을 재발급한다
		if (manager.isTokenExpiringSoon(now)) {
			client.fetchAccessToken()
				.doOnSuccess(kisAccessToken -> log.debug("success the kis access token issue : {}", kisAccessToken))
				.blockOptional(Duration.ofMinutes(10))
				.ifPresent(newKisAccessToken -> {
					redisService.setAccessTokenMap(newKisAccessToken, now);
					manager.refreshAccessToken(newKisAccessToken);
					log.info("만료 1시간 이전의 액세스 토큰 재발급 {}", newKisAccessToken);
				});
			return;
		}
		if (!manager.isAccessTokenExpired(now)) {
			log.debug("access token is not expired");
			return;
		}
		redisService.getAccessTokenMap()
			.ifPresentOrElse(manager::refreshAccessToken, () ->
				handleNewAccessToken().ifPresentOrElse(newKisAccessToken -> {
					redisService.setAccessTokenMap(newKisAccessToken, now);
					manager.refreshAccessToken(newKisAccessToken);
					log.info("complete the newKisAccessToken");
				}, () -> {
					log.error("fail to newKisAccessToken");
					throw new FineAntsException(KisErrorCode.ACCESS_TOKEN_ISSUE_FAIL);
				}));
	}

	private Optional<KisAccessToken> handleNewAccessToken() {
		Optional<KisAccessToken> result = client.fetchAccessToken()
			.blockOptional(Duration.ofMinutes(10));
		log.info("new access Token Issue : {}", result);
		return result;
	}
}
