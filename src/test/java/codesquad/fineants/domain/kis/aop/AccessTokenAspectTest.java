package codesquad.fineants.domain.kis.aop;

import static org.mockito.BDDMockito.*;

import java.time.Duration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.kis.client.KisClient;
import codesquad.fineants.domain.kis.repository.KisAccessTokenRepository;
import codesquad.fineants.domain.kis.service.KisAccessTokenRedisService;
import codesquad.fineants.global.common.time.LocalDateTimeService;
import codesquad.fineants.global.errors.exception.kis.ExpiredAccessTokenKisException;
import reactor.core.publisher.Mono;

class AccessTokenAspectTest extends AbstractContainerBaseTest {

	@MockBean
	private KisClient client;

	@Autowired
	private KisAccessTokenRedisService kisAccessTokenRedisService;

	@Autowired
	private LocalDateTimeService localDateTimeService;

	@AfterEach
	void tearDown() {
		kisAccessTokenRedisService.deleteAccessTokenMap();
	}

	@DisplayName("액세스 토큰을 새로 발급하여 redis에 저장한다")
	@Test
	void checkAccessTokenExpiration() {
		// given
		AccessTokenAspect accessTokenAspect = new AccessTokenAspect(new KisAccessTokenRepository(null), client,
			kisAccessTokenRedisService, localDateTimeService);
		kisAccessTokenRedisService.deleteAccessTokenMap();

		given(client.fetchAccessToken())
			.willReturn(
				Mono.just(createKisAccessToken())
					.delayElement(Duration.ofMillis(500L))
			);
		// when
		accessTokenAspect.checkAccessTokenExpiration();
		// then
		Assertions.assertThat(kisAccessTokenRedisService.getAccessTokenMap().isPresent()).isTrue();
	}

	@DisplayName("액세스 토큰 발급이 실패하고 예외가 발생한다")
	@Test
	void checkAccessTokenExpiration_whenAccessTokenExpiredAndFailFetchAccessToken_thenThrowException() {
		// given
		AccessTokenAspect accessTokenAspect = new AccessTokenAspect(new KisAccessTokenRepository(null), client,
			kisAccessTokenRedisService, localDateTimeService);
		kisAccessTokenRedisService.deleteAccessTokenMap();

		given(client.fetchAccessToken())
			.willReturn(Mono.error(new ExpiredAccessTokenKisException("1", "EGW00201", "초당 거래건수를 초과하였습니다.")));
		// when
		Throwable throwable = Assertions.catchThrowable(accessTokenAspect::checkAccessTokenExpiration);
		// then
		Assertions.assertThat(throwable)
			.isInstanceOf(ExpiredAccessTokenKisException.class)
			.hasMessage("초당 거래건수를 초과하였습니다.");
	}
}
