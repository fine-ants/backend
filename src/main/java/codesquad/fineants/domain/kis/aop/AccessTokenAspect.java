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
import reactor.core.publisher.Mono;

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
		if (!manager.isAccessTokenExpired(now)) {
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
		return client.fetchAccessToken()
			.onErrorResume(throwable -> Mono.empty())
			.blockOptional(Duration.ofMinutes(10));
	}
}
